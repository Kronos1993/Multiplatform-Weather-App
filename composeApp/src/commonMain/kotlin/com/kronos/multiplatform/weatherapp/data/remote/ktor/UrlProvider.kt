package com.kronos.multiplatform.weatherapp.data.remote.ktor

interface UrlProvider {
    fun getPublicApiUrl():String
    fun getPrivateApiUrl():String
    fun getServerUrl():String
    fun getImageUrl(partUrl:String,quality:String):String

    fun extractIdFromUrl(url:String):Int
}