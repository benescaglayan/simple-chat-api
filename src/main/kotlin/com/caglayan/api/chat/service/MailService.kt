package com.caglayan.api.chat.service

interface MailService {

    fun send(to: String, subject: String, content: String)

}