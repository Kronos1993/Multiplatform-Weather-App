package com.kronos.multiplatform.weatherapp.core.logguer

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


const val LOG_FILE_NAME = "log.log"
const val LOG_FILE_URI = "weather-app/logs"

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

interface ILogManager {
    suspend fun log(level: LogLevel, tag: String, message: String)
    suspend fun getAllLogs(): List<String>
    suspend fun clearLogs()
}

expect class LogManager: ILogManager

@OptIn(ExperimentalTime::class)
fun formatLog(level: LogLevel, tag: String, message: String): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val timestamp = "${now.date} ${now.time}"
    return "[$timestamp] [${level.name}] [$tag] $message"
}

const val MAX_LOG_SIZE_BYTES = 10 * 1024 * 1024 // 10MB