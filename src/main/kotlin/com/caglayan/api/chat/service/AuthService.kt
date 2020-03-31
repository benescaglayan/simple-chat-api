package com.caglayan.api.chat.service

import com.caglayan.api.chat.model.request.RegistrationRequest

interface AuthService {

    fun register(request: RegistrationRequest): Long

}