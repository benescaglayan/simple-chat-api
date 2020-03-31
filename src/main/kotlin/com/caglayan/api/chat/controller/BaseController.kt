package com.caglayan.api.chat.controller


import com.caglayan.api.chat.exception.InvalidAuthenticationTokenException
import com.caglayan.api.chat.util.JwtUtil
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

open class BaseController(val jwtUtil: JwtUtil) {

    private fun getAuthenticatedUserDetails(): UserDetails {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            throw InvalidAuthenticationTokenException(principal = authentication.principal.toString())
        }

        return authentication.principal as UserDetails
    }

    protected fun getAuthenticatedUsername() = getAuthenticatedUserDetails().username


}
