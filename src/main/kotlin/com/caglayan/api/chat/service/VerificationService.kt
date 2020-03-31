package com.caglayan.api.chat.service

import com.caglayan.api.chat.entity.User

interface VerificationService {

    fun generate(user: User): String

    fun verify(token: String)

    fun resend(email: String)

}