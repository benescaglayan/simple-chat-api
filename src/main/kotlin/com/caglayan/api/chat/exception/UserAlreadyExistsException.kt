package com.caglayan.api.chat.exception

class UserAlreadyExistsException(private val msg: String = "Username or email is in use.", val username: String, val email: String) : Exception(msg)