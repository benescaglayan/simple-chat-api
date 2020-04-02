package com.caglayan.api.chat.event.listener

import com.caglayan.api.chat.event.model.UserVerifiedEvent
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.service.LogService
import com.caglayan.api.chat.service.UserService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class UserEventListener(val userService: UserService, val logService: LogService) {

    @Async
    @EventListener
    fun onUserVerifiedEvent(event: UserVerifiedEvent) {
        val user = event.user
        user.isConfirmed = true

        userService.save(user)
        logService.info(LogAction.USER_CONFIRMED, mapOf("idUser" to user.id))
    }
}