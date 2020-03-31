package com.caglayan.api.chat.service

import com.caglayan.api.chat.model.enum.LogAction
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailServiceImpl(val mailSender: JavaMailSender, val logService: LogService) : MailService {

    override fun send(to: String, subject: String, content: String) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.setSubject(subject)
        message.setText(content)

        mailSender.send(message)
        logService.info(LogAction.EMAIL_SENT, mapOf("to" to to, "subject" to subject))

    }

}