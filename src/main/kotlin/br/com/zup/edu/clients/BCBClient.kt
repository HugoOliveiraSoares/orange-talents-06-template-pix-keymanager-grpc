package br.com.zup.edu.clients

import br.com.zup.edu.clients.request.CreatePixKeyRequest
import br.com.zup.edu.clients.request.DeletePixKeyRequest
import br.com.zup.edu.clients.response.CreatePixKeyResponse
import br.com.zup.edu.clients.response.DeletePixKeyResponse
import br.com.zup.edu.clients.response.PixKeyDetailsResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.keys.url}")
interface BCBClient {

    @Post(produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun cadastraChavePix(@Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/{key}",produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun deletaChavePix(@Body deletePixKeyRequest: DeletePixKeyRequest, @QueryValue key: String): HttpResponse<DeletePixKeyResponse>

    @Get(value = "/{key}", consumes = [MediaType.APPLICATION_XML])
    fun buscaChavePix(@QueryValue key: String): HttpResponse<PixKeyDetailsResponse>
}