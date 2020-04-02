package com.caglayan.api.chat.job

import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.service.LogService
import com.caglayan.api.chat.service.UserService
import com.caglayan.api.chat.service.VerificationService
import com.caglayan.api.chat.util.Date
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class UserJob(val userService: UserService, val verificationService: VerificationService, val logService: LogService) {

    @Transactional
    @Scheduled(cron = "0 0 */6 * * *")
    fun deleteUserDueVerificationDeadline() {
        val unconfirmedUsersDueVerification = userService.getUnconfirmedUsersRegisteredBefore(Date.now().minusDays(7))

        logService.info(LogAction.DUE_VERIFICATION_JOB, mapOf("unconfirmed_users" to unconfirmedUsersDueVerification.size))

        unconfirmedUsersDueVerification.forEach {
            verificationService.deleteByUserId(it.id)
            userService.deleteById(it.id)
        }
    }

}