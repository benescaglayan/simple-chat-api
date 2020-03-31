package com.caglayan.api.chat.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.Size

@Entity
data class User(

        @Size(min = 3, max = 18)
        @Column(nullable = false)
        val username: String,

        @Size(min = 3, max = 18)
        @Column(nullable = false)
        @JsonIgnore
        val firstName: String,

        @Size(min = 3, max = 18)
        @Column(nullable = false)
        @JsonIgnore
        val lastName: String,

        @Email
        @Column(nullable = false)
        @JsonIgnore
        val email: String,

        @Column(nullable = false)
        @JsonIgnore
        val password: String

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Long = 0

    @JsonIgnore
    var createdAt: LocalDateTime? = null

    @JsonIgnore
    var updatedAt: LocalDateTime? = null

    @JsonIgnore
    var isConfirmed = false

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sender", orphanRemoval = true)
    var sentMessages: List<Message> = arrayListOf()

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "receiver", orphanRemoval = true)
    var receivedMessages: List<Message> = arrayListOf()

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "blocker", orphanRemoval = true)
    var blocks: List<Block> = arrayListOf()

    @PrePersist
    private fun prePersist() {
        createdAt = LocalDateTime.now()
    }

    @PreUpdate
    private fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

}