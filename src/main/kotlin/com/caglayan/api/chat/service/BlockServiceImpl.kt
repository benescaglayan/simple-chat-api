package com.caglayan.api.chat.service

import com.caglayan.api.chat.entity.Block
import com.caglayan.api.chat.entity.User
import com.caglayan.api.chat.exception.BlockNotFoundException
import com.caglayan.api.chat.exception.UserNotFoundException
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.repository.BlockRepository
import org.springframework.stereotype.Service

@Service
class BlockServiceImpl(val blockRepository: BlockRepository, val userService: UserService, val logService: LogService): BlockService {

    override fun block(blockerUsername: String, blockedUsername: String): Long {
        logService.info(LogAction.BLOCK_REQUEST, mapOf("blocker" to blockerUsername, "blocked" to blockedUsername))

        val blocked = userService.getByUsername(blockedUsername)
        if (!blocked.isConfirmed) {
            throw UserNotFoundException()
        }

        val blocker = userService.getByUsername(blockerUsername)

        return block(blocker, blocked)
    }

    override fun unblock(blockerUsername: String, blockedUsername: String) {
        logService.info(LogAction.UNBLOCK_REQUEST, mapOf("blocker" to blockerUsername, "blocked" to blockedUsername))

        val block = blockRepository.findByBlockerUsernameAndBlockedUsername(blockerUsername, blockedUsername) ?: throw BlockNotFoundException(blocker = blockerUsername, blocked = blockedUsername)
        if (block.blocker.username != blockerUsername) {
            throw BlockNotFoundException(blocker = blockerUsername, blocked = blockedUsername)
        }

        delete(block)
        logService.info(LogAction.BLOCK_DELETED, mapOf("blocker" to blockerUsername, "blocked" to block.blocked.username))
    }

    override fun isUserBlockedBy(blocked: User, blocker: User) = blockRepository.existsByBlockerAndBlocked(blocker, blocked)

    private fun block(blocker: User, blocked: User): Long {
        val block = save(Block(blocker, blocked))
        logService.info(LogAction.BLOCK_CREATED, mapOf("id" to block.id, "blocker" to blocker.username, "blocked" to blocked.username))

        return block.id
    }

    private fun save(block: Block) = blockRepository.save(block)

    private fun delete(block: Block) = blockRepository.delete(block)

}