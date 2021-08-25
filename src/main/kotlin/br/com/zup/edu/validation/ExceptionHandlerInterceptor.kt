package br.com.zup.edu.validation

import io.grpc.BindableService
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class ExceptionHandlerInterceptor : MethodInterceptor<BindableService, Any?> {

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {

        try {
            return context.proceed()
        } catch (e: Exception) {

            val statusError = when(e) {
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException()
                is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message).asRuntimeException()
                is ChaveJaExistente -> Status.ALREADY_EXISTS.withDescription(e.message).asRuntimeException()
                is ObjetoNaoEncontrado -> Status.NOT_FOUND.withDescription(e.message).asRuntimeException()
                is PermicaoNegada -> Status.PERMISSION_DENIED.withDescription(e.message).asRuntimeException()
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                    .withDescription("Dados invalidos ${e.constraintViolations}").asRuntimeException()
                else -> Status.UNKNOWN.withDescription("erro inesperado").asRuntimeException()
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusError)
            return null
        }


    }


}

class ChaveJaExistente(message: String) : RuntimeException(message) {}
class ObjetoNaoEncontrado(message: String) : RuntimeException(message) {}
class PermicaoNegada(message: String) : RuntimeException(message) {}
