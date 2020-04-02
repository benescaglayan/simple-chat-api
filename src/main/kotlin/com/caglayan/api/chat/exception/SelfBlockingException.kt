package com.caglayan.api.chat.exception

class SelfBlockingException(private val msg: String = "You cannot block yourself.", val username: String) : Exception(msg)