package com.kronos.multiplatform.weatherapp.core.notification

interface INotifications {

    fun createNotification(
        title: String,
        description: String,
        group: NotificationGroup,
        notificationsId: NotificationType,
    )

    fun hideNotification(notificationType: NotificationType)

}