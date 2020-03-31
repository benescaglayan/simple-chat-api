package com.caglayan.api.chat.controller

import com.caglayan.api.chat.exception.UnauthorizedRequestException
import com.caglayan.api.chat.model.request.LoginRequest
import com.caglayan.api.chat.model.request.RegistrationRequest
import com.caglayan.api.chat.model.response.LoginResponse
import com.caglayan.api.chat.model.response.RegistrationResponse
import com.caglayan.api.chat.service.AuthService
import com.caglayan.api.chat.service.UserDetailsService
import com.caglayan.api.chat.util.JwtUtil
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI


@RestController
@RequestMapping(path = ["auth"])
class AuthController(val authService: AuthService, val userDetailsService: UserDetailsService, val authenticationManager: AuthenticationManager,
                     jwtUtil: JwtUtil): BaseController(jwtUtil) {

    @PostMapping("/register")
    fun register(@RequestBody @Validated request: RegistrationRequest): ResponseEntity<RegistrationResponse> {
        val userId = authService.register(request)

        return ResponseEntity.created(URI.create("users/$userId")).body(RegistrationResponse())
    }

    @PostMapping("/login")
    fun login(@RequestBody @Validated request: LoginRequest): ResponseEntity<LoginResponse> {
        authenticate(request.username, request.password)

        val userDetails = userDetailsService.loadUserByUsername(request.username)
        val token = jwtUtil.generateJwt(userDetails)

        return ResponseEntity.ok().body(LoginResponse(token))
    }

    private fun authenticate(username: String, password: String) {
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
        }  catch (e: BadCredentialsException) {
            throw UnauthorizedRequestException(action = "invalid login credentials", data = username)
        }
    }

}