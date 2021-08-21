package br.com.zup.edu.model

import br.com.zup.edu.TipoConta
import br.com.zup.edu.clients.request.BankAccountRequest
import br.com.zup.edu.clients.request.CreatePixKeyRequest
import br.com.zup.edu.clients.request.OwnerRequest
import br.com.zup.edu.enums.AccountType
import br.com.zup.edu.enums.TypePerson
import javax.persistence.*

@Entity
class Conta(
    @Enumerated(EnumType.STRING)
    val tipo: TipoConta,
    @OneToOne(cascade = [CascadeType.PERSIST])
    val instituicao: Instituicao,
    val agencia: String,
    val numero: String,
    @ManyToOne(cascade = [CascadeType.PERSIST])
    val titular: Titular
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun toRequest(): BankAccountRequest {
        return BankAccountRequest(
            instituicao.ispb,
            agencia,
            numero,
            AccountType.values()[tipo.ordinal]
        )
    }

}

@Entity
class Titular(
    val id: String,
    val nome: String,
    val cpf: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val idTitular: Long? = null

    fun toRequest(): OwnerRequest {

        return OwnerRequest(
            TypePerson.NATURAL_PERSON,
            nome,
            cpf
        )

    }
}

@Entity
class Instituicao(
    val nome: String,
    val ispb: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val idTitular: Long? = null
}


