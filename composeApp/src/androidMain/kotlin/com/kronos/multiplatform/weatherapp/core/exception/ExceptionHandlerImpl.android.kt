package com.kronos.multiplatform.weatherapp.core.exception

import android.content.Context
import android.util.Log
import com.kronos.multiplatform.weatherapp.core.logguer.ILogManager
import com.kronos.multiplatform.weatherapp.core.logguer.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

actual class ExceptionHandlerImpl(
    private val context: Context,
    var logguer: ILogManager
) : ExceptionHandler,Thread.UncaughtExceptionHandler {

    private val TAG = "Weather-App Exception"

    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    override fun init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e(this.javaClass.name, "uncaughtException: ", e)
        runBlocking(Dispatchers.IO) {
            logguer.log(LogLevel.ERROR, TAG, "uncaughtException: ${e.message.orEmpty()}")
        }
    }
}