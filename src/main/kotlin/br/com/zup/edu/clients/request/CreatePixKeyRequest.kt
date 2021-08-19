package br.com.zup.edu.clients.request

import br.com.zup.edu.enums.AccountType
import br.com.zup.edu.enums.KeyType
import br.com.zup.edu.enums.TypePerson

class CreatePixKeyRequest(

    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest

) {

}

class OwnerRequest(

    val type: TypePerson,
    val name: String,
    val taxIdNumber: String

) {
}

class BankAccountRequest(

    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType

) {
}
