package com.caglayan.api.chat.service

import com.caglayan.api.chat.entity.Message
import com.caglayan.api.chat.entity.User
import com.caglayan.api.chat.exception.MessageNotFoundException
import com.caglayan.api.chat.exception.UserNotFoundException
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.repository.MessageRepository
import com.caglayan.api.chat.util.Date
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


@Service
class MessageServiceImpl(val messageRepository: MessageRepository, val blockService: BlockService,
                         val userService: UserService, val logService: LogService, val entityManager: EntityManager) : MessageService {

    override fun send(senderUsername: String, receiverUsername: String, text: String): Long {
        logService.info(LogAction.MESSAGE_SEND_REQUEST, mapOf("sender" to senderUsername, "receiver" to receiverUsername, "messageLength" to text.length))

        val receiver = userService.getByUsername(receiverUsername)
        if (!receiver.isConfirmed) {
            throw UserNotFoundException()
        }

        val sender = userService.getByUsername(senderUsername)
        val message = Message(text, sender, receiver)

        if (blockService.isUserBlockedBy(sender, receiver)) {
            message.isFromBlockedUser = true
        }

        val x = save(message)

        logService.info(LogAction.MESSAGE_CREATED, mapOf("id" to x.id, "senderId" to message.sender.username, "receiver" to message.receiver.username))
        return x.id
    }

    override fun get(username: String, messageId: Long): Message {
        val message = messageRepository.findById(messageId).orElseThrow { MessageNotFoundException(owner = username, messageId = messageId) }
        if ((message.isFromBlockedUser && message.receiver.username == username) || !listOf(message.receiver.username, message.sender.username).contains(username)) {
            throw MessageNotFoundException(owner = username, messageId = messageId)
        }

        return message
    }

    override fun getMessages(ownerUsername: String, searchingUsername: String?, from: String?, to: String?): List<Message> {
        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(Message::class.java)

        val root: Root<Message> = cq.from(Message::class.java)
        val sender = root.join<Message, User>("sender", JoinType.LEFT)
        val receiver = root.join<Message, User>("receiver", JoinType.LEFT)

        val predicates = ArrayList<Predicate>()
        predicates.add(cb.or(cb.equal(sender.get<String>("username"), ownerUsername), cb.equal(receiver.get<String>("username"), ownerUsername)))
        predicates.add(cb.or(cb.equal(root.get<Boolean>("isFromBlockedUser"), false), cb.equal(sender.get<String>("username"), ownerUsername)))

        if (searchingUsername != null) {
            predicates.add(cb.or(cb.equal(sender.get<String>("username"), searchingUsername), cb.equal(receiver.get<String>("username"), searchingUsername)))
        }

        if (from != null) {
            predicates.add(cb.and(cb.greaterThanOrEqualTo(root.get<LocalDateTime>("sentAt"), Date.formatDatetime(from))))
        }

        if (to != null) {
            predicates.add(cb.and(cb.lessThanOrEqualTo(root.get<LocalDateTime>("sentAt"), Date.formatDatetime(to))))
        }

        cq.where(*predicates.toTypedArray())

        val query: TypedQuery<Message> = entityManager.createQuery(cq)

        query.resultList.filter { it.readAt == null && it.receiver.username == ownerUsername }.forEach {
            it.readAt = LocalDateTime.now()
            save(it)
        }

        return query.resultList
    }

    override fun save(message: Message) = messageRepository.save(message)

    override fun getNewMessages(receiverUsername: String): List<Message>? {
        val newMessages = messageRepository.findByReceiverUsernameAndReadAtNullAndIsFromBlockedUserFalse(receiverUsername)
        newMessages.forEach {
            it.readAt = LocalDateTime.now()
            save(it)
        }

        return newMessages
    }

}