package com.kronos.multiplatform.weatherapp.core.notification

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual class AppNotification : INotifications {

    override fun createNotification(
        title: String,
        shortDescription: String,
        description: String,
        notificationImageUrl: String,
        group: NotificationGroup,
        notificationsId: NotificationType,
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setSubtitle(shortDescription)
            setBody(description)
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            1.0,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notificationsId.name,
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(
            request = request
        ) { error ->
            if (error != null) {
                println("🚨 Error al enviar notificación: ${error.localizedDescription}")
            } else {
                println("✅ Notificación programada exitosamente")
            }
        }
    }

    override fun createNotificationAlerts(
        title: String,
        shortDescription: String,
        description: String,
        group: NotificationGroup,
        notificationsId: NotificationType
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setSubtitle(shortDescription)
            setBody(description)
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            1.0,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notificationsId.name,
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(
            request = request
        ) { error ->
            if (error != null) {
                println("🚨 Error al enviar notificación: ${error.localizedDescription}")
            } else {
                println("✅ Notificación programada exitosamente")
            }
        }
    }


    override fun hideNotification(notificationType: NotificationType) {
        UNUserNotificationCenter.currentNotificationCenter().removePendingNotificationRequestsWithIdentifiers(
            identifiers = listOf(notificationType.name)
        )
    }
}