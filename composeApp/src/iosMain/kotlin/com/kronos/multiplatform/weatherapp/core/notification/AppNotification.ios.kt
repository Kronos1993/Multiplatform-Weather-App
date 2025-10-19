package com.kronos.multiplatform.weatherapp.core.notification

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual class AppNotification : INotifications {

    override fun createNotification(
        title: String,
        description: String,
        group: NotificationGroup,
        notificationsId: NotificationType
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(description)
            setSound(UNNotificationSound.defaultSound())
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            1.0, // Muestra la notificación después de 1 segundo
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notificationsId.name,
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(
            request = request,
            withCompletionHandler = { error ->
                if (error != null) {
                    println("🚨 Error al enviar notificación: ${error.localizedDescription}")
                } else {
                    println("✅ Notificación programada exitosamente")
                }
            }
        )
    }


    override fun hideNotification(notificationType: NotificationType) {
        UNUserNotificationCenter.currentNotificationCenter().removePendingNotificationRequestsWithIdentifiers(
            identifiers = listOf(notificationType.name)
        )
    }
}