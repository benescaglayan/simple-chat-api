package com.caglayan.api.chat.entity

import com.caglayan.api.chat.util.Date
import com.caglayan.api.chat.util.Random
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Verification(

        @OneToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "user", updatable = false, nullable = false)
        val user: User

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    val token = Random.uuid()

    var sentAt: LocalDateTime? = null

    @PrePersist
    private fun onPrePersist() {
        if (sentAt == null) {
            sentAt = Date.now()
        }
    }

}