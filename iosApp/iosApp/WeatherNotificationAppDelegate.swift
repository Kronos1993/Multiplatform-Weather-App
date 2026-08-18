import Foundation
import UIKit
import UserNotifications
import BackgroundTasks
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    private let suggestionScheduler = WeatherSuggestionScheduler()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {

        // ── Registrar BGTask de clima ──────────────────────────────────────
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "com.kronos.weatherapp.refresh_weather_notification",
            using: nil
        ) { [weak self] task in
            self?.handleWeatherRefresh(task: task as! BGAppRefreshTask)
        }

        scheduleWeatherRefresh()

        // ── Permisos de notificación ───────────────────────────────────────
        UNUserNotificationCenter.current().delegate = self
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { granted, _ in
            print(granted ? "✅ Notificaciones concedidas" : "🚫 Denegadas")
        }

        // ── Re-arma las sugerencias al arrancar ─────────────────────────────
        // UNCalendarNotificationTrigger(repeats: false) es de un solo disparo:
        // hace falta volver a llamar scheduleAll antes de cada ocurrencia para
        // que sigan llegando día a día. El BGAppRefreshTask horario es
        // oportunista (iOS decide si corre), así que el lanzamiento de la app
        // es el único momento garantizado — equivalente nativo de iOS al
        // re-arme que Android hace en cada Application.onCreate.
        performWeatherRefresh()

        return true
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        scheduleWeatherRefresh()
    }

    // ── BGTask handler ─────────────────────────────────────────────────────
    private func handleWeatherRefresh(task: BGAppRefreshTask) {
        task.expirationHandler = {
            task.setTaskCompleted(success: false)
        }

        performWeatherRefresh { success in
            self.scheduleWeatherRefresh()
            task.setTaskCompleted(success: success)
        }
    }

    // ── Fetch + notifica + re-arma sugerencias — usado en el lanzamiento y en el BGTask ──
    private func performWeatherRefresh(onComplete: ((Bool) -> Void)? = nil) {
        let worker = WeatherNotificationBackgroundTask()
        worker.doInitNotificationStrings(
            title: NSLocalizedString("notification_title", comment: ""),
            shortDetails: NSLocalizedString("notification_short_details", comment: ""),
            longDetails: NSLocalizedString("notification_long_details", comment: ""),
            titleFahrenheit: NSLocalizedString("notification_title_fahrenheit", comment: ""),
            shortDetailsFahrenheit: NSLocalizedString("notification_short_details_fahrenheit", comment: ""),
            longDetailsFahrenheit: NSLocalizedString("notification_long_details_fahrenheit", comment: "")
        )
        worker.onForecastReady = { [weak self] forecast, measureUnit in
            self?.suggestionScheduler.scheduleAll(forecast: forecast, measureUnit: measureUnit)
        }

        Task {
            do {
                try await worker.refreshWeather()
                onComplete?(true)
            } catch {
                print("❌ Error: \(error)")
                onComplete?(false)
            }
        }
    }

    // ── Scheduler BGTask clima — cada 1 hora ───────────────────────────────
    private func scheduleWeatherRefresh() {
        let request = BGAppRefreshTaskRequest(
            identifier: "com.kronos.weatherapp.refresh_weather_notification"
        )
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60)
        try? BGTaskScheduler.shared.submit(request)
        print("⏰ BGTask clima programada en 60 min")
    }

    // ── Notification delegates ─────────────────────────────────────────────
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // A routine weather-refresh update (manual or hourly background)
        // shouldn't pop a banner over whatever the user is already looking
        // at — Android doesn't show a heads-up alert for this either. Still
        // update Notification Center and the badge so the latest info is
        // there when the user checks, just without interrupting the screen.
        if notification.request.identifier == "WEATHER_UPDATED" {
            completionHandler([.list, .badge])
        } else {
            completionHandler([.banner, .list, .badge])
        }
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        print("🔔 Notificación tocada: \(response.notification.request.identifier)")
        completionHandler()
    }
}
