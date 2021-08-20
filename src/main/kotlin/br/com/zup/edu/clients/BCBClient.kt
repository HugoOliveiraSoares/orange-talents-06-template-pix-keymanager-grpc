package br.com.zup.edu.clients

import br.com.zup.edu.clients.request.CreatePixKeyRequest
import br.com.zup.edu.clients.request.DeletePixKeyRequest
import br.com.zup.edu.clients.response.CreatePixKeyResponse
import br.com.zup.edu.clients.response.DeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.keys.url}")
interface BCBClient {

    @Post(produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun cadastraChavePix(@Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/{key}",produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun deletaChavePix(@Body deletePixKeyRequest: DeletePixKeyRequest, @QueryValue key: String): HttpResponse<DeletePixKeyResponse>

}