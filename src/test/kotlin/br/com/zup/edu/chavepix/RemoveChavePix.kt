package br.com.zup.edu.chavepix

import br.com.zup.edu.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@MicronautTest(transactional = false)
internal class RemoveChavePix(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub
) {
    @field:Inject
    lateinit var contaClient: ContaClient
    lateinit var dadosDaContaResponse: DadosDaContaResponse
    lateinit var chaveExistente: ChavePix

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

        chaveExistente = repository.save(
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

        Mockito
            .`when`(contaClient.buscaContaPorIdETipo("c56dfef4-7901-44fb-84e2-a2cefb157890", TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse))

    }

    @Test
    fun `deve deletar uma chave pix`() {

        // faz a requisição para deletar a chave
        val respose = grpcClient.deletaChavePix(
            IdPixRequest.newBuilder()
                .setPixId(chaveExistente.pixId!!)
                .setIdentificador(chaveExistente.identificadorCliente)
                .build()
        )

        // Validação
        Assertions.assertTrue(respose.equals(Empty.newBuilder().build()))
        Assertions.assertFalse(repository.existsById(chaveExistente.pixId!!))


    }

    @Test
    fun `deve retornar NOT_FOUND para uma chave inexistente`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(
                IdPixRequest.newBuilder()
                    .setPixId(9999)
                    .setIdentificador("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .build()
            )
        }

        Assertions.assertEquals(Status.NOT_FOUND.code, exception.status.code)
        Assertions.assertEquals("NOT_FOUND: Chave Pix não encontrada", exception.message)

    }

    @Test
    fun `deve retornar NOT_FOUND para um cliente inexistente no Itau`(){

        // cenário

        val identificador = UUID.randomUUID().toString()

        Mockito
            .`when`(contaClient.buscaContaPorIdETipo(identificador, TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.notFound())

        // ação
        val idPixRequest = IdPixRequest.newBuilder()
            .setPixId(chaveExistente.pixId!!)
            .setIdentificador(identificador)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(idPixRequest)
        }

        // validação
        Assertions.assertEquals(Status.NOT_FOUND.code, exception.status.code)
        Assertions.assertTrue(repository.existsById(chaveExistente.pixId!!)) // verifica se a chave ainda existe no banco
        Assertions.assertEquals("NOT_FOUND: Cliente não encontrado no Itau", exception.message)

    }

    @Test
    fun `nao deleta uma chave por dados invalidos`(){

        // ação
        val idPixRequest = IdPixRequest.newBuilder()
            .setIdentificador("")
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(idPixRequest)
        }

        // validação
        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)

    }

    @Test
    fun `nao deleta uma chave por nao pertencer ao cliente`() {
        // cenário
        // Salva uma chave no banco
        val identificador = "5260263c-a3c1-4727-ae32-3bdb2538841b"
        val dadosContaResponse = DadosDaContaResponse(
            TipoConta.CONTA_POUPANCA,
            InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
            "0001",
            "291900",
            TitularResponse(
                identificador,
                "Yuri Matheus",
                "86135457004"
            )
        )

        Mockito
            .`when`(contaClient.buscaContaPorIdETipo(identificador, TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(dadosContaResponse))

        // ação
        val idPixRequest = IdPixRequest.newBuilder()
            .setPixId(chaveExistente.pixId!!)
            .setIdentificador(identificador)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(idPixRequest)
        }

        // validação
        Assertions.assertEquals(Status.PERMISSION_DENIED.code, exception.status.code)
        Assertions.assertTrue(repository.existsById(chaveExistente.pixId!!)) // verifica se a chave ainda existe no banco

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