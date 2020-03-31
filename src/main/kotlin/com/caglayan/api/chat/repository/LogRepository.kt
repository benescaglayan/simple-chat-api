package com.caglayan.api.chat.repository

import com.caglayan.api.chat.document.Log
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface LogRepository: MongoRepository<Log, String>