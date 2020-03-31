package com.caglayan.api.chat.service

import com.caglayan.api.chat.model.enum.LogAction

interface LogService {

    fun info(message: LogAction, context: Map<String, Any?>)

    fun warn(message: LogAction, context: Map<String, Any?> = hashMapOf())

    fun error(message: LogAction, context: Map<String, Any?> = hashMapOf())
}