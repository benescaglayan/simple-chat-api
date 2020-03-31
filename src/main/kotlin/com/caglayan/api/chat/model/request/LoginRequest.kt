package com.caglayan.api.chat.model.request

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class LoginRequest(

        @field:NotBlank
        val username: String,

        @field:Size(min = 8)
        val password: String
)