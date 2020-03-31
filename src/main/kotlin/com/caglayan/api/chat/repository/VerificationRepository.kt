package com.caglayan.api.chat.repository

import com.caglayan.api.chat.entity.User
import com.caglayan.api.chat.entity.Verification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VerificationRepository : JpaRepository<Verification, Long> {

    fun findByToken(token: String): Verification?

    fun findByUser(user: User): Verification?

}