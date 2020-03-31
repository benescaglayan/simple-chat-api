package com.caglayan.api.chat.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Value("\${app.baseUrl}")
    lateinit var baseUrl: String

    @Value("\${app.verificationThreshold}")
    lateinit var verificationThreshold: String

    @Value("\${spring.mail.host}")
    lateinit var mailServerHost: String

    @Value("\${spring.mail.port}")
    lateinit var mailServerPort: String

    @Value("\${spring.mail.username}")
    lateinit var mailServerUsername: String

    @Value("\${spring.mail.password}")
    lateinit var mailServerPassword: String

}