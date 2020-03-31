package com.caglayan.api.chat.service

import com.caglayan.api.chat.exception.UnauthorizedRequestException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.security.core.userdetails.User as CoreUser

@Service
class UserDetailsService(val userService: UserService) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userService.getByUsername(username)
        if (!user.isConfirmed) {
            throw UnauthorizedRequestException(action = "unconfirmed user login", data = username)
        }

        return CoreUser
                .withUsername(user.username)
                .password(user.password)
                .roles("USER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build()
    }

}