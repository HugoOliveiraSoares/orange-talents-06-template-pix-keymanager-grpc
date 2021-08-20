package br.com.zup.edu.services

import br.com.zup.edu.*
import br.com.zup.edu.chavepix.DadosDaContaResponse
import br.com.zup.edu.chavepix.InstituicaoResponse
import br.com.zup.edu.chavepix.TitularResponse
import br.com.zup.edu.clients.BCBClient
import br.com.zup.edu.clients.ContaClient
import br.com.zup.edu.clients.request.BankAccountRequest
import br.com.zup.edu.clients.request.CreatePixKeyRequest
import br.com.zup.edu.clients.request.OwnerRequest
import br.com.zup.edu.clients.response.BankAccountResponse
import br.com.zup.edu.clients.response.CreatePixKeyResponse
import br.com.zup.edu.clients.response.OwnerResponse
import br.com.zup.edu.enums.AccountType
import br.com.zup.edu.enums.KeyType
import br.com.zup.edu.enums.TypePerson
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
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastraChaveTest(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
) {

    @Inject
    lateinit var contaClient: ContaClient
    @Inject
    lateinit var bcbClient: BCBClient

    lateinit var dadosDaContaResponse: DadosDaContaResponse
    lateinit var createPixKeyRequest: CreatePixKeyRequest
    lateinit var createPixKeyResponse: CreatePixKeyResponse

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

        createPixKeyRequest = CreatePixKeyRequest(
            KeyType.CPF,
            "02467781054",
            BankAccountRequest(
                "60701190",
                "0001",
                "291900",
                AccountType.CACC
            ),
            OwnerRequest(
                TypePerson.NATURAL_PERSON,
                "Rafael M C Ponte",
                "02467781054"
            )
        )

        createPixKeyResponse = CreatePixKeyResponse(
            KeyType.CPF,
            "02467781054",
            BankAccountResponse(
                "60701190",
                "0001",
                "291900",
                AccountType.CACC
            ),
            OwnerResponse(
                TypePerson.NATURAL_PERSON,
                "Rafael M C Ponte",
                "02467781054"
            ),
            LocalDateTime.now()
        )

        Mockito
            .`when`(contaClient.buscaContaPorIdETipo("c56dfef4-7901-44fb-84e2-a2cefb157890", TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse))

        Mockito
            .`when`(bcbClient.cadastraChavePix(createPixKeyRequest))
            .thenReturn(HttpResponse.created(createPixKeyResponse))

    }

    /*** Registrando uma nova chave Pix ***/

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
                .setChave("")
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

    @Test
    fun `nao deve cadastar uma nova chave pix com cliente inexistente`() {

        val identificador = UUID.randomUUID().toString()

        Mockito
            .`when`(contaClient.buscaContaPorIdETipo(identificador, TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdentificador(identificador)
                    .setTipoChave(TipoChave.CPF)
                    .setChave("02467781054")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        assertEquals("NOT_FOUND: Cliente não encontrado no Itau", exception.message)

    }

    @Test
    fun `deve retornar UNKNOWN quando der erro no sistema BCB e nao deve salvar no banco` () {

        val createPix = CreatePixKeyRequest(
            KeyType.CPF,
            "02467781054",
            BankAccountRequest(
                "60701190",
                "0001",
                "791903",
                AccountType.CACC
            ),
            OwnerRequest(
                TypePerson.NATURAL_PERSON,
                "Hugo",
                "84859958055"
            )
        )

        Mockito
            .`when`(bcbClient.cadastraChavePix(createPix))
            .thenReturn(HttpResponse.badRequest())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdentificador("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setChave("02467781054")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        assertEquals(Status.UNKNOWN.code, exception.status.code)
        assertEquals("UNKNOWN: Erro ao cadastar no BCB", exception.message)
        assertFalse(repository.existsByChave("02467781054"))

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