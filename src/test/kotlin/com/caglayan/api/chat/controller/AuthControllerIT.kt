package com.caglayan.api.chat.controller

import com.caglayan.api.chat.entity.User
import com.caglayan.api.chat.event.model.VerificationRequestedEvent
import com.caglayan.api.chat.repository.UserRepository
import com.caglayan.api.chat.repository.VerificationRepository
import com.caglayan.api.chat.service.EventBusService
import com.caglayan.api.chat.service.MailService
import com.caglayan.api.chat.util.JwtUtil
import com.google.gson.Gson
import com.jayway.jsonpath.JsonPath
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    var gson = Gson()

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var verificationRepository: VerificationRepository

    @Autowired
    lateinit var jwtUtil: JwtUtil

    @SpykBean
    lateinit var eventBus: EventBusService

    @MockkBean
    lateinit var mailService: MailService

    val slot = slot<VerificationRequestedEvent>()

    private val passwordEncoder = BCryptPasswordEncoder(12)

    @BeforeAll
    fun setup() {
        every { mailService.send(any(), any(), any()) } returns Unit
    }

    @Nested
    inner class Register {

        @Test
        fun givenInvalidRequest_thenReturn400() {
            var registerBody = mapOf(
                    "email" to "register_test0@auth.com",
                    "username" to "register_test",
                    "password" to "123456",
                    "firstName" to "John",
                    "lastName" to "Doe"
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(registerBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)

            registerBody = mapOf(
                    "email" to "register_test0@auth.com",
                    "password" to "12345678"
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(registerBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        fun givenExistingUsernameOrEmail_thenReturn409() {
            val email = "register_test@auth.com"
            val username = "register_test"
            val password = "12345689"
            val firstName = "John"
            val lastName = "Doe"

            val user = User(username, firstName, lastName, email, passwordEncoder.encode(password))
            user.isConfirmed = true
            userRepository.save(user)

            var registerBody = mapOf(
                    "email" to "register_test1@auth.com",
                    "username" to username,
                    "password" to password,
                    "firstName" to firstName,
                    "lastName" to lastName
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(registerBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isConflict)

            registerBody = mapOf(
                    "email" to email,
                    "username" to "register_test1",
                    "password" to password,
                    "firstName" to firstName,
                    "lastName" to lastName
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(registerBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isConflict)
        }

        @Test
        fun givenValidRequest_thenReturn201() {
            val email = "register_test@auth.com"
            val username = "register_test"
            val password = "12345689"
            val firstName = "John"
            val lastName = "Doe"

            val registerBody = mapOf(
                    "email" to email,
                    "username" to username,
                    "password" to password,
                    "firstName" to firstName,
                    "lastName" to lastName
            )

            val registeredUserLocation = mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(registerBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated)
                    .andExpect(header().exists("Location"))
                    .andExpect(header().string("Location", Matchers.containsString("users/")))
                    .andReturn().response.getHeaderValue("Location")

            verify(timeout = 2000) { eventBus.send(capture(slot)) }

            val userId = registeredUserLocation.toString().substringAfter("/").toLong()

            val user = userRepository.findByEmail(email)

            assertThat(user).isNotNull
            assertThat(user!!.id).isEqualTo(userId)
            assertFalse(user.isConfirmed)
            assertThat(user.firstName).isEqualTo(firstName)
            assertThat(user.lastName).isEqualTo(lastName)
            assertTrue(passwordEncoder.matches(password, user.password))
        }

    }

    @Nested
    inner class Login {

        @Test
        fun givenInvalidRequest_thenReturn400() {
            var loginBody = mapOf(
                    "username" to "login_test"
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(loginBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)

            loginBody = mapOf(
                    "password" to "12345678"
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(loginBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        fun givenUnconfirmedUser_thenReturn401() {
            val email = "login_test@auth.com"
            val username = "login_test"
            val password = "12345689"
            val firstName = "John"
            val lastName = "Doe"

            userRepository.save(User(username, firstName, lastName, email, passwordEncoder.encode(password)))

            val loginBody = mapOf(
                    "username" to username,
                    "password" to password
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(loginBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized)
        }

        @Test
        fun givenInvalidPassword_thenReturn401() {
            val email = "login_test@auth.com"
            val username = "login_test"
            val password = "12345689"
            val firstName = "John"
            val lastName = "Doe"

            val user = User(username, firstName, lastName, email, passwordEncoder.encode(password))
            user.isConfirmed = true
            userRepository.save(user)

            val loginBody = mapOf(
                    "username" to username,
                    "password" to password.plus(password)
            )

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(loginBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized)
        }

        @Test
        fun givenValidRequest_thenReturn200() {
            val email = "login_test@auth.com"
            val username = "login_test"
            val password = "12345689"
            val firstName = "John"
            val lastName = "Doe"

            val user = User(username, firstName, lastName, email, passwordEncoder.encode(password))
            user.isConfirmed = true
            userRepository.save(user)

            val loginBody = mapOf(
                    "username" to username,
                    "password" to password
            )

            val loginResponseBody = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(loginBody))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andReturn().response.contentAsString

            val token = JsonPath.read<String>(loginResponseBody, "$.token")

            assertThat(username).isEqualTo(jwtUtil.getUsernameFromJwt(token))
        }

    }

    @AfterEach
    internal fun teardown() {
        verificationRepository.deleteAll()
        userRepository.deleteAll()
    }

}
