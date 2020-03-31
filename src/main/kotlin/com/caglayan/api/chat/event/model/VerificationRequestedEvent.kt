package com.caglayan.api.chat.event.model

import com.caglayan.api.chat.entity.User

class VerificationRequestedEvent(val user: User): BaseEvent(NAME) {

    companion object {
        var NAME = "verification-requested-event"
    }
}