package br.com.zup.edu.endpoint

import br.com.zup.edu.*
import br.com.zup.edu.clients.BCBClient
import br.com.zup.edu.clients.ContaClient
import br.com.zup.edu.endpoint.dto.IdPix
import br.com.zup.edu.endpoint.dto.NovaChavePix
import br.com.zup.edu.extension.toModel
import br.com.zup.edu.model.Conta
import br.com.zup.edu.repository.ChavePixRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException
import javax.validation.Valid

@Validated
@Singleton
class PixEndpoint(
    @Inject val contaClient: ContaClient,
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val bcbClient: BCBClient
) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    override fun registraChavePix(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {

        try {
            val idPix = registra(request.toModel())

            responseObserver.onNext(ChavePixResponse.newBuilder().setPixId(idPix).build())

            responseObserver.onCompleted()

        } catch (e: ConstraintViolationException) {
            responseObserver.onError(
                StatusRuntimeException(
                    Status.INVALID_ARGUMENT
                        .withDescription("Dados invalidos")
                )
            )
        } catch (e: StatusRuntimeException) {
            responseObserver.onError(e)
        }

    }

    override fun deletaChavePix(request: IdPixRequest, responseObserver: StreamObserver<Empty>) {

        try {
            deleta(request.toModel())

            responseObserver.onNext(Empty.newBuilder().build())
            responseObserver.onCompleted()

        } catch (e: ConstraintViolationException) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Dados invalidos")
                    .asRuntimeException()
            )
        } catch (e: StatusRuntimeException) {
            responseObserver.onError(e)
            return
        }

    }

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix): Long {

        if (chavePixRepository.existsByChave(novaChavePix.chave)) {
            throw StatusRuntimeException(
                Status.ALREADY_EXISTS
                    .withDescription("Chave Pix já cadastrada")
            )
        }

        val contaResponse = contaClient
            .buscaContaPorIdETipo(novaChavePix.identificadorCliente, novaChavePix.tipoConta)

        if (contaResponse.body() == null) {
            throw StatusRuntimeException(
                Status.NOT_FOUND
                    .withDescription("Cliente não encontrado no Itau")
            )
        }

        val conta = contaResponse.body()?.toModel()

        val chavePix = novaChavePix.toModel(conta as Conta)

        chavePixRepository.save(chavePix)

        val createPixKeyRequest = chavePix.toRequest()

        val cadastraChavePixResponse = bcbClient.cadastraChavePix(createPixKeyRequest)
        if (cadastraChavePixResponse.status != HttpStatus.CREATED)
            throw StatusRuntimeException(
                Status.UNKNOWN
                    .withDescription("Erro ao cadastar no BCB")
            )

        chavePix.utualizaChave(cadastraChavePixResponse.body()!!.key)

        return chavePix.pixId!!

    }

    @Transactional
    fun deleta(@Valid idPix: IdPix) {

        val possivelChave = chavePixRepository.findById(idPix.pixId)
        if (possivelChave.isEmpty) {
            throw StatusRuntimeException(
                Status.NOT_FOUND
                    .withDescription("Chave Pix não encontrada")
            )
        }

        val chave = possivelChave.get()

        val contaResponse = contaClient
            .buscaContaPorIdETipo(idPix.identificador, chave.tipoConta)

        if (contaResponse.body() == null) {
            throw StatusRuntimeException(
                Status.NOT_FOUND
                    .withDescription("Cliente não encontrado no Itau")
            )
        }

        if (idPix.identificador != chave.identificadorCliente) {
            throw StatusRuntimeException(
                Status.PERMISSION_DENIED
                    .withDescription("Chave não pertencente a este cliente")
            )
        }

        val response = bcbClient.deletaChavePix(chave.toDeletePixKeyRequest(), chave.chave)
        if (response.status != HttpStatus.OK)
            throw StatusRuntimeException(
                Status.UNKNOWN
                    .withDescription("Erro ao deletar no BCB")
            )

        chavePixRepository.deleteById(idPix.pixId)

    }
}