package com.caglayan.api.chat.document

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
    var id: String = Random.uuid()

    private var createdAt: LocalDateTime? = null

    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }

}