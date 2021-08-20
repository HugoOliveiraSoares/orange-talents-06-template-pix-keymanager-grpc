package br.com.zup.edu.clients.response

import java.time.LocalDateTime

class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
) {

}
