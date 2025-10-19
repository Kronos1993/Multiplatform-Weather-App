package com.kronos.multiplatform.weatherapp.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

actual class ExpectedIntents(private val context: Context) : IExpectedIntents {
    override fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ContextCompat.startActivity(context, intent, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun makeCall(phone: String) {
        if (phone.isBlank()) return

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$phone".toUri()
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    override fun sendEmail(email: String) {
        if (email.isBlank()) return

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        }

        try {
            val chooserIntent = Intent.createChooser(intent, "Choose an Email client").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to find an email client", Toast.LENGTH_SHORT).show()
        }
    }
}