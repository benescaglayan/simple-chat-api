package com.caglayan.api.chat.repository

import com.caglayan.api.chat.entity.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long> {

    fun findByReceiverUsernameAndReadAtNullAndIsFromBlockedUserFalse(receiverUsername: String): List<Message>

}