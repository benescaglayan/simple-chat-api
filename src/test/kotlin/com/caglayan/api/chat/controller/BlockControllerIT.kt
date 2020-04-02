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
import com.caglayan.api.chat.util.Random
import com.google.gson.Gson
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
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

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BlockControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userDetailsService: UserDetailsService

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
    inner class Block {

        @Test
        fun givenEmptyToken_thenReturn403() {
            mockMvc.perform(MockMvcRequestBuilders.post("/blocks/random_username")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun givenInvalidToken_thenReturn403() {
            val blocker = User("blocker", "John", "Doe", "block_test@block.com", passwordEncoder.encode("12345689"))
            blocker.isConfirmed = true
            userRepository.save(blocker)

            val token = jwtUtil.generateJwt(userDetailsService.loadUserByUsername(blocker.username))

            blocker.isConfirmed = false
            userRepository.save(blocker)

            mockMvc.perform(MockMvcRequestBuilders.post("/blocks/random_username")
                    .header("Authorization", "Bearer $token")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized)
        }

        @Test
        fun whenSelfBlocking_thenReturn422() {
            val blockerUsername = "blocker"

            val blocker = User("blocker", "John", "Doe", "blocker_test@block.com", passwordEncoder.encode("12345689"))
            blocker.isConfirmed = true
            userRepository.save(blocker)

            val token = jwtUtil.generateJwt(userDetailsService.loadUserByUsername(blockerUsername))

            mockMvc.perform(MockMvcRequestBuilders.post("/blocks/$blockerUsername")
                    .header("Authorization", "Bearer $token")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
        }

        @Test
        fun givenAlreadyBlockedUser_thenReturn208() {
            val blocker = User("blocker", "John", "Doe", "blocker_test@block.com", passwordEncoder.encode("12345689"))
            blocker.isConfirmed = true
            userRepository.save(blocker)

            val blocked = User("blocked", "John", "Doe", "blocked_test@block.com", passwordEncoder.encode("12345689"))
            blocked.isConfirmed = true
            userRepository.save(blocked)

            val token = jwtUtil.generateJwt(userDetailsService.loadUserByUsername(blocker.username))

            mockMvc.perform(MockMvcRequestBuilders.post("/blocks/${blocked.username}")
                    .header("Authorization", "Bearer $token")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated)

            mockMvc.perform(MockMvcRequestBuilders.post("/blocks/${blocked.username}")
                    .header("Authorization", "Bearer $token")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isAlreadyReported)
        }

        @Test
        fun givenValidTokenAndInvalidUsername_thenReturn404() {
            val blocker = User("blocker", "John", "Doe", "block_test@block.com", passwordEncoder.encode("12345689"))
            blocker.isConfirmed = true
            userRepository.save(blocker)

            mockMvc.perform(MockMvcRequestBuilders.post("/blocks/random_username")
                    .header("Authorization", "Bearer ${jwtUtil.generateJwt(userDetailsService.loadUserByUsername(blocker.username))}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        @Test
        fun givenValidTokenAndUsername_thenReturn201() {
            val blocker = User("blocker", "John", "Doe", "blocker_test@block.com", passwordEncoder.encode("12345689"))
            blocker.isConfirmed = true
            userRepository.save(blocker)

            val blocked = User("blocked", "John", "Doe", "blocked_test@block.com", passwordEncoder.encode("12345689"))
            blocked.isConfirmed = true
            userRepository.save(blocked)

            val createdBlockResponse = mockMvc.perform(MockMvcRequestBuilders.post("/blocks/${blocked.username}")
                    .header("Authorization", "Bearer ${jwtUtil.generateJwt(userDetailsService.loadUserByUsername(blocker.username))}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated)
                    .andExpect(MockMvcResultMatchers.header().exists("Location"))
                    .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.containsString("blocks/")))
                    .andReturn().response.getHeaderValue("Location")

            val blockId = createdBlockResponse.toString().substringAfter("/").toLong()

            val block = blockRepository.findById(blockId).get()

            assertThat(block).isNotNull
            assertThat(block.blocker.username).isEqualTo(blocker.username)
            assertThat(block.blocked.username).isEqualTo(blocked.username)
        }

    }

    @Nested
    inner class Unblock {

        @Test
        fun givenEmptyToken_thenReturn403() {
            mockMvc.perform(MockMvcRequestBuilders.delete("/blocks/random_username")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun givenInvalidToken_thenReturn403() {
            val blocker = User("blocker", "John", "Doe", "block_test@block.com", passwordEncoder.encode("12345689"))
            blocker.isConfirmed = true
            userRepository.save(blocker)

            val token = jwtUtil.generateJwt(userDetailsService.loadUserByUsername(blocker.username))

            blocker.isConfirmed = false
            userRepository.save(blocker)

            mockMvc.perform(MockMvcRequestBuilders.delete("/blocks/random_username")
                    .header("Authorization", "Bearer $token")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized)
        }

        @Test
        fun givenValidTokenAndInvalidUsername_thenReturn404() {
            val blocker = User("blocker", "John", "Doe", "block_test@block.com", passwordEncoder.encode("12345689"))
            blocker.isConfirmed = true
            userRepository.save(blocker)

            mockMvc.perform(MockMvcRequestBuilders.delete("/blocks/random_username")
                    .header("Authorization", "Bearer ${jwtUtil.generateJwt(userDetailsService.loadUserByUsername(blocker.username))}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        @Test
        fun givenValidTokenAndUsername_thenReturn201() {
            val blocker = User("blocker", "John", "Doe", "blocker_test@block.com", passwordEncoder.encode("12345689"))
            blocker.isConfirmed = true
            userRepository.save(blocker)

            val blocked = User("blocked", "John", "Doe", "blocked_test@block.com", passwordEncoder.encode("12345689"))
            blocked.isConfirmed = true
            userRepository.save(blocked)

            blockRepository.save(Block(blocker, blocked)).id

            mockMvc.perform(MockMvcRequestBuilders.delete("/blocks/${blocked.username}")
                    .header("Authorization", "Bearer ${jwtUtil.generateJwt(userDetailsService.loadUserByUsername(blocker.username))}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk)

            assertFalse(blockRepository.existsByBlockerAndBlocked(blocker, blocked))
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
