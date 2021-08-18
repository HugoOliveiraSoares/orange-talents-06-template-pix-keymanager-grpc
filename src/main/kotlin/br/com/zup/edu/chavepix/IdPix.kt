package br.com.zup.edu.chavepix

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Positive

@Introspected
class IdPix(
    @field:Positive
    val pixId: Long,
    @field:NotBlank @field:NotEmpty
    val identificador: String
) {

}
