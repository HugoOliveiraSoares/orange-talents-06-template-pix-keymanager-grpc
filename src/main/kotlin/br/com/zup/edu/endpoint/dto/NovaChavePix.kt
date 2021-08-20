package br.com.zup.edu.endpoint.dto

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.validation.Pix
import br.com.zup.edu.model.ChavePix
import br.com.zup.edu.model.Conta
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Pix
@Introspected
class NovaChavePix(

    @field:NotEmpty @field:NotBlank
    val identificadorCliente: String,

    @field:NotNull
    val tipoChave: TipoChave,

    @field:Size(max = 77)
    var chave: String,

    @field:NotNull
    val tipoConta: TipoConta

) {
    fun toModel(conta: Conta): ChavePix {

        return ChavePix(
            identificadorCliente,
            tipoChave,
            chave,
            tipoConta,
            conta
        )

    }

}