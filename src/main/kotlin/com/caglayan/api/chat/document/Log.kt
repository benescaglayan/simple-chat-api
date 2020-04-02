package com.caglayan.api.chat.document

import com.caglayan.api.chat.util.Date
import com.caglayan.api.chat.util.Random
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.persistence.Id
import javax.persistence.PrePersist


@Document(collection = "log")
data class Log(

        val message: String,

        val data: Map<String, Any?>

) {

    @Id
    val id: String = Random.uuid()

    val createdAt = Date.now()

}