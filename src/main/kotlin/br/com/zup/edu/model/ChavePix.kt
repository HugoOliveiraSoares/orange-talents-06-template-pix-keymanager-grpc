package br.com.zup.edu.model

import br.com.zup.edu.ChavePixDetailResponse
import br.com.zup.edu.ChaveResponse
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.clients.request.CreatePixKeyRequest
import br.com.zup.edu.clients.request.DeletePixKeyRequest
import br.com.zup.edu.enums.KeyType
import br.com.zup.edu.validation.ValidUUID
import com.google.protobuf.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(

    @ValidUUID
    @field:NotEmpty @field:NotBlank
    @Column(nullable = false)
    val identificadorCliente: String,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @ValidUUID
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

    @NotNull
    val criadoEm = LocalDateTime.now()

    fun toRequest(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            KeyType.values()[tipoChave.ordinal],
            chave,
            conta.toRequest(),
            conta.titular.toRequest()
        )
    }

    fun toDeletePixKeyRequest(): DeletePixKeyRequest {

        return DeletePixKeyRequest(
            chave,
            conta.instituicao.ispb
        )

    }

    fun utualizaChave(key: String) {
        chave = key
    }

    fun toResponse(createdAt: LocalDateTime?): ChavePixDetailResponse {

        val instant = createdAt?.atZone(ZoneId.of("UTC"))?.toInstant()!!

        return ChavePixDetailResponse.newBuilder()
            .setPixId(pixId!!)
            .setIdentificador(identificadorCliente)
            .setTipoChave(tipoChave)
            .setChave(chave)
            .setNome(conta.titular.nome)
            .setCpf(conta.titular.cpf)
            .setNomeInstituicao(conta.instituicao.nome)
            .setAgencia(conta.agencia)
            .setTipoConta(tipoConta)
            .setCriadoEm(
                Timestamp.newBuilder()
                    .setSeconds(instant.epochSecond)
                    .setNanos(instant.nano)
                    .build()
            )
            .build()

    }

    fun toChaveResponse(): ChaveResponse {

        val instant: Instant? = criadoEm.atZone(ZoneId.of("UTC")).toInstant()

        return ChaveResponse.newBuilder()
            .setPixId(pixId!!)
            .setIdentificador(identificadorCliente)
            .setTipoChave(tipoChave)
            .setChave(chave)
            .setTipoConta(tipoConta)
            .setCriadoEm(
                Timestamp.newBuilder()
                    .setSeconds(instant?.epochSecond ?: 0)
                    .setNanos(instant?.nano ?: 0)
                    .build()
            )
            .build()
    }

}