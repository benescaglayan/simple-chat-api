package com.caglayan.api.chat.service

import com.caglayan.api.chat.event.model.BaseEvent
import com.caglayan.api.chat.model.enum.LogAction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class EventBusServiceImpl(val logService: LogService, val publisher: ApplicationEventPublisher): EventBusService {

    override fun send(event: BaseEvent) = publish(event)

    private fun publish(event: BaseEvent) {
        logService.info(LogAction.EVENT_TRIGGERED, mapOf("name" to event.name))
        publisher.publishEvent(event)
    }
}