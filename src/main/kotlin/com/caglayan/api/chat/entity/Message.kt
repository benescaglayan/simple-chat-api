package com.caglayan.api.chat.entity

import com.caglayan.api.chat.util.Crypto
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
data class Message(

        @NotBlank
        @Column(nullable = false, updatable = false, columnDefinition = "text")
        var text: String,

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "sender_user", updatable = false, nullable = false)
        val sender: User,

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "receiver_user", updatable = false, nullable = false)
        val receiver: User

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    var sentAt: LocalDateTime? = null

    var readAt: LocalDateTime? = null

    @JsonIgnore
    var isFromBlockedUser: Boolean = false

    @PrePersist
    private fun prePersist() {
        text = Crypto.encrypt(text)
        sentAt = LocalDateTime.now()
    }

    @PostLoad
    private fun postLoad() {
        text = Crypto.decrypt(text)
    }

}