package br.com.zup.edu.clients.response

import br.com.zup.edu.enums.AccountType
import br.com.zup.edu.enums.KeyType
import br.com.zup.edu.enums.TypePerson
import java.time.LocalDateTime

class CreatePixKeyResponse(

    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: LocalDateTime

) {

}

class BankAccountResponse(

    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType

) {

}

class OwnerResponse(

    val type: TypePerson,
    val name: String,
    val taxIdNumber: String

) {

}
