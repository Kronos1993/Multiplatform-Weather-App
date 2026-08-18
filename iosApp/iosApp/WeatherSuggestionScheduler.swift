//
// Created by Marcos Guerra Liso on 4/24/26.
//

import Foundation
import UserNotifications
import ComposeApp

class WeatherSuggestionScheduler {

    private let resolver = SuggestionStringResolver()

    private enum NotificationId {
        static let morning = "suggestion_morning"
        static let midday  = "suggestion_midday"
        static let evening = "suggestion_evening"
    }

    // ── Entry point — llamar cuando se actualiza el clima ─────────────────
    func scheduleAll(forecast: Forecast, measureUnit: MeasureUnit) {
        cancelPrevious()

        let timeZone = forecast.location.tzId

        // 6:00 AM — resumen del día
        let morningSuggestions = forecast.mapMorningSuggestions(
            timeZone: timeZone,
            measureUnit: measureUnit
        )
        if let top = morningSuggestions.first as? WeatherSuggestionModel {
            scheduleAt(
                id: NotificationId.morning,
                hour: 6, minute: 0,
                title: resolver.resolveTitle(suggestion: top, locationName: forecast.location.name),
                body: morningSuggestions
                .compactMap { $0 as? WeatherSuggestionModel }
                .map { resolver.resolveMessage(suggestion: $0) }
                .joined(separator: " • ")
            )
        }

        // 12:00 PM — condiciones actuales
        let middaySuggestions = forecast.mapCurrentSuggestions(
            timeZone: timeZone,
            measureUnit: measureUnit
        )
        if let top = middaySuggestions.first as? WeatherSuggestionModel {
            let top2 = middaySuggestions.prefix(2).compactMap { $0 as? WeatherSuggestionModel }
            scheduleAt(
                id: NotificationId.midday,
                hour: 12, minute: 0,
                title: resolver.resolveTitle(suggestion: top, locationName: forecast.location.name),
                body: top2
                .map { resolver.resolveMessage(suggestion: $0) }
                .joined(separator: " • ")
            )
        }

        // 9:00 PM — pronóstico de mañana
        if let eveningSuggestion = forecast.mapTomorrowNotification(
            timeZone: timeZone,
            measureUnit: measureUnit
        ) as? WeatherSuggestionModel {
            scheduleAt(
                id: NotificationId.evening,
                hour: 21, minute: 0,
                title: resolver.resolveTitle(
                    suggestion: eveningSuggestion,
                    locationName: forecast.location.name
                ),
                body: resolver.resolveMessage(suggestion: eveningSuggestion)
            )
        }
    }

    // ── Programa una notificación a hora exacta con UNCalendarTrigger ──────
    private func scheduleAt(
        id: String,
        hour: Int,
        minute: Int,
        title: String,
        body: String
    ) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default

        var components = DateComponents()
        components.hour = hour
        components.minute = minute
        components.second = 0

        let trigger = UNCalendarNotificationTrigger(
            dateMatching: components,
            repeats: false
        )

        let request = UNNotificationRequest(
            identifier: id,
            content: content,
            trigger: trigger
        )

        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("🚨 Error scheduling \(id): \(error.localizedDescription)")
            } else {
                print("✅ Scheduled \(id) at \(hour):\(String(format: "%02d", minute))")
            }
        }
    }

    // ── Cancela las notificaciones anteriores ──────────────────────────────
    private func cancelPrevious() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(
            withIdentifiers: [
                NotificationId.morning,
                NotificationId.midday,
                NotificationId.evening
            ]
        )
    }
}
