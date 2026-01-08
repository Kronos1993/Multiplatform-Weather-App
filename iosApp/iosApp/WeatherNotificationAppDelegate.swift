//
// Created by Marcos Guerra Liso on 11/10/25.
//

import Foundation
import UIKit
import UserNotifications
import BackgroundTasks
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {

        // 🔹 Registrar tarea de background
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "com.kronos.weatherapp.refresh_weather_notification",
            using: nil
        ) { task in
            self.handleAppRefresh(task: task as! BGAppRefreshTask)
        }

        // 🔹 Programar tarea periódica
        scheduleAppRefresh()

        let center = UNUserNotificationCenter.current()
        center.delegate = self // 🔹 Esto permite mostrar notificaciones aunque la app esté abierta

        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if granted {
                print("✅ Permisos de notificación concedidos")
            } else {
                print("🚫 Permisos de notificación denegados: \(error?.localizedDescription ?? "")")
            }
        }

        BGTaskScheduler.shared.getPendingTaskRequests { tasks in
            print("📌 Pending BGTasks:", tasks.map { $0.identifier })
        }

        return true
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        print("📱 App pasó a background → reprogramando BGTask")
        scheduleAppRefresh()
    }

    // 🔹 Muestra notificación también cuando la app está abierta (foreground)
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .list, .badge])
    }

    // 🔹 (Opcional) Manejar clic en la notificación
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let id = response.notification.request.identifier
        print("🔔 Notificación tocada con ID: \(id)")
        completionHandler()
    }

    // 🔹 Lógica al ejecutar la tarea en background
    private func handleAppRefresh(task: BGAppRefreshTask) {
        print("🌤 Ejecutando tarea background de clima")


        let worker = WeatherNotificationBackgroundTask()

        task.expirationHandler = {
            print("⏰ Expiró la tarea")
            task.setTaskCompleted(success: false)
        }

        Task {
            worker.doInitNotificationStrings()

            do {
                try await worker.refreshWeather()
                scheduleAppRefresh()
                task.setTaskCompleted(success: true)
            } catch {
                print("❌ Error ejecutando refreshWeather(): \(error)")
                task.setTaskCompleted(success: false)
            }
        }

    }

    // 🔹 Reprogramar la tarea para ejecutarse de nuevo en unas horas
    private func scheduleAppRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: "com.kronos.weatherapp.refresh_weather_notification")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60 * 1) // cada 1 horas

        do {
            try BGTaskScheduler.shared.submit(request)
            print("⏰ BGTask programada exitosamente")
        } catch {
            print("⚠️ Error programando BGTask: \(error)")
        }
    }
}
