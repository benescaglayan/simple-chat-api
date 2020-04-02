package com.caglayan.api.chat.util


import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField


@Component
class Date {

    companion object {

        private val formatter = DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd[ HH:mm:ss]")
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter()

        fun formatDatetime(date: String): LocalDateTime? {
            return try {
                LocalDateTime.parse(date, formatter)
            } catch (ex: Exception) {
                null
            }
        }

        fun now() = LocalDateTime.now()

    }
}