package br.com.zup.edu.endpoint

import br.com.zup.edu.ClienteRequest
import br.com.zup.edu.KeyManagerGrpcServiceGrpc
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chavepix.InstituicaoResponse
import br.com.zup.edu.clients.BCBClient
import br.com.zup.edu.clients.ContaClient
import br.com.zup.edu.clients.response.DadosDoClienteResponse
import br.com.zup.edu.model.ChavePix
import br.com.zup.edu.model.Conta
import br.com.zup.edu.model.Instituicao
import br.com.zup.edu.model.Titular
import br.com.zup.edu.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListaTodasChavesClienteTest(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    @Inject val contaClient: ContaClient
) {

    val chavesExistentes: MutableList<ChavePix> = mutableListOf()
    lateinit var dadosDaContaResponse: DadosDoClienteResponse

    @BeforeEach
    internal fun setUp() {

        chavesExistentes.addAll(
            listOf(
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
                ),
                ChavePix(
                    "c56dfef4-7901-44fb-84e2-a2cefb157890",
                    TipoChave.CHAVE_ALEATORIA,
                    UUID.randomUUID().toString(),
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
        )
        repository.saveAll(chavesExistentes)

        dadosDaContaResponse = DadosDoClienteResponse(
            "c56dfef4-7901-44fb-84e2-a2cefb157890",
            "Rafael M C Ponte",
            "02467781054",
            InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190")
        )

    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `deve retornar uma lista de chaves de um cliente`() {

        Mockito
            .`when`(contaClient.buscaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse))

        val chaves = grpcClient.listaTodasChaves(clienteRequest("c56dfef4-7901-44fb-84e2-a2cefb157890"))

        assertNotNull(chaves)
        assertTrue(chaves.chaveResponseList.size == 2)

    }

    @Test
    fun `deve retornar um lista vazia`() {

        Mockito
            .`when`(contaClient.buscaCliente("2ac09233-21b2-4276-84fb-d83dbd9f8bab"))
            .thenReturn(HttpResponse.ok())

        val chaves = grpcClient.listaTodasChaves(clienteRequest("2ac09233-21b2-4276-84fb-d83dbd9f8bab"))

        assertNotNull(chaves)
        assertTrue(chaves.chaveResponseList.isEmpty())

    }

    @Test
    fun `deve retornar NOT_FOUD para um usuario nao encontrado no sistema Itau`() {
        Mockito
            .`when`(contaClient.buscaCliente("2ac09233-21b2-4276-84fb-d83dbd9f8bab"))
            .thenThrow(HttpClientResponseException("", HttpResponse.notFound("")))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.listaTodasChaves(clienteRequest("2ac09233-21b2-4276-84fb-d83dbd9f8bab"))
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("NOT_FOUND: Cliente não encontrado no sistema do Itau", exception.message)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT para dados invalidos` () {

        val assertThrows = assertThrows<StatusRuntimeException> {
            grpcClient.listaTodasChaves(clienteRequest(""))
        }

        assertEquals(Status.INVALID_ARGUMENT.code, assertThrows.status.code)

    }

    fun clienteRequest(identificador: String): ClienteRequest {

        return ClienteRequest.newBuilder()
            .setIdentificador(identificador)
            .build()

    }

    @MockBean(ContaClient::class)
    fun contaMock(): ContaClient {
        return Mockito.mock(ContaClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub? {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }

    }

}