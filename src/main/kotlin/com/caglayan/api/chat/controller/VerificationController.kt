package com.caglayan.api.chat.controller

import com.caglayan.api.chat.model.request.VerificationResendRequest
import com.caglayan.api.chat.model.response.VerificationResentResponse
import com.caglayan.api.chat.model.response.VerificationResponse
import com.caglayan.api.chat.service.VerificationService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping(path = ["verifications"])
class VerificationController(val verificationService: VerificationService) {

    @GetMapping
    fun verify(@RequestParam token: String): ResponseEntity<VerificationResponse> {
        verificationService.verify(token)

        return ResponseEntity.accepted().body(VerificationResponse())
    }

    @PostMapping("/resend")
    fun resend(@RequestBody @Validated request: VerificationResendRequest): ResponseEntity<VerificationResentResponse> {
        verificationService.resend(request.email)

        return ResponseEntity.ok().body(VerificationResentResponse())
    }

}