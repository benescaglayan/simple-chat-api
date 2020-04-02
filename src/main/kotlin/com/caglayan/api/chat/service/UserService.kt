package com.caglayan.api.chat.service

import com.caglayan.api.chat.entity.User
import java.time.LocalDateTime

interface UserService {

    fun checkIfUsernameOrEmailExists(username: String, email: String): Boolean

    fun getByEmail(email: String): User

    fun getByUsername(username: String): User

    fun getUnconfirmedUsersRegisteredBefore(date: LocalDateTime): Set<User>

    fun register(username: String, firstName: String, lastName: String, email: String, password: String): Long

    fun save(user: User): User

    fun deleteById(userId: Long)
}