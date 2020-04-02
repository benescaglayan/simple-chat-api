package com.caglayan.api.chat.entity

import com.caglayan.api.chat.util.Date
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Block(

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        val blocker: User,

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        val blocked: User

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    var blockedAt: LocalDateTime? = null

    @PrePersist
    private fun onPrePersist() {
        blockedAt = Date.now()
    }

}