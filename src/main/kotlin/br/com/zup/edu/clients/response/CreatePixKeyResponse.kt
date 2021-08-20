package br.com.zup.edu.clients.response

import br.com.zup.edu.enums.AccountType
import br.com.zup.edu.enums.KeyType
import br.com.zup.edu.enums.TypePerson
import java.time.LocalDateTime

data class CreatePixKeyResponse(

    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: LocalDateTime

) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CreatePixKeyResponse) return false

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}

data class BankAccountResponse(

    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BankAccountResponse) return false

        if (participant != other.participant) return false
        if (branch != other.branch) return false
        if (accountNumber != other.accountNumber) return false
        if (accountType != other.accountType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = participant.hashCode()
        result = 31 * result + branch.hashCode()
        result = 31 * result + accountNumber.hashCode()
        result = 31 * result + accountType.hashCode()
        return result
    }
}

data class OwnerResponse(

    val type: TypePerson,
    val name: String,
    val taxIdNumber: String

) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OwnerResponse) return false

        if (type != other.type) return false
        if (name != other.name) return false
        if (taxIdNumber != other.taxIdNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + taxIdNumber.hashCode()
        return result
    }
}
