package com.caglayan.api.chat.event.model

import com.caglayan.api.chat.entity.User

class UserVerifiedEvent(val user: User): BaseEvent(NAME) {

    companion object {
        var NAME = "user-verified-event"
    }
}