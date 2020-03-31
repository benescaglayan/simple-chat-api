package com.caglayan.api.chat.model.request

import javax.validation.constraints.NotBlank

class MessageRequest(

        @field:NotBlank
        val text: String,

        @field:NotBlank
        val receiverUsername: String
)