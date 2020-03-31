package com.caglayan.api.chat.exception

class UserNotFoundException(private val msg: String = "User not found.", val email: String? = null, val username: String? = null) : Exception(msg)