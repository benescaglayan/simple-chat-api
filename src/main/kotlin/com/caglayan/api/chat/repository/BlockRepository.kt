package com.caglayan.api.chat.repository

import com.caglayan.api.chat.entity.Block
import com.caglayan.api.chat.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BlockRepository : JpaRepository<Block, Long> {

    fun existsByBlockerAndBlocked(blocker: User, blocked: User): Boolean

    fun findByBlockerUsernameAndBlockedUsername(blockerUsername: String, blockedUsername: String): Block?

}