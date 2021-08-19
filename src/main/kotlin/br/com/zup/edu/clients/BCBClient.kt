package br.com.zup.edu.clients

import br.com.zup.edu.clients.request.CreatePixKeyRequest
import br.com.zup.edu.clients.response.CreatePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.keys.url}")
interface BCBClient {

    @Post
    fun buscaTodasAsChaves(createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

}