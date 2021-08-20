package br.com.zup.edu.extension

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.IdPixRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.clients.BCBClient
import br.com.zup.edu.clients.request.BankAccountRequest
import br.com.zup.edu.clients.request.CreatePixKeyRequest
import br.com.zup.edu.endpoint.dto.IdPix
import br.com.zup.edu.endpoint.dto.NovaChavePix

fun ChavePixRequest.toModel(): NovaChavePix {

    return NovaChavePix(
        identificador,
        tipoChave,
        chave, // Se for selecionado chave aleatoria, o request.chave deve ser vazio
        tipoConta
    )
}

fun IdPixRequest.toModel(): IdPix {

    return IdPix(
        pixId,
        identificador
    )

}