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
        postNotification(
            notificationsId = notificationsId,
            title = title,
            shortDescription = shortDescription,
            description = description,
        )
    }

    override fun createNotificationAlerts(
        title: String,
        shortDescription: String,
        description: String,
        group: NotificationGroup,
        notificationsId: NotificationType
    ) {
        postNotification(
            notificationsId = notificationsId,
            title = title,
            shortDescription = shortDescription,
            description = description,
        )
    }

    override fun createNotificationSuggestion(
        title: String,
        shortDescription: String,
        description: String,
        group: NotificationGroup,
        notificationsId: NotificationType
    ) {
        postNotification(
            notificationsId = notificationsId,
            title = title,
            shortDescription = shortDescription,
            description = description,
        )
    }

    private fun postNotification(
        notificationsId: NotificationType,
        title: String,
        shortDescription: String,
        description: String,
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

        val center = UNUserNotificationCenter.currentNotificationCenter()

        // A 1s trigger fires almost immediately, moving the request from
        // pending to delivered before a same-identifier add can replace it —
        // clear both states first so at most one notification per
        // NotificationType is ever visible, mirroring Android's
        // NotificationManagerCompat.notify(id, ...) replace behavior.
        center.removeDeliveredNotificationsWithIdentifiers(listOf(notificationsId.name))
        center.removePendingNotificationRequestsWithIdentifiers(listOf(notificationsId.name))

        center.addNotificationRequest(
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