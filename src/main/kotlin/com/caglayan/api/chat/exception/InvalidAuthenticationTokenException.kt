package com.caglayan.api.chat.exception

class InvalidAuthenticationTokenException(msg: String = "Invalid Authentication Token", val principal: String?) : Exception(msg)