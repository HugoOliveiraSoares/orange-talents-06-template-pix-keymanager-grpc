package br.com.zup.edu.novachavepix

import br.com.zup.edu.TipoConta
import javax.persistence.*

@Entity
class Conta(
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


