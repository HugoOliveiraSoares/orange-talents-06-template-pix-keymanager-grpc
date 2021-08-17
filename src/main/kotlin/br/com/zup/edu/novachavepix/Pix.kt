package br.com.zup.edu.novachavepix

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint

@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PixValidator::class])
annotation class Pix(
    val message: String = "Chave pix com formato invalido"
)

@Singleton
class PixValidator : ConstraintValidator<Pix, String> {

    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<Pix>,
        context: ConstraintValidatorContext
    ): Boolean {

        if (value == null) {
            return false
        }

        return isTelefone(value) || isCpf(value) || isEmail(value) || isUUID(value)

    }

    fun isTelefone(value: String): Boolean {
        return value.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
    }

    fun isCpf(value: String): Boolean {
        return value.matches("^[0-9]{11}\$".toRegex())
    }

    fun isEmail(value: String): Boolean {
        return value
            .matches(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
                    .toRegex()
            )
    }

    fun isUUID(value: String): Boolean {
        return value
            .matches(
                "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$"
                    .toRegex()
            )
    }

}








