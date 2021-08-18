package br.com.zup.edu.novachavepix

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.KeyManagerGrpcServiceGrpc
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class PixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub
) {

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve adicionar nova chave pix`() {

        val response = grpcClient.registraChavePix(
            ChavePixRequest.newBuilder()
                .setIdentificador("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.CPF)
                .setChave("70306444682")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        with(response) {
            println("Id do Pix: $pixId")
            assertNotNull(pixId)
            assertTrue(repository.existsById(pixId))
        }

    }

    @Test
    fun `nao deve cadastrar uma nova chave pix ja existente`() {

        val chaveExistente = repository.save(
            ChavePix(
                UUID.randomUUID().toString(),
                TipoChave.CPF,
                "12419962028",
                TipoConta.CONTA_CORRENTE,
                Conta(
                    "",
                    Instituicao("", ""),
                    "agencia",
                    "numero",
                    Titular("", "", "")
                )
            )
        )

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdentificador(chaveExistente.identificadorCliente)
                    .setTipoChave(chaveExistente.tipoChave)
                    .setChave(chaveExistente.chave)
                    .setTipoConta(chaveExistente.tipoConta)
                    .build()
            )
        }

        assertEquals(Status.ALREADY_EXISTS.code, exception.status.code)

    }

    @Test
    fun `nao deve cadastrar uma nova chave pix com dados invalidos`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setTipoChave(TipoChave.CPF)
                    .setChave(" ")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)

    }

    @Test
    fun `nao deve cadastrar uma nova chave pix com dados invalidos sem chave`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdentificador("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setChave(" ")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
    }

    @Test
    fun `deve cadastrar uma chave aleatoria`() {

        val response = grpcClient.registraChavePix(
            ChavePixRequest.newBuilder()
                .setIdentificador("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.CHAVE_ALEATORIA)
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        with(response) {
            println("Id do Pix: $pixId")
            assertNotNull(pixId)
            assertTrue(repository.existsById(pixId))
        }

    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub? {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }

    }

}