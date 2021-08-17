package br.com.zup.edu.novachavepix

import br.com.zup.edu.ChavePixRequest
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
    val tipoChave: ChavePixRequest.TipoChave,

    @field:NotBlank
    @field:Size(max = 77)
    @field:Pix
    @Column(nullable = false, unique = true)
    val chave: String,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoConta: ChavePixRequest.TipoConta

) {

    @Id
    @GeneratedValue
    val pixId: Long? = null

    fun criaChave(){

    }

}