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

        // ── Default de idioma ANTES de resolver cualquier string ────────────
        // Si el usuario nunca abrió Settings a elegir idioma, "AppleLanguages"
        // no existe todavía en NSUserDefaults — currentLanguageTable() caía al
        // "en" hardcodeado. Con esto queda seteado explícitamente al idioma
        // del sistema (si es "es", si no "en") apenas se instala/lanza la app
        // por primera vez, en vez de depender de un fallback silencioso.
        ensureDefaultLanguagePreference()

        // ── Arranca Koin ANTES de cualquier trabajo en background ───────────
        // WeatherNotificationBackgroundTask (KoinComponent) se ejecuta desde
        // performWeatherRefresh() más abajo, en este mismo método — y también
        // desde el BGAppRefreshTask, que puede lanzar la app sin que SwiftUI
        // llegue a construir ComposeView/MainViewController (único lugar que
        // antes arrancaba Koin). Sin este arranque explícito, `by inject()`
        // fallaba en silencio (excepción atrapada por el catch genérico de
        // refreshWeather()) y ni la notificación WEATHER_UPDATED ni las
        // sugerencias se programaban — por eso solo aparecían al refrescar
        // manualmente desde Home/Locations, donde Koin ya estaba arrancado.
        MainViewControllerKt.startKoinIOS()

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

    // en.strings/es.strings are plain flat files copied to the bundle root
    // (project.pbxproj has them as separate PBXFileReferences, not a
    // PBXVariantGroup, and knownRegions only lists "en" — "es" isn't even a
    // declared project localization). NSLocalizedString(key, comment:) looks
    // up the default "Localizable" table inside the current locale's
    // .lproj folder, which doesn't exist here, so it always fell back to
    // returning the raw key — that's why notification text showed literal
    // keys instead of resolved strings. Passing tableName: explicitly looks
    // up "<table>.strings" directly in the bundle root instead, matching
    // how these files are actually bundled.
    private func currentLanguageTable() -> String {
        let langs = UserDefaults.standard.stringArray(forKey: "AppleLanguages") ?? []
        let code = langs.first?.split(separator: "-").first.map(String.init) ?? "en"
        return code == "es" ? "es" : "en"
    }

    // Only the two bundled tables ("en"/"es") are ever valid here — anything
    // else (a third system language, or none set at all) falls back to "en",
    // matching the project's developmentRegion.
    private func ensureDefaultLanguagePreference() {
        let defaults = UserDefaults.standard
        guard defaults.stringArray(forKey: "AppleLanguages") == nil else { return }

        let systemCode = Locale.current.language.languageCode?.identifier.lowercased() ?? "en"
        let table = systemCode == "es" ? "es" : "en"
        defaults.set([table], forKey: "AppleLanguages")
    }

    private func localized(_ key: String) -> String {
        NSLocalizedString(key, tableName: currentLanguageTable(), bundle: .main, value: key, comment: "")
    }

    // ── Fetch + notifica + re-arma sugerencias — usado en el lanzamiento y en el BGTask ──
    private func performWeatherRefresh(onComplete: ((Bool) -> Void)? = nil) {
        let worker = WeatherNotificationBackgroundTask()
        worker.doInitNotificationStrings(
            title: localized("notification_title"),
            shortDetails: localized("notification_short_details"),
            longDetails: localized("notification_long_details"),
            titleFahrenheit: localized("notification_title_fahrenheit"),
            shortDetailsFahrenheit: localized("notification_short_details_fahrenheit"),
            longDetailsFahrenheit: localized("notification_long_details_fahrenheit")
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
