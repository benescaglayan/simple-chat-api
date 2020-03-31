package com.caglayan.api.chat.service

import com.caglayan.api.chat.event.model.BaseEvent

interface EventBusService {

    fun send(event: BaseEvent)
}