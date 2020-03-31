package com.caglayan.api.chat.service

import com.caglayan.api.chat.entity.User

interface BlockService {


    fun block(blockerUsername: String, blockedUsername: String): Long

    fun unblock(blockerUsername: String, blockedUsername: String)

    fun isUserBlockedBy(blocked: User, blocker: User): Boolean

}