package com.kronos.multiplatform.weatherapp.core.util

interface IAppInfo{
    fun getAppVersion():String
}

expect class AppInfo: IAppInfo