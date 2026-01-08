package com.kronos.multiplatform.weatherapp.core.logguer

import android.os.Environment
import java.io.File

actual class LogManager : ILogManager {
    override suspend fun log(
        level: LogLevel,
        tag: String,
        message: String
    ) {
        try {
            val file = getLogFile()

            // Si el archivo ya es mayor a 10MB, se borra y se crea uno nuevo
            if (file.exists() && file.length() >= MAX_LOG_SIZE_BYTES) {
                file.delete()
                file.createNewFile()
            }

            val line = formatLog(level, tag, message)
            file.appendText("$line\n")
        } catch (e: Exception) {
            println("Error writing log: ${e.message}")
        }
    }

    override suspend fun getAllLogs(): List<String> {
        return try {
            getLogFile().readLines()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun clearLogs() {
        try {
            getLogFile().writeText("")
        } catch (_: Exception) {}
    }

    private fun getLogsDir(): File {
        val dir = File(
            Environment.getExternalStorageDirectory(),
            LOG_FILE_URI
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getLogFile(): File = File(getLogsDir(), LOG_FILE_NAME)


}