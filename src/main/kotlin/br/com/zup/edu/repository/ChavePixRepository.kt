package br.com.zup.edu.repository

import br.com.zup.edu.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {

    fun existsByChave(chave: String?): Boolean
    fun findByChave(chave: String): Optional<ChavePix>
}