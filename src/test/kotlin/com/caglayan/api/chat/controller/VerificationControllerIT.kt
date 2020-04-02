package com.caglayan.api.chat.controller

import com.caglayan.api.chat.config.AppConfig
import com.caglayan.api.chat.entity.User
import com.caglayan.api.chat.entity.Verification
import com.caglayan.api.chat.event.model.UserVerifiedEvent
import com.caglayan.api.chat.event.model.VerificationRequestedEvent
import com.caglayan.api.chat.repository.UserRepository
import com.caglayan.api.chat.repository.VerificationRepository
import com.caglayan.api.chat.service.EventBusService
import com.caglayan.api.chat.service.MailService
import com.caglayan.api.chat.util.Date
import com.caglayan.api.chat.util.Random
import com.google.gson.Gson
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.task.SyncTaskExecutor
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
class VerificationControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    var gson = Gson()

    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var verificationRepository: VerificationRepository

    @SpykBean
    lateinit var eventBus: EventBusService

    val userVerifiedEventSlot = slot<UserVerifiedEvent>()

    val verificationRequestedEventSlot = slot<VerificationRequestedEvent>()

    @MockkBean
    lateinit var mailService: MailService

    private val passwordEncoder = BCryptPasswordEncoder(12)

    @BeforeAll
    fun setup() {
        every { mailService.send(any(), any(), any()) } returns Unit
    }

    @Nested
    inner class Verify {

        @Test
        fun givenValidRequest_thenReturn200() {
            val user = userRepository.save(User("verify_test", "John", "Doe", "verify_test@verification.com", passwordEncoder.encode("12345689")))
            val verification = verificationRepository.save(Verification(user))

            mockMvc.perform(MockMvcRequestBuilders.get("/verifications/?token=${verification.token}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isAccepted)

            verify(timeout = 2000) { eventBus.send(capture(userVerifiedEventSlot)) }

            assertTrue(userRepository.findByUsername(user.username)!!.isConfirmed)
        }

        @Test
        fun givenNonExistingToken_thenReturn400() {
            mockMvc.perform(MockMvcRequestBuilders.get("/verifications/?token=${Random.uuid()}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        fun givenExpiredToken_thenReturn400() {
            val user = userRepository.save(User("verify_test", "John", "Doe", "verify_test@verification.com", passwordEncoder.encode("12345689")))
            val verification = Verification(user)
            verification.sentAt = Date.now().minusHours(appConfig.verificationThreshold.toLong())
            verificationRepository.save(verification)

            mockMvc.perform(MockMvcRequestBuilders.get("/verifications/?token=${verification.token}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

    }

    @Nested
    inner class Resend {

        @Test
        fun givenInvalidRequest_thenReturn404() {
            val resendBody = mapOf(
                    "email" to "resend_test@verification.com"
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/verifications/resend")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(resendBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        @Test
        fun givenConfirmedUser_thenReturn208() {
            val email = "resend_test@verification.com"

            val user = User("resend_test", "John", "Doe", email, passwordEncoder.encode("12345689"))
            user.isConfirmed = true
            userRepository.save(user)

            val resendBody = mapOf(
                    "email" to email
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/verifications/resend")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(resendBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isAlreadyReported)
        }

        @Test
        fun givenValidRequest_thenReturn200() {
            val email = "resend_test@verification.com"

            val user = userRepository.save(User("resend_test", "John", "Doe", email, passwordEncoder.encode("12345689")))
            verificationRepository.save(Verification(user))

            val resendBody = mapOf(
                    "email" to email
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/verifications/resend")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(resendBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk)

            verify(timeout = 2000) { eventBus.send(capture(verificationRequestedEventSlot)) }
        }

    }

    @TestConfiguration
    class TaskExecutorConfig {

        @Bean
        @Primary
        fun taskExecutor() = SyncTaskExecutor()

    }

    @AfterEach
    internal fun teardown() {
        verificationRepository.deleteAll()
        userRepository.deleteAll()
    }

}
