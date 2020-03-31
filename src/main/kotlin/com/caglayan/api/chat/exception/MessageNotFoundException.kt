package com.caglayan.api.chat.exception

class MessageNotFoundException(private val msg: String = "Message not found.", val owner: String? = null, val messageId: Long? = null) : Exception(msg)