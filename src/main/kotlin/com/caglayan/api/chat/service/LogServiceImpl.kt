package com.caglayan.api.chat.service

import com.caglayan.api.chat.document.Log
import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.repository.LogRepository
import net.logstash.logback.argument.StructuredArguments
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class LogServiceImpl(val logRepository: LogRepository): LogService {

    override fun info(message: LogAction, context: Map<String, Any?>) {
        save(message.toString(), context)
        info(message.toString(), context)
    }

    override fun warn(message: LogAction, context: Map<String, Any?>) {
        save(message.toString(), context)
        warn(message.toString(), context)
    }

    override fun error(message: LogAction, context: Map<String, Any?>) {
        save(message.toString(), context)
        error(message.toString(), context)
    }

    private fun save(message: String, context: Map<String, Any?>) = logRepository.save(Log(message, context))

    private fun info(message: String, context: Map<String, Any?>) = LOGGER.info(message, StructuredArguments.keyValue("data", context))

    private fun warn(message: String, context: Map<String, Any?>) = LOGGER.warn(message, StructuredArguments.keyValue("data", context))

    private fun error(message: String, context: Map<String, Any?>) = LOGGER.error(message,  context)

    companion object {
        private val LOGGER = LogManager.getLogger(LogServiceImpl::class.java)
    }

}