package com.caglayan.api.chat.service

import com.caglayan.api.chat.entity.User
import com.caglayan.api.chat.event.model.RegistrationCompletedEvent
import com.caglayan.api.chat.exception.UserNotFoundException
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(val userRepository: UserRepository, val eventBusService: EventBusService, val logService: LogService): UserService {

    override fun checkIfUsernameOrEmailExists(username: String, email: String) = userRepository.existsByUsernameOrEmail(username, email)

    override fun getByEmail(email: String) = userRepository.findByEmail(email) ?: throw UserNotFoundException(email = email)

    override fun getByUsername(username: String) = userRepository.findByUsername(username) ?: throw UserNotFoundException(username = username)

    override fun getById(id: Long) = userRepository.findById(id).orElseThrow { UserNotFoundException() }

    override fun register(username: String, firstName: String, lastName: String, email: String, password: String): Long {
        val user = save(User(username, firstName, lastName, email, password))

        logService.info(LogAction.USER_CREATED, mapOf("id" to user.id, "username" to username, "firstName" to firstName, "lastName" to lastName, "email" to email))

        eventBusService.send(RegistrationCompletedEvent(user))
        return user.id
    }

    override fun save(user: User) = userRepository.save(user)

}