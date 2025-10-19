package com.kronos.multiplatform.weatherapp.data.remote.ktor

class UrlProviderImp (
) : UrlProvider {
    override fun getPublicApiUrl(): String {
        return UrlConstants.API
    }

    override fun getPrivateApiUrl(): String {
        return UrlConstants.API
    }

    override fun getServerUrl(): String {
        return UrlConstants.API
    }

    override fun getImageUrl(partUrl: String,quality:String): String {
        return UrlConstants.HTTP + if (quality == "low") partUrl else partUrl.replace("64x64","128x128")
    }

    override fun extractIdFromUrl(url: String): Int {
        return "/-?[0-9]+/$".toRegex().find(url)!!.value.filter { it.isDigit() || it == '-' }
            .toInt()
    }
}