package br.com.zup.edu.clients.response

import br.com.zup.edu.ChavePixDetailResponse
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.enums.KeyType
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

data class PixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: LocalDateTime
) {

    fun toChavePixDetailResponse(): ChavePixDetailResponse {

        val instant = createdAt.atZone(ZoneId.of("UTC"))?.toInstant()!!

        return ChavePixDetailResponse.newBuilder()
            .setTipoChave(TipoChave.values()[keyType.ordinal])
            .setChave(key)
            .setNome(owner.name)
            .setCpf(owner.taxIdNumber)
            .setNomeInstituicao(bankAccount.participant)
            .setAgencia(bankAccount.branch)
            .setNumeroDaConta(bankAccount.accountNumber)
            .setTipoConta(TipoConta.values()[bankAccount.accountType.ordinal])
            .setCriadoEm(
                Timestamp.newBuilder()
                    .setSeconds(instant.epochSecond)
                    .setNanos(instant.nano)
                    .build()
            )
            .build()
    }

}
