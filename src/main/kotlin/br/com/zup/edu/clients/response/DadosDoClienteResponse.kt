package br.com.zup.edu.clients.response

import br.com.zup.edu.chavepix.InstituicaoResponse

data class DadosDoClienteResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstituicaoResponse
) {

}
