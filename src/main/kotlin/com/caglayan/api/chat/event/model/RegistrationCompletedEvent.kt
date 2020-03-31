package com.caglayan.api.chat.event.model

import com.caglayan.api.chat.entity.User

class RegistrationCompletedEvent(val user: User): BaseEvent(NAME) {

    companion object {
        var NAME = "registration-complete-event"
    }
}