package com.caglayan.api.chat.service

import com.caglayan.api.chat.entity.Message

interface MessageService {

    fun send(senderUsername: String, receiverUsername: String, text: String): Long

    fun get(username: String, messageId: Long): Message

    fun getMessages(ownerUsername: String, searchingUsername: String?, from: String?, to: String?): List<Message>

    fun save(message: Message): Message

    fun getNewMessages(receiverUsername: String): List<Message>?

}