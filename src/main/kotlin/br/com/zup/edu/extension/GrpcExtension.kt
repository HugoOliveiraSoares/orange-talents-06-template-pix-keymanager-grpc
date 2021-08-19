package br.com.zup.edu.chavepix

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.IdPixRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.endpoint.dto.IdPix
import br.com.zup.edu.endpoint.dto.NovaChavePix
import java.util.*

fun ChavePixRequest.toModel(): NovaChavePix {

    return NovaChavePix(

        identificador,
        tipoChave,
        // Se for selecionado chave aleatoria, o request.chave deve ser vazio
        chave.let {
            if (tipoChave.equals(TipoChave.CHAVE_ALEATORIA)) {
                if (chave.isEmpty() || chave.isBlank())
                    UUID.randomUUID().toString()
                else ""
            } else chave
        },
        tipoConta

    )

}

fun IdPixRequest.toModel(): IdPix {

    return IdPix(
        pixId,
        identificador
    )

}