package com.caglayan.api.chat.controller

import com.caglayan.api.chat.entity.Block
import com.caglayan.api.chat.entity.Message
import com.caglayan.api.chat.entity.User
import com.caglayan.api.chat.repository.BlockRepository
import com.caglayan.api.chat.repository.MessageRepository
import com.caglayan.api.chat.repository.UserRepository
import com.caglayan.api.chat.repository.VerificationRepository
import com.caglayan.api.chat.service.UserDetailsService
import com.caglayan.api.chat.util.JwtUtil
import com.google.gson.Gson
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userDetailsService: UserDetailsService

    var gson = Gson()

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var blockRepository: BlockRepository

    @Autowired
    lateinit var jwtUtil: JwtUtil

    @Autowired
    lateinit var verificationRepository: VerificationRepository

    @Autowired
    lateinit var messageRepository: MessageRepository


    private val passwordEncoder = BCryptPasswordEncoder(12)


    @Nested
    inner class Message {

        @Test
        fun givenInvalidRequest_thenReturn400() {
            var messageBody = mapOf(
                    "text" to "",
                    "receiverUsername" to "somebody"
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(messageBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)

            messageBody = mapOf(
                    "text" to "Hello World!"
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(messageBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)

            messageBody = mapOf(
                    "text" to "Hello World!",
                    "receiverUsername" to ""
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(messageBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        fun givenInvalidToken_thenReturn403() {
            val messageBody = mapOf(
                    "text" to "Hello World!",
                    "receiverUsername" to "somebody"
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(messageBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun givenValidTokenAndUnconfirmedReceiver_thenReturn200() {
            val senderUsername = "message_test_sender"
            val receiverUsername = "message_test_receiver"

            val sender = User(senderUsername, "John", "Doe", "message_test_sender@message.com", passwordEncoder.encode("12345689"))
            sender.isConfirmed = true
            userRepository.save(sender)

            val receiver = User(receiverUsername, "John", "Doe", "message_test_receiver@message.com", passwordEncoder.encode("12345689"))
            userRepository.save(receiver)

            val messageBody = mapOf(
                    "text" to "Hello World!",
                    "receiverUsername" to receiverUsername
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                    .header("Authorization", "Bearer ${jwtUtil.generateJwt(userDetailsService.loadUserByUsername(senderUsername))}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(messageBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        @Test
        fun givenValidTokenAndBlockedSender_thenReturn201() {
            val senderUsername = "message_test_sender"
            val receiverUsername = "message_test_receiver"
            val text = "Hello World!"

            val sender = User(senderUsername, "John", "Doe", "message_test_sender@message.com", passwordEncoder.encode("12345689"))
            sender.isConfirmed = true
            userRepository.save(sender)

            val receiver = User(receiverUsername, "John", "Doe", "message_test_receiver@message.com", passwordEncoder.encode("12345689"))
            receiver.isConfirmed = true
            userRepository.save(receiver)

            blockRepository.save(Block(receiver, sender))

            val messageBody = mapOf(
                    "text" to text,
                    "receiverUsername" to receiverUsername
            )

            val createdMessageLocation = mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                    .header("Authorization", "Bearer ${jwtUtil.generateJwt(userDetailsService.loadUserByUsername(senderUsername))}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(messageBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated)
                    .andExpect(MockMvcResultMatchers.header().exists("Location"))
                    .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.containsString("messages/")))
                    .andReturn().response.getHeaderValue("Location")

            val messageId = createdMessageLocation.toString().substringAfter("/").toLong()

            val message = messageRepository.findById(messageId).get()

            assertThat(message).isNotNull
            assertThat(message.text).isEqualTo(text)
            assertThat(message.sender.username).isEqualTo(senderUsername)
            assertThat(message.receiver.username).isEqualTo(receiverUsername)
            assertTrue(message.isFromBlockedUser)
        }

        @Test
        fun givenValidTokenAndConfirmedUsers_thenReturn201() {
            val senderUsername = "message_test_sender"
            val receiverUsername = "message_test_receiver"
            val text = "Hello World!"

            val sender = User(senderUsername, "John", "Doe", "message_test_sender@message.com", passwordEncoder.encode("12345689"))
            sender.isConfirmed = true
            userRepository.save(sender)

            val receiver = User(receiverUsername, "John", "Doe", "message_test_receiver@message.com", passwordEncoder.encode("12345689"))
            receiver.isConfirmed = true
            userRepository.save(receiver)

            val messageBody = mapOf(
                    "text" to text,
                    "receiverUsername" to receiverUsername
            )

            val createdMessageLocation = mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                    .header("Authorization", "Bearer ${jwtUtil.generateJwt(userDetailsService.loadUserByUsername(senderUsername))}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(messageBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated)
                    .andExpect(MockMvcResultMatchers.header().exists("Location"))
                    .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.containsString("messages/")))
                    .andReturn().response.getHeaderValue("Location")

            val messageId = createdMessageLocation.toString().substringAfter("/").toLong()

            val message = messageRepository.findById(messageId).get()

            assertThat(message).isNotNull
            assertThat(message.text).isEqualTo(text)
            assertThat(message.sender.username).isEqualTo(senderUsername)
            assertThat(message.receiver.username).isEqualTo(receiverUsername)
        }

    }

    @Nested
    inner class Get {

        @Test
        fun givenNoToken_thenReturn403() {
            mockMvc.perform(MockMvcRequestBuilders.get("/messages/${Random.nextInt()}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun givenInvalidId_thenReturn404() {
            val ownerUsername = "message_test_getter"

            val owner = User(ownerUsername, "John", "Doe", "message_test_getterr@message.com", passwordEncoder.encode("12345689"))
            owner.isConfirmed = true
            userRepository.save(owner)

            mockMvc.perform(MockMvcRequestBuilders.get("/messages/${Random.nextInt()}")
                    .header("Authorization", "Bearer ${jwtUtil.generateJwt(userDetailsService.loadUserByUsername(ownerUsername))}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        @Test
        fun givenBlockedMessageId_thenReturn404() {
            val receiverUsername = "message_test_receiver"

            val receiver = User(receiverUsername, "John", "Doe", "message_test_getterr@message.com", passwordEncoder.encode("12345689"))
            receiver.isConfirmed = true
            userRepository.save(receiver)

            val sender = User("message_test_sender", "John", "Doe", "message_test_getterr@message.com", passwordEncoder.encode("12345689"))
            sender.isConfirmed = true
            userRepository.save(sender)

            val blockedMessage = Message("Hello World!", sender, receiver)
            blockedMessage.isFromBlockedUser = true
            val messageId = messageRepository.save(blockedMessage).id

            mockMvc.perform(MockMvcRequestBuilders.get("/messages/$messageId")
                    .header("Authorization", "Bearer ${jwtUtil.generateJwt(userDetailsService.loadUserByUsername(receiverUsername))}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        @Test
        fun givenValidId_thenReturn200() {
            val text = "Hello World!"
            val receiverUsername = "message_test_receiver"

            val receiver = User(receiverUsername, "John", "Doe", "message_test_getterr@message.com", passwordEncoder.encode("12345689"))
            receiver.isConfirmed = true
            userRepository.save(receiver)

            val sender = User("message_test_sender", "John", "Doe", "message_test_getterr@message.com", passwordEncoder.encode("12345689"))
            sender.isConfirmed = true
            userRepository.save(sender)

            val message = messageRepository.save(Message(text, sender, receiver))

            val messageResponse = mockMvc.perform(MockMvcRequestBuilders.get("/messages/${message.id}")
                    .header("Authorization", "Bearer ${jwtUtil.generateJwt(userDetailsService.loadUserByUsername(receiverUsername))}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andReturn().response.contentAsString

            assertThat(JsonPath.read<Int>(messageResponse, "$.message.id")).isEqualTo(message.id)
            assertThat(JsonPath.read<String>(messageResponse, "$.message.receiver.username")).isEqualTo(receiverUsername)
            assertThat(JsonPath.read<String>(messageResponse, "$.message.text")).isEqualTo(text)
        }

    }

    @AfterEach
    internal fun teardown() {
        verificationRepository.deleteAll()
        messageRepository.deleteAll()
        blockRepository.deleteAll()
        userRepository.deleteAll()
    }

}
