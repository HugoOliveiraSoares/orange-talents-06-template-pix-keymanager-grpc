package br.com.zup.edu.endpoint

import br.com.zup.edu.*
import br.com.zup.edu.chavepix.DadosDaContaResponse
import br.com.zup.edu.chavepix.InstituicaoResponse
import br.com.zup.edu.chavepix.TitularResponse
import br.com.zup.edu.clients.BCBClient
import br.com.zup.edu.clients.ContaClient
import br.com.zup.edu.clients.response.BankAccountResponse
import br.com.zup.edu.clients.response.OwnerResponse
import br.com.zup.edu.clients.response.PixKeyDetailsResponse
import br.com.zup.edu.enums.AccountType
import br.com.zup.edu.enums.KeyType
import br.com.zup.edu.enums.TypePerson
import br.com.zup.edu.model.ChavePix
import br.com.zup.edu.model.Conta
import br.com.zup.edu.model.Instituicao
import br.com.zup.edu.model.Titular
import br.com.zup.edu.repository.ChavePixRepository
import com.github.javafaker.Faker
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
internal class ConsultaDadosChavePixTest(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    @Inject val contaClient: ContaClient,
    @Inject val bcbClient: BCBClient
) {

    lateinit var chaveExistente: ChavePix

    @BeforeEach
    internal fun setUp() {

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

        val dadosDaContaResponse = DadosDaContaResponse(
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
            .`when`(contaClient.buscaContaPorIdETipo(chaveExistente.identificadorCliente, chaveExistente.tipoConta))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse))


    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `deve retornar uma chave pix da nossa base de dados`() {

        Mockito
            .`when`(bcbClient.buscaChavePix(chaveExistente.chave))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse(chaveExistente.chave)))

        val consultaChavePix = grpcClient.consultaChavePix(pixRequest(chaveExistente.chave))

        assertNotNull(consultaChavePix)
        assertEquals(chaveExistente.chave,consultaChavePix.chave)
        assertEquals(ChavePixDetailResponse::class.java, consultaChavePix::class.java)

    }

    @Test
    fun `deve retornar uma chave pix da base do sistema pix BCB`() {

        val chave = geraChave()

        Mockito
            .`when`(bcbClient.buscaChavePix(chave))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse(chave)))

        val consultaChavePix = grpcClient.consultaChavePix(pixRequest(chave))

        assertNotNull(consultaChavePix)
        assertEquals(chave,consultaChavePix.chave)
        assertEquals(ChavePixDetailResponse::class.java, consultaChavePix::class.java)

    }

    @Test
    fun `deve retornar NOT_FOUND para chave nao encontrada no sistema BCB`() {

        val chave = chaveExistente.chave

        Mockito
            .`when`(bcbClient.buscaChavePix(chave))
            .thenReturn(HttpResponse.notFound())

        val assertThrows = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChavePix(pixRequest(chave))
        }

        assertEquals(Status.NOT_FOUND.code, assertThrows.status.code)
        assertEquals("NOT_FOUND: Chave não encontrada no sistema BCB", assertThrows.message)

    }

    @Test
    fun `deve retornar FAILED_PRECONDITION para erro ao buscar chave no sistema BCB`() {

        val chave = geraChave()

        Mockito
            .`when`(bcbClient.buscaChavePix(chave))
            .thenReturn(HttpResponse.badRequest())

        val assertThrows = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChavePix(pixRequest(chave))
        }

        assertEquals(Status.FAILED_PRECONDITION.code, assertThrows.status.code)
        assertEquals("FAILED_PRECONDITION: Erro ao buscar no sistema Pix do BCB", assertThrows.message)

    }

    @Test
    fun `deve retornar INVALID_ARGUMENT para dados invalidos`() {

        val chave = ""

        Mockito
            .`when`(bcbClient.buscaChavePix(chave))
            .thenReturn(HttpResponse.badRequest())

        val assertThrows = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChavePix(pixRequest(chave))
        }

        assertEquals(Status.INVALID_ARGUMENT.code, assertThrows.status.code)

    }

    fun geraChave(): String {

        return Faker(Locale("pt-BR")).phoneNumber().cellPhone()

    }

    fun pixRequest(chave: String) : PixRequest {
        return PixRequest.newBuilder()
            .setChave(chave)
            .build()
    }

    fun pixKeyDetailsResponse(chave: String): PixKeyDetailsResponse {

        return PixKeyDetailsResponse(
            KeyType.PHONE,
            chave,
            BankAccountResponse("","","",AccountType.CACC),
            OwnerResponse(TypePerson.NATURAL_PERSON, "",""),
            LocalDateTime.now()
        )

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

    @MockBean(BCBClient::class)
    fun bcbMock(): BCBClient? {
        return Mockito.mock(BCBClient::class.java)
    }

}