package com.caglayan.api.chat.util


import org.springframework.stereotype.Component
import java.util.*

@Component
class Random {

    companion object {

        fun uuid() = UUID.randomUUID().toString()

    }
}