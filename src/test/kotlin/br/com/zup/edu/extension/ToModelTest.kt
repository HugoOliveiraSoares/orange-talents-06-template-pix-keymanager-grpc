package br.com.zup.edu.extension

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.IdPixRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chavepix.DadosDaContaResponse
import br.com.zup.edu.chavepix.InstituicaoResponse
import br.com.zup.edu.chavepix.TitularResponse
import br.com.zup.edu.clients.request.BankAccountRequest
import br.com.zup.edu.clients.request.CreatePixKeyRequest
import br.com.zup.edu.endpoint.dto.IdPix
import br.com.zup.edu.endpoint.dto.NovaChavePix
import br.com.zup.edu.model.Conta
import br.com.zup.edu.model.Instituicao
import br.com.zup.edu.model.Titular
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ToModelTest{

    @Test
    fun `deve retornar uma NovaChavePix`() {

        val chavePixRequest = ChavePixRequest.newBuilder()
            .setIdentificador("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setChave("02467781054")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val toModel = chavePixRequest.toModel()

        assertEquals(NovaChavePix::class.java.name, toModel::class.java.name)
        assertEquals(chavePixRequest.identificador, toModel.identificadorCliente)

    }

    @Test
    fun `deve retornar um IdPix`() {

        val idPixRequest = IdPixRequest.newBuilder()
            .setPixId(1)
            .setIdentificador("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .build()

        val toModel = idPixRequest.toModel()

        assertEquals(IdPix::class.java.name, toModel::class.java.name)
        assertEquals(idPixRequest.pixId, toModel.pixId)

    }

    @Test
    fun `deve retornar uma Conta` () {

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

        val toModel = dadosDaContaResponse.toModel()

        assertEquals(Conta::class.java.name, toModel::class.java.name)
        assertEquals(dadosDaContaResponse.agencia, toModel.agencia)

    }

    @Test
    fun `deve retornar um BankAccountRequest` () {

        val conta = Conta(
            TipoConta.CONTA_CORRENTE,
            Instituicao(
                "Itau",
                "60701190"
            ),
            "0001",
            "123456",
            Titular(
                "1",
                "Hugo",
                "02467781054"
            )
        )

        val toRequest = conta.toRequest()
        assertEquals(BankAccountRequest::class.java.name, toRequest::class.java.name)
        assertEquals(conta.tipo.ordinal, toRequest.accountType.ordinal)


    }
}