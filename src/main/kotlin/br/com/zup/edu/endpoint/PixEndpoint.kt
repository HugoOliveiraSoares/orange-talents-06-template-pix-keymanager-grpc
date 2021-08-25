package br.com.zup.edu.endpoint

import br.com.zup.edu.*
import br.com.zup.edu.clients.BCBClient
import br.com.zup.edu.clients.ContaClient
import br.com.zup.edu.endpoint.dto.Chave
import br.com.zup.edu.endpoint.dto.IdPix
import br.com.zup.edu.endpoint.dto.Identificador
import br.com.zup.edu.endpoint.dto.NovaChavePix
import br.com.zup.edu.extension.toModel
import br.com.zup.edu.repository.ChavePixRepository
import br.com.zup.edu.validation.ChaveJaExistente
import br.com.zup.edu.validation.ErrorHandler
import br.com.zup.edu.validation.ObjetoNaoEncontrado
import br.com.zup.edu.validation.PermicaoNegada
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class PixEndpoint(
    @Inject val contaClient: ContaClient,
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val bcbClient: BCBClient
) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    @ErrorHandler
    override fun registraChavePix(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {

        val idPix = registra(request.toModel())

        responseObserver.onNext(ChavePixResponse.newBuilder().setPixId(idPix).build())

        responseObserver.onCompleted()

    }

    @ErrorHandler
    override fun deletaChavePix(request: IdPixRequest, responseObserver: StreamObserver<Empty>) {

        deleta(request.toModel())

        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
    }

    @ErrorHandler
    override fun consultaChavePixKeyManager(
        request: IdPixRequest, responseObserver: StreamObserver<ChavePixDetailResponse>
    ) {
        val consultaDados = consultaDadosKeyManager(request.toModel())

        responseObserver.onNext(consultaDados)
        responseObserver.onCompleted()
    }

    @ErrorHandler
    override fun consultaChavePix(request: PixRequest, responseObserver: StreamObserver<ChavePixDetailResponse>) {

        val consultaDados = consultaDados(request.toModel())

        responseObserver.onNext(consultaDados)
        responseObserver.onCompleted()

    }

    @ErrorHandler
    override fun listaTodasChaves(request: ClienteRequest, responseObserver: StreamObserver<ChavesResponse>) {

        val lista = listaChaves(request.toModel())

        responseObserver.onNext(lista)
        responseObserver.onCompleted()
    }

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix): Long {

        if (chavePixRepository.existsByChave(novaChavePix.chave))
            throw ChaveJaExistente("Chave Pix já cadastrada")

        val contaResponse = contaClient
            .buscaContaPorIdETipo(novaChavePix.identificadorCliente, novaChavePix.tipoConta)

        val conta = contaResponse.body()?.toModel() ?: throw ObjetoNaoEncontrado("Cliente não encontrado no Itau")

        val chavePix = novaChavePix.toModel(conta)

        chavePixRepository.save(chavePix)

        val createPixKeyRequest = chavePix.toRequest()

        val cadastraChavePixResponse = bcbClient.cadastraChavePix(createPixKeyRequest)
        if (cadastraChavePixResponse.status != HttpStatus.CREATED)
            throw IllegalStateException("Erro ao cadastar no BCB")

        chavePix.utualizaChave(cadastraChavePixResponse.body()!!.key)

        return chavePix.pixId!!

    }

    @Transactional
    fun deleta(@Valid idPix: IdPix) {

        val possivelChave = chavePixRepository.findById(idPix.pixId)
        if (possivelChave.isEmpty)
            throw ObjetoNaoEncontrado("Chave Pix não encontrada")

        val chave = possivelChave.get()

        val contaResponse = contaClient
            .buscaContaPorIdETipo(idPix.identificador, chave.tipoConta)

        if (contaResponse.body() == null)
            throw ObjetoNaoEncontrado("Cliente não encontrado no Itau")

        if (idPix.identificador != chave.identificadorCliente)
            throw PermicaoNegada("Chave não pertencente a este cliente")

        val response = bcbClient.deletaChavePix(chave.toDeletePixKeyRequest(), chave.chave)
        if (response.status != HttpStatus.OK)
            throw IllegalStateException("Erro ao deletar no BCB")

        chavePixRepository.deleteById(idPix.pixId)

    }

    fun listaChaves(@Valid identificador: Identificador): ChavesResponse {

        try {
            contaClient.buscaCliente(identificador.identificador)
        } catch (e: HttpClientResponseException) {
            throw ObjetoNaoEncontrado("Cliente não encontrado no sistema do Itau")
        }

        val possiveisChaves = chavePixRepository.findByIdentificadorCliente(identificador.identificador)
        if (possiveisChaves.isEmpty())
            return ChavesResponse.newBuilder().build()

        val chaves = mutableListOf<ChaveResponse>()
        possiveisChaves.forEach {
            chaves.add(it.toChaveResponse())
        }

        return ChavesResponse.newBuilder()
            .addAllChaveResponse(chaves)
            .build()
    }

    fun consultaDadosKeyManager(@Valid idPix: IdPix): ChavePixDetailResponse {

        val possivelChavePix = chavePixRepository.findById(idPix.pixId)
        if (possivelChavePix.isEmpty)
            throw ObjetoNaoEncontrado("Chave não encontrada")

        val chavePix = possivelChavePix.get()
        if (chavePix.identificadorCliente != idPix.identificador)
            throw PermicaoNegada("Chave não pertencente a este cliente")

        val httpResponse = bcbClient.buscaChavePix(chavePix.chave)
        if (httpResponse.status != HttpStatus.OK)
            throw ObjetoNaoEncontrado("Chave não encontrada no sistema BCB")

        return chavePix.toResponse(httpResponse.body()?.createdAt)

    }

    fun consultaDados(@Valid chave: Chave): ChavePixDetailResponse {

        val possivelChavePix = chavePixRepository.findByChave(chave.chave)
        // Se não for encontrado na nossa base de dados pesquisa no sistema Pix do BCB
        if (possivelChavePix.isEmpty) {
            return bcbClient.buscaChavePix(chave.chave).body()?.toChavePixDetailResponse()
                ?: throw IllegalStateException("Erro ao buscar no sistema Pix do BCB")
        }

        val chavePix = possivelChavePix.get()

        val httpResponse = bcbClient.buscaChavePix(chavePix.chave)
        if (httpResponse.status != HttpStatus.OK)
            throw ObjetoNaoEncontrado("Chave não encontrada no sistema BCB")

        return chavePix.toResponse(httpResponse.body()?.createdAt)

    }


}