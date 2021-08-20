package br.com.zup.edu.endpoint.dto

import br.com.zup.edu.clients.request.DeletePixKeyRequest
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
