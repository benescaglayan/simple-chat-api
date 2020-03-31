package com.caglayan.api.chat.exception

class BlockNotFoundException(private val msg: String = "Block not found.", val blocker: String? = null, val blocked: String? = null) : Exception(msg)