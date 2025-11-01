package com.kronos.multiplatform.weatherapp.core.logguer

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

actual class LogManager : ILogManager {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun log(
        level: LogLevel,
        tag: String,
        message: String
    ) {
        val fileManager = NSFileManager.defaultManager
        val logPath = getLogFile()

        try {
            if (fileManager.fileExistsAtPath(logPath)) {
                val attrs = fileManager.attributesOfItemAtPath(logPath, null)
                val fileSize = attrs?.get(NSFileSize) as? NSNumber
                if (fileSize != null && fileSize.longLongValue >= MAX_LOG_SIZE_BYTES) {
                    fileManager.removeItemAtPath(logPath, null)
                }
            }

            val line = formatLog(level, tag, message)
            val existing = try {
                NSString.stringWithContentsOfFile(logPath, NSUTF8StringEncoding, null) ?: ""
            } catch (_: Exception) {
                ""
            }
            val updated = existing + "$line\n"
            updated.writeTextToFile(logPath)

        } catch (e: Exception) {
            println("Error writing log: ${e.message}")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getAllLogs(): List<String> {
        val content = try {
            NSString.stringWithContentsOfFile(getLogFile(), NSUTF8StringEncoding, null)
        } catch (_: Exception) {
            null
        }
        return content?.lines() ?: emptyList()
    }

    override suspend fun clearLogs() {
        "".writeTextToFile(getLogFile())
    }



    @OptIn(ExperimentalForeignApi::class)
    private fun getLogsDir(): String {
        val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        val documentsDirectory = paths.firstOrNull()?.toString() ?: ""
        val logsPath = "$documentsDirectory/$LOG_FILE_URI"
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(logsPath)) {
            fileManager.createDirectoryAtPath(
                logsPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
        return logsPath
    }

    private fun getLogFile(): String = "${getLogsDir()}/$LOG_FILE_NAME"


    private fun String.writeTextToFile(path: String) {
        val data = this.encodeToByteArray().toNSData()
        data.writeToFile(path, atomically = true)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData {
        return this.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), this.size.toULong())
        }
    }


}