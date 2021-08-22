package br.com.zup.edu.endpoint.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@Introspected
class Identificador(

    @field:NotEmpty
    @field:NotBlank
    val identificador: String
) {

}
