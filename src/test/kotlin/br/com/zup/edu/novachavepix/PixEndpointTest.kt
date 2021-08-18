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
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class PixEndpointTest(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub
) {

    @field:Inject
    lateinit var contaClient: ContaClient
    lateinit var dadosDaContaResponse: DadosDaContaResponse

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
        dadosDaContaResponse = DadosDaContaResponse(
            TipoConta.CONTA_CORRENTE,
            InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
            "0001",
            "291900",
            TitularResponse(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "Rafael M C Ponte",
                "02467781054"
            )
        )

        Mockito
            .`when`(contaClient.buscaContaPorIdETipo("c56dfef4-7901-44fb-84e2-a2cefb157890", TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse))

    }

    @Test
    fun `deve adicionar nova chave pix`() {

        val chavePixRequest = ChavePixRequest.newBuilder()
            .setIdentificador("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setChave("02467781054")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val response = grpcClient.registraChavePix(chavePixRequest)

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
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                TipoChave.CPF,
                "02467781054",
                TipoConta.CONTA_CORRENTE,
                Conta(
                    TipoConta.CONTA_CORRENTE,
                    Instituicao(
                        "ITAÚ UNIBANCO S.A.",
                        "60701190"
                    ),
                    "0001",
                    "291900",
                    Titular(
                        "c56dfef4-7901-44fb-84e2-a2cefb157890",
                        "Rafael M C Ponte",
                        "02467781054"
                    )
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

    @Test
    fun `nao deve cadastrar uma nova chave pix aleatoria`() {
        // O tipo de chave deveria ser CHAVE_ALEATORIA
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdentificador("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub? {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }

    }

    @MockBean(ContaClient::class)
    fun contaMock(): ContaClient {
        return Mockito.mock(ContaClient::class.java)
    }

}