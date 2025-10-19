package com.kronos.multiplatform.weatherapp.core.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kronos.multiplatform.weatherapp.MainActivity
import com.kronos.multiplatform.weatherapp.NOTIFICATION_CHANNEL
import com.kronos.multiplatform.weatherapp.R

actual class AppNotification(
    private val context: Context
) : INotifications {

    override fun createNotification(
        title: String,
        description: String,
        group: NotificationGroup,
        notificationsId: NotificationType,
    ) {
        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)

        val intent = Intent(context, MainActivity::class.java)
        /*val bundle = Bundle()
        bundle.putInt("go_to", R.id.navigation_notifications)
        intent.putExtras(bundle)*/
        intent.action = "notificaciones"
        val pendingIntent: PendingIntent? =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)

        val notification: Notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_weather_app_icon)
            .setContentTitle(title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(description)
                    .setBigContentTitle(title)
            )
            .setContentText(
                description.take(50)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setGroup(group.name)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        notificationManager.notify(notificationsId.ordinal, notification)
    }

    override fun hideNotification(notificationType: NotificationType) {
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)!!
        notificationManager.cancel(notificationType.ordinal)
    }
}