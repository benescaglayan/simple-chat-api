package com.caglayan.api.chat.controller

import com.caglayan.api.chat.model.response.BlockResponse
import com.caglayan.api.chat.model.response.UnblockResponse
import com.caglayan.api.chat.service.BlockService
import com.caglayan.api.chat.util.JwtUtil
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.net.URI


@RestController
@RequestMapping(path = ["blocks"])
class BlockController(val blockService: BlockService, jwtUtil: JwtUtil) : BaseController(jwtUtil) {

    @PostMapping("/{username}")
    @Secured("ROLE_USER")
    fun block(@PathVariable username: String): ResponseEntity<BlockResponse> {
        val blockId = blockService.block(getAuthenticatedUsername(), username)

        return ResponseEntity.created(URI.create("blocks/$blockId")).body(BlockResponse())
    }

    @DeleteMapping("/{username}")
    @Secured("ROLE_USER")
    fun unblock(@PathVariable username: String): ResponseEntity<UnblockResponse> {
        blockService.unblock(getAuthenticatedUsername(), username)

        return ResponseEntity.ok().body(UnblockResponse())
    }

}