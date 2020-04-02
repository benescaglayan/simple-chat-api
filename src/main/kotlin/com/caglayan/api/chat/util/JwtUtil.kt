package com.caglayan.api.chat.util

import com.caglayan.api.chat.config.JwtConfig
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.service.LogService
import io.jsonwebtoken.*
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*
import java.util.Date


@Component
class JwtUtil(val jwtConfig: JwtConfig, val logService: LogService) : Serializable {

    fun generateJwt(userDetails: UserDetails): String {
        val claims: Map<String, Any> = HashMap()
        val expiryDate = Date(Date().time + jwtConfig.expiration.toLong() * 60 * 1000 )

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.username)
                .setIssuedAt(Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtConfig.secret)
                .compact()
    }

    fun getUsernameFromJwt(token: String?): String? {
        val claims = Jwts.parser()
                .setSigningKey(jwtConfig.secret)
                .parseClaimsJws(token)
                .body

        return claims.subject
    }

    fun validateJwt(token: String?): Boolean {
        try {
            Jwts.parser().setSigningKey(jwtConfig.secret).parseClaimsJws(token)
            return true
        } catch (ex: SignatureException) {
            logService.error(LogAction.JWT_VALIDATION_ERROR, mapOf("token" to token, "reason" to "Invalid JWT signature"))
        } catch (ex: MalformedJwtException) {
            logService.error(LogAction.JWT_VALIDATION_ERROR,mapOf("token" to token, "reason" to "Invalid JWT token"))
        } catch (ex: ExpiredJwtException) {
            logService.error(LogAction.JWT_VALIDATION_ERROR,mapOf("token" to token, "reason" to "Expired JWT token"))
        } catch (ex: UnsupportedJwtException) {
            logService.error(LogAction.JWT_VALIDATION_ERROR,mapOf("token" to token, "reason" to "Unsupported JWT token"))
        } catch (ex: IllegalArgumentException) {
            logService.error(LogAction.JWT_VALIDATION_ERROR,mapOf("token" to token, "reason" to "JWT claims string is empty."))
        }

        return false
    }
}