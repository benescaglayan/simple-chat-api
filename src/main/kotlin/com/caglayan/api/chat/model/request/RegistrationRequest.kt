package com.caglayan.api.chat.model.request

import javax.validation.constraints.Email
import javax.validation.constraints.Size


class RegistrationRequest(

        @field:Size(min = 3, max = 18)
        val username: String,

        @field:Size(min = 3, max = 18)
        val firstName: String,

        @field:Size(min = 3, max = 18)
        val lastName: String,

        @field:Email
        val email: String,

        @field:Size(min = 8)
        val password: String
)