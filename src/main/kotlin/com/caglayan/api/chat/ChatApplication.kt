package com.caglayan.api.chat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class ChatApplication {

	companion object {

		@JvmStatic
		fun main(args: Array<String>) {
			runApplication<ChatApplication>(*args)
		}

	}
}