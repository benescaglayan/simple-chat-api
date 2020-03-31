package com.caglayan.api.chat.service

import com.caglayan.api.chat.exception.UserAlreadyExistsException
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.model.request.RegistrationRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(val userService: UserService, val logService: LogService,
                      val passwordEncoder: BCryptPasswordEncoder): AuthService {

    override fun register(request: RegistrationRequest): Long {
        logService.info(LogAction.USER_REGISTR_REQUEST,
                mapOf("username" to request.username, "firstName" to request.firstName, "lastName" to request.lastName, "email" to request.email))

        if (userService.checkIfUsernameOrEmailExists(request.username, request.email)) {
            throw UserAlreadyExistsException(username = request.username, email = request.email)
        }

        return userService.register(request.username, request.firstName, request.lastName, request.email, passwordEncoder.encode(request.password))
    }

}