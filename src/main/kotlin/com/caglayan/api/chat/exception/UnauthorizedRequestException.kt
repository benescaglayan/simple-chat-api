package com.caglayan.api.chat.exception

class UnauthorizedRequestException(msg: String = "Unauthorized", val action: String?, val data: String?) : Exception(msg)