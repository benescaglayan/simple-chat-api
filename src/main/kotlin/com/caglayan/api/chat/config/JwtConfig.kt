package com.caglayan.api.chat.config


import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig {

    @Value("\${app.security.jwt.header:Authorization}")
    lateinit var header: String

    @Value("\${app.security.jwt.prefix:Bearer}")
    lateinit var prefix: String

    @Value("\${app.security.jwt.expiration}")
    val expiration: Int = 0 // default 24 hours

    @Value("\${app.security.jwt.secret}")
    val secret: String? = null

}