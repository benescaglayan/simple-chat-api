package com.caglayan.api.chat.event.model

import org.springframework.context.ApplicationEvent

abstract class BaseEvent(val name: String): ApplicationEvent(name)