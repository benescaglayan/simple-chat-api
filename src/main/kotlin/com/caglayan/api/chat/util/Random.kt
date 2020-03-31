package com.caglayan.api.chat.util


import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ThreadLocalRandom


@Component
class Random {

    companion object {

        private val charPool: List<Char> = ('a'..'z') + ('A'..'Z')

        fun uuid() = UUID.randomUUID().toString()

        fun string(length: Int = 6): String {
            return (1..length)
                    .map { ThreadLocalRandom.current().nextInt(0, charPool.size) }
                    .map(charPool::get)
                    .joinToString("")
        }

    }
}