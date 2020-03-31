package com.caglayan.api.chat.exception

class InvalidVerificationTokenException(private val msg: String = "Verification link is either invalid or expired.", val token: String) : Exception(msg)