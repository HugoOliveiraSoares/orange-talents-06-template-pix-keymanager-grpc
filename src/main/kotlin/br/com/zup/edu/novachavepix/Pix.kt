package br.com.zup.edu.novachavepix

import br.com.zup.edu.TipoChave
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PixValidator::class])
annotation class Pix(
    val message: String = "Chave pix com formato invalido"
)

@Singleton
class PixValidator : ConstraintValidator<Pix, NovaChavePix> {

    override fun isValid(
        pix: NovaChavePix?,
        annotationMetadata: AnnotationValue<Pix>,
        context: ConstraintValidatorContext
    ): Boolean {

        if (pix == null) {
            return false
        }

        return isTelefone(pix.chave, pix.tipoChave) || isCpf(pix.chave, pix.tipoChave) ||
                isEmail(pix.chave, pix.tipoChave) || isUUID(pix.chave, pix.tipoChave)

    }

    fun isTelefone(chave: String, tipoChave: TipoChave): Boolean {
        return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex()) &&
                tipoChave == TipoChave.TELEFONE
    }

    fun isCpf(chave: String, tipoChave: TipoChave): Boolean {
        return chave.matches("^[0-9]{11}\$".toRegex()) &&
                tipoChave == TipoChave.CPF
    }

    fun isEmail(chave: String, tipoChave: TipoChave): Boolean {
        return chave
            .matches(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
                    .toRegex()
            ) && tipoChave == TipoChave.EMAIL
    }

    fun isUUID(chave: String, tipoChave: TipoChave): Boolean {
        return chave
            .matches(
                "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$"
                    .toRegex()
            ) && tipoChave == TipoChave.CHAVE_ALEATORIA
    }

}






