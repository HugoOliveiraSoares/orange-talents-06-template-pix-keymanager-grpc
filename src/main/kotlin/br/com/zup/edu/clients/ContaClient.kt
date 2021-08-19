package br.com.zup.edu.clients

import br.com.zup.edu.TipoConta
import br.com.zup.edu.chavepix.DadosDaContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${erpItau.contas.url}")
interface ContaClient {

    @Get("/{id}/contas{?tipo}")
    fun buscaContaPorIdETipo(@PathVariable id: String, @QueryValue tipo: TipoConta): HttpResponse<DadosDaContaResponse>

}