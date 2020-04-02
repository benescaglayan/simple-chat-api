package com.caglayan.api.chat.service

import com.caglayan.api.chat.entity.User

interface UserService {

    fun checkIfUsernameOrEmailExists(username: String, email: String): Boolean

    fun getByEmail(email: String): User

    fun getByUsername(username: String): User

    fun register(username: String, firstName: String, lastName: String, email: String, password: String): Long

    fun save(user: User): User
}