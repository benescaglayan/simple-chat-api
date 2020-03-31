package com.caglayan.api.chat.model.request

import javax.validation.constraints.Email

class VerificationResendRequest(

        @field:Email
        val email: String
)