package com.kronos.multiplatform.weatherapp.core.logguer

class DummyLogger: ILogManager {
    override suspend fun log(
        level: LogLevel,
        tag: String,
        message: String
    ) {

    }

    override suspend fun getAllLogs(): List<String> {
        return emptyList()
    }

    override suspend fun clearLogs() {
    }
}