package br.com.zup.edu.endpoint.dto

import br.com.zup.edu.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
class IdPix(
    @field:NotNull
    @field:Positive
    val pixId: Long,

    @ValidUUID
    @field:NotEmpty
    @field:NotBlank
    val identificador: String
) {

}
