package br.com.zup.edu.extension

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.IdPixRequest
import br.com.zup.edu.PixRequest
import br.com.zup.edu.endpoint.dto.IdPix
import br.com.zup.edu.endpoint.dto.NovaChavePix
import br.com.zup.edu.endpoint.dto.Chave

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

fun PixRequest.toModel(): Chave {

    return Chave(chave)
}