package com.caglayan.api.chat.event.listener

import com.caglayan.api.chat.config.AppConfig
import com.caglayan.api.chat.event.model.VerificationRequestedEvent
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.service.LogService
import com.caglayan.api.chat.service.MailService
import com.caglayan.api.chat.service.VerificationService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class VerificationEventListener(val appConfig: AppConfig, val mailService: MailService,
                                val verificationService: VerificationService, val logService: LogService) {

    @EventListener
    fun onVerificationRequestedEvent(event: VerificationRequestedEvent) {
        val token = verificationService.generate(event.user)

        mailService.send(event.user.email, "Chat API Email Confirmation", "${appConfig.baseUrl}/verifications/?token=$token")
        logService.info(LogAction.VERIFICATION_SENT, mapOf("userId" to event.user.id))
    }
}