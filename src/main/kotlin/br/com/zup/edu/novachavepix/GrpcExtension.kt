package br.com.zup.edu.novachavepix

import br.com.zup.edu.ChavePixRequest
import java.util.*

fun ChavePixRequest.toModel(): ChavePix {

    return ChavePix(

        identificador,
        tipoChave,
        // Se for selecionado chave aleatoria, o request.chave deve ser vazio
        chave.let {
            if (tipoChave.equals(ChavePixRequest.TipoChave.CHAVE_ALEATORIA)) {
                if (chave.isEmpty() || chave.isBlank())
                    UUID.randomUUID().toString()
                else ""
            } else chave
        },
        tipoConta

    )

}