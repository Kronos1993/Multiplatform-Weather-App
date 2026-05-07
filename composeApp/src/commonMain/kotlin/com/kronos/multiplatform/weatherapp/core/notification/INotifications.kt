package com.kronos.multiplatform.weatherapp.core.notification

interface INotifications {

    fun createNotification(
        title: String,
        shortDescription: String,
        description: String,
        notificationImageUrl: String,
        group: NotificationGroup,
        notificationsId: NotificationType,
    )

    fun createNotificationAlerts(
        title: String,
        shortDescription: String,
        description: String,
        group: NotificationGroup,
        notificationsId: NotificationType,
    )

    fun createNotificationSuggestion(
        title: String,
        shortDescription: String,
        description: String,
        group: NotificationGroup,
        notificationsId: NotificationType,
    )

    fun hideNotification(notificationType: NotificationType)

}