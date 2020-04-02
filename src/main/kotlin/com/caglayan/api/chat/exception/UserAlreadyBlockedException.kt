package com.caglayan.api.chat.exception

class UserAlreadyBlockedException(private val msg: String = "You have already blocked this user.", val blockerUsername: String, val blockedUsername: String) : Exception(msg)