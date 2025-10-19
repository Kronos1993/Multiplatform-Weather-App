package com.kronos.multiplatform.weatherapp.core.util

import platform.Foundation.NSLog
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class ExpectedIntents : IExpectedIntents{
    override fun openBrowser(url: String) {
        val nsUrl = NSURL(string = url)
        val application = UIApplication.sharedApplication
        if (application.canOpenURL(nsUrl)) {
            application.openURL(
                url = nsUrl,
                options = mapOf<Any?, Any>(),
                completionHandler = null
            )
        }
    }

    override fun makeCall(phone: String) {
        val telUrl = NSURL(string = "tel:$phone")
        val application = UIApplication.sharedApplication
        if (application.canOpenURL(telUrl)) {
            application.openURL(
                url = telUrl,
                options = mapOf<Any?, Any>(),
                completionHandler = { success ->
                    if (!success) {
                        NSLog("Failed to initiate call to: $phone")
                    }
                }
            )
        } else {
            NSLog("Cannot make call. Invalid phone number or no supported application: $phone")
        }
    }

    override fun sendEmail(email: String) {
        val mailtoUrl = NSURL(string = "mailto:$email")
        val application = UIApplication.sharedApplication
        if (application.canOpenURL(mailtoUrl)) {
            application.openURL(
                url = mailtoUrl,
                options = mapOf<Any?, Any>(),
                completionHandler = { success ->
                    if (!success) {
                        NSLog("Failed to open email client for: $email")
                    }
                }
            )
        } else {
            NSLog("Cannot send email. Invalid email address or no supported application: $email")
        }
    }
}