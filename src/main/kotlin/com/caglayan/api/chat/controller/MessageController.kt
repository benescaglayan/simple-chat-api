package com.caglayan.api.chat.controller

import com.caglayan.api.chat.model.request.MessageRequest
import com.caglayan.api.chat.model.response.MessageListResponse
import com.caglayan.api.chat.model.response.MessageResponse
import com.caglayan.api.chat.model.response.MessageSentResponse
import com.caglayan.api.chat.service.MessageService
import com.caglayan.api.chat.util.JwtUtil
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI


@RestController
@RequestMapping(path = ["messages"])
class MessageController(val messageService: MessageService, jwtUtil: JwtUtil): BaseController(jwtUtil) {

    @PostMapping
    @Secured("ROLE_USER")
    fun message(@RequestBody @Validated request: MessageRequest): ResponseEntity<MessageSentResponse> {
        val messageId = messageService.send(getAuthenticatedUsername(), request.receiverUsername, request.text)

        return ResponseEntity.created(URI("messages/$messageId")).body(MessageSentResponse())
    }

    @GetMapping("/{messageId}")
    @Secured("ROLE_USER")
    fun getMessage(@PathVariable messageId: Long): ResponseEntity<MessageResponse> {
        val message = messageService.get(getAuthenticatedUsername(), messageId)

        return ResponseEntity.ok().body(MessageResponse(message))
    }

    @GetMapping
    @Secured("ROLE_USER")
    fun getMessages(@RequestParam username: String?, @RequestParam from: String?, @RequestParam to: String?): ResponseEntity<MessageListResponse> {
        val messages = messageService.getMessages(getAuthenticatedUsername(), username, from, to)

        return ResponseEntity.ok().body(MessageListResponse(messages))
    }

    @GetMapping("/new")
    @Secured("ROLE_USER")
    fun getNewMessages(): ResponseEntity<MessageListResponse> {
        val messages = messageService.getNewMessages(getAuthenticatedUsername())

        return ResponseEntity.ok().body(MessageListResponse(messages))
    }

}