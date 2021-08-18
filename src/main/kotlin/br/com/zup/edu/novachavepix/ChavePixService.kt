package br.com.zup.edu.novachavepix

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class ChavePixService(
    @Inject val contaClient: ContaClient,
    @Inject val chavePixRepository: ChavePixRepository
) {

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix): Long {

        if (chavePixRepository.existsByChave(novaChavePix.chave)) {
            throw StatusRuntimeException(
                Status.ALREADY_EXISTS
                    .withDescription("Chave Pix já cadastrada")
            )
        }

        val contaResponse = contaClient
            .buscaContaPorIdETipo(novaChavePix.identificadorCliente, novaChavePix.tipoConta)

        val conta = contaResponse.body()?.toModel() ?: throw StatusRuntimeException(
            Status.NOT_FOUND
                .withDescription("Cliente não encontrado no Itau"))

        val chavePix = novaChavePix.toModel(conta)

        chavePixRepository.save(chavePix)

        return chavePix.pixId!!

    }

}