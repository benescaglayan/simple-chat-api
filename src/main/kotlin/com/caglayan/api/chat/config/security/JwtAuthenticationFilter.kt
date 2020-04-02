package com.caglayan.api.chat.config.security

import com.caglayan.api.chat.config.JwtConfig
import com.caglayan.api.chat.exception.UnauthorizedRequestException
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.service.LogService
import com.caglayan.api.chat.service.UserDetailsService
import com.caglayan.api.chat.util.JwtUtil
import com.google.gson.Gson
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class JwtAuthenticationFilter(val jwtConfig: JwtConfig, val userDetailsService: UserDetailsService,
                              val logService: LogService, val jwtUtil: JwtUtil): OncePerRequestFilter() {

    private val gson = Gson()

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val requestTokenHeader = request.getHeader(jwtConfig.header)
        var username: String? = null
        var jwtToken: String? = null

        if (requestTokenHeader != null && requestTokenHeader.startsWith(jwtConfig.prefix)) {
            jwtToken = requestTokenHeader.substring(7)

            try {
                username = jwtUtil.getUsernameFromJwt(jwtToken)
            } catch (ex: IllegalArgumentException) {
                logService.error(LogAction.UNAUTHORIZED_REQUEST, mapOf("reason" to ex.message, "data" to ex.localizedMessage))

                response.status = 401
                response.contentType = "application/json"
                response.writer.write(gson.toJson(mapOf("message" to "Invalid Authentication Token")))

                return
            } catch (ex: ExpiredJwtException) {
                logService.error(LogAction.UNAUTHORIZED_REQUEST, mapOf("reason" to ex.message, "data" to ex.localizedMessage))

                response.status = 401
                response.contentType = "application/json"
                response.writer.write(gson.toJson(mapOf("message" to "Invalid Authentication Token")))

                return
            }
        }

        if (username != null) {
            val userDetails: UserDetails
            try {
                userDetails = userDetailsService.loadUserByUsername(username)
            } catch (ex: UnauthorizedRequestException) {
                logService.error(LogAction.UNAUTHORIZED_REQUEST, mapOf("reason" to ex.action, "data" to ex.data))

                response.status = 401
                response.contentType = "application/json"
                response.writer.write(gson.toJson(mapOf("message" to ex.message)))

                return
            }

            if (jwtUtil.validateJwt(jwtToken)) {
                val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)

                usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
            }
        }

        chain.doFilter(request, response)
    }
}