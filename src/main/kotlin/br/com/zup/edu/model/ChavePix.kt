package br.com.zup.edu.model

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.clients.request.CreatePixKeyRequest
import br.com.zup.edu.clients.request.DeletePixKeyRequest
import br.com.zup.edu.enums.KeyType
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(

    @field:NotEmpty @field:NotBlank
    @Column(nullable = false)
    val identificadorCliente: String,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @field:Size(max = 77)
    @Column(nullable = false, unique = true)
    var chave: String,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,

    @field:NotNull
    @field:ManyToOne(cascade = [CascadeType.PERSIST])
    val conta: Conta

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val pixId: Long? = null

    fun toRequest(): CreatePixKeyRequest{
        return CreatePixKeyRequest(
            KeyType.values()[tipoChave.ordinal],
            chave,
            conta.toRequest(),
            conta.titular.toRequest()
        )
    }

    fun toDeletePixKeyRequest(): DeletePixKeyRequest{

        return DeletePixKeyRequest(
            chave,
            conta.instituicao.ispb
        )

    }

    fun utualizaChave(key: String) {
        chave = key
    }

}