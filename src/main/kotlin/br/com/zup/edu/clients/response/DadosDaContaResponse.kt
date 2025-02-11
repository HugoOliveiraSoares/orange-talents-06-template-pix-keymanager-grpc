package br.com.zup.edu.chavepix

import br.com.zup.edu.TipoConta
import br.com.zup.edu.model.Conta
import br.com.zup.edu.model.Instituicao
import br.com.zup.edu.model.Titular

data class DadosDaContaResponse(
    val tipo: TipoConta,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {

    fun toModel(): Conta {

        return Conta(
            tipo,
            instituicao.let {
                Instituicao(it.nome, it.ispb)
            },
            agencia,
            numero,
            titular.let {
                Titular(it.id, it.nome, it.cpf)
            }
        )
    }

}

data class TitularResponse(
    val id: String,
    val nome: String,
    val cpf: String
) {}

data class InstituicaoResponse(
    val nome: String,
    val ispb: String
) {}


