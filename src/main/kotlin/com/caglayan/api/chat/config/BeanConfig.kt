package com.caglayan.api.chat.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder


@Configuration
class BeanConfig(val appConfig: AppConfig) {

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder(12)

    @Bean
    fun mailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = appConfig.mailServerHost
        mailSender.port = appConfig.mailServerPort.toInt()
        mailSender.username = appConfig.mailServerUsername
        mailSender.password = appConfig.mailServerPassword

        val props = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.debug"] = "true"

        return mailSender
    }

}