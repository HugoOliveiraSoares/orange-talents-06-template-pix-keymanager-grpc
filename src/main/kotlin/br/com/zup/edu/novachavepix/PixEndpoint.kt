package br.com.zup.edu.novachavepix

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.ChavePixResponse
import br.com.zup.edu.KeyManagerGrpcServiceGrpc
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class PixEndpoint(@Inject val chavePixService: ChavePixService) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    override fun registraChavePix(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {

        try {

            val novaChavePix = request.toModel()
            val pixId = chavePixService.registra(novaChavePix)

            responseObserver.onNext(
                ChavePixResponse.newBuilder()
                    .setPixId(pixId)
                    .build()
            )

            responseObserver.onCompleted()

        } catch (e: ConstraintViolationException){
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Dados invalidos")
                    .asRuntimeException()
            )
        }
        catch (e: StatusRuntimeException) {
            responseObserver.onError(e)
            return
        }

    }

}