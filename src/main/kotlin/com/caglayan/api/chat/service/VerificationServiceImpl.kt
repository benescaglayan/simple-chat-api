package com.caglayan.api.chat.service

import com.caglayan.api.chat.config.AppConfig
import com.caglayan.api.chat.entity.User
import com.caglayan.api.chat.entity.Verification
import com.caglayan.api.chat.event.model.UserVerifiedEvent
import com.caglayan.api.chat.event.model.VerificationRequestedEvent
import com.caglayan.api.chat.exception.InvalidVerificationTokenException
import com.caglayan.api.chat.exception.UserAlreadyConfirmedException
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.repository.VerificationRepository
import com.caglayan.api.chat.util.Date
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class VerificationServiceImpl(val appConfig: AppConfig, val verificationRepository: VerificationRepository, val userService: UserService,
                              val eventBusService: EventBusService, val logService: LogService) : VerificationService {

    override fun generate(user: User): String {
        verificationRepository.findByUser(user)?.let { delete(it) }

        val verification = save(Verification(user))

        logService.info(LogAction.VERIFICATION_GENERATED, mapOf("verificationId" to verification.id, "userId" to user.id))

        return verification.token
    }

    override fun verify(token: String) {
        logService.info(LogAction.VERIFICATION_REQUEST, mapOf("token" to token))

        val verification = verificationRepository.findByToken(token) ?: throw InvalidVerificationTokenException(token = token)

        if (verification.sentAt!!.isBefore(Date.now().minusHours(appConfig.verificationThreshold.toLong()))) {
            throw InvalidVerificationTokenException(token = token)
        }

        delete(verification)
        eventBusService.send(UserVerifiedEvent(verification.user))
    }

    override fun resend(email: String) {
        logService.info(LogAction.VERIFICATION_RESEND_REQUEST, mapOf("email" to email))

        val user = userService.getByEmail(email)
        if (user.isConfirmed) {
            throw UserAlreadyConfirmedException(email = email)
        }

        eventBusService.send(VerificationRequestedEvent(user))
    }

    override fun deleteByUserId(userId: Long) {
        verificationRepository.deleteByUserId(userId)

        logService.info(LogAction.VERIFICATION_DELETED, mapOf("userId" to userId))
    }

    private fun save(verification: Verification) = verificationRepository.save(verification)

    private fun delete(verification: Verification) = verificationRepository.delete(verification)


}