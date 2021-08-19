package br.com.zup.edu.model

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
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

    @field:NotBlank
    @field:Size(max = 77)
    @Column(nullable = false, unique = true)
    val chave: String,

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

}