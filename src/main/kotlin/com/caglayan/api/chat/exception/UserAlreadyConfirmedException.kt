package com.caglayan.api.chat.exception

class UserAlreadyConfirmedException(private val msg: String = "User was already confirmed.", val email: String) : Exception(msg)