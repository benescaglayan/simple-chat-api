package com.caglayan.api.chat.repository

import com.caglayan.api.chat.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: JpaRepository<User, Long> {

    fun existsByUsernameOrEmail(username: String, email: String): Boolean

    fun findByEmail(email: String): User?

    fun findByUsername(username: String): User?

}