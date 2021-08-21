package br.com.zup.edu.endpoint.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

@Introspected
class Chave(

    @field:Size(max = 77)
    @field:NotBlank
    @field:NotEmpty
    val chave: String

) {
}
