package com.caglayan.api.chat.event.listener

import com.caglayan.api.chat.config.AppConfig
import com.caglayan.api.chat.event.model.RegistrationCompletedEvent
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.service.LogService
import com.caglayan.api.chat.service.MailService
import com.caglayan.api.chat.service.VerificationService
import org.springframework.context.event.EventListener
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class RegistrationEventListener(val appConfig: AppConfig, val mailService: MailService, val messageSource: ResourceBundleMessageSource,
                                val verificationService: VerificationService, val logService: LogService) {

    @Async
    @EventListener
    fun onRegistrationCompletedEvent(event: RegistrationCompletedEvent) {
        val token = verificationService.generate(event.user)

        val content = messageSource.getMessage("confirmation.email", arrayOf(event.user.username, "${appConfig.baseUrl}/verifications/?token=$token"),
                LocaleContextHolder.getLocale())

        mailService.send(event.user.email, "Chat API Email Confirmation", content)
        logService.info(LogAction.VERIFICATION_SENT, mapOf("userId" to event.user.id))
    }
}