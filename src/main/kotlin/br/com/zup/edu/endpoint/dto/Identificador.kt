package br.com.zup.edu.endpoint.dto

import br.com.zup.edu.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@Introspected
class Identificador(

    @field:NotEmpty
    @field:NotBlank
    @ValidUUID
    val identificador: String
) {

}
