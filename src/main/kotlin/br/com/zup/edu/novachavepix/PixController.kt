package br.com.zup.edu.novachavepix

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.ChavePixResponse
import br.com.zup.edu.KeyManagerGrpcServiceGrpc
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import io.grpc.Status
import java.util.*
import javax.persistence.PersistenceException
import javax.validation.ConstraintViolationException

@Singleton
class PixController(@Inject val chavePixRepository: ChavePixRepository) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    override fun registraChavePix(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {

        if (chavePixRepository.existsByChave(request.chave)) {

            responseObserver.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Chave Pix já cadastrada")
                    .asRuntimeException()
            )

            return
        }

        // Se for selecionado chave aleatoria, o request.chave deve ser vazio
        val chave = if (request.tipoChave.equals(ChavePixRequest.TipoChave.CHAVE_ALEATORIA)) {
            if (request.chave.isEmpty() || request.chave.isBlank())
                UUID.randomUUID().toString()
            else ""

        } else request.chave


        val chavePix = ChavePix(
            request.identificador,
            request.tipoChave,
            chave,
            request.tipoConta
        )

        try {
            chavePixRepository.save(chavePix)
        } catch (e: ConstraintViolationException) {

            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Dados de entrada inválidos")
                    .asRuntimeException()
            )

            return

        }

        responseObserver.onNext(
            ChavePixResponse.newBuilder()
                .setPixId(chavePix.pixId!!)
                .build()
        )

        responseObserver.onCompleted()

    }

}