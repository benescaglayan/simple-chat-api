package com.caglayan.api.chat.model.enum

enum class LogAction(private val value: String) {

    BLOCK_CREATED("block.created"),
    BLOCK_ALREADY_EXISTS("block.already.exists"),
    BLOCK_REQUEST("block.request"),
    BLOCK_NOT_FOUND("block.not.found"),
    UNBLOCK_REQUEST("unblock.request"),
    BLOCK_DELETED("block.deleted"),
    SELF_BLOCK("self.block"),

    DUE_VERIFICATION_JOB("due.verification.job"),

    EMAIL_SENT("email.sent"),

    EVENT_TRIGGERED("event.triggered"),

    JWT_VALIDATION_ERROR("jwt.validation.error"),

    INVALID_VERIFICATION_TOKEN("invalid.verification.token"),
    INVALID_AUTHENTICATION_TOKEN("invalid.authentication.token"),

    INVALID_REQUEST("invalid.request"),

    LOGIN_REQUEST("login.request"),
    LOGIN_SUCCESSFUL("login.successful"),

    MESSAGE_CREATED("message.created"),
    MESSAGE_SEND_REQUEST("message.send.request"),
    MESSAGE_NOT_FOUND("message.not.found"),

    USER_ALREADY_CONFIRMED("user.already.confirmed"),
    USER_ALREADY_EXISTS("user.already.exists"),
    USER_REGISTR_REQUEST("user.registration.request"),
    USER_CREATED("user.created"),
    USER_NOT_FOUND("user.not.found"),
    USER_CONFIRMED("user.confirmed"),
    USER_DELETED("user.deleted"),

    UNAUTHORIZED_REQUEST("unauthorized.request"),

    VERIFICATION_DELETED("verification.deleted"),
    VERIFICATION_GENERATED("verification.generated"),
    VERIFICATION_RESEND_REQUEST("verification.resend.request"),
    VERIFICATION_REQUEST("verification.request"),
    VERIFICATION_SENT("verification.sent");

    override fun toString() = value
}