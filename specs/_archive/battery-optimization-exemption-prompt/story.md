---
spec_id: battery-optimization-exemption-prompt
source: manual
source_ref: Reporte de usuario (Honor 200 / MagicOS) — notificaciones y widgets no se actualizan; dictado por el mantenedor en chat
fetched_at: 2026-10-07
fetched_by: /spec-new
---

# Story: Solicitar exención de optimización de batería para todos los dispositivos Android

<!--
RAW INTAKE — DO NOT EDIT AFTER FETCH

This file preserves the source as it arrived, so future readers can
audit what /spec-plan worked from. /spec-plan reads this and produces
proposal.md; story.md is append-only thereafter.
-->

## Metadata

| Field | Value |
|-------|-------|
| Type | bug |
| Priority | high |
| Created | 2026-10-07 |

## Description

Solicitar exención de optimización de batería para todos los dispositivos Android (no solo Honor/Huawei).

Contexto: un usuario reportó que en un Honor 200 (MagicOS) ni las notificaciones de clima ni los widgets Glance se actualizan; los workers periódicos de WorkManager (WeatherNotificationWorker, WeatherAlertNotificationWorker, WeatherWidgetUpdateWorker, WeatherSuggestionNotificationWorker) no llegan a ejecutarse porque el sistema mata la app en segundo plano. Hoy la app nunca pide REQUEST_IGNORE_BATTERY_OPTIMIZATIONS ni guía al usuario a ajustes.

Objetivo: mostrar a TODOS los usuarios Android (cuando la app aún está sujeta a optimización de batería, `PowerManager.isIgnoringBatteryOptimizations == false`) un diálogo/pantalla explicativo que lance la solicitud de exención (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` o, como fallback, `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`); opcionalmente, en fabricantes con gestor de autoinicio propio (Honor, Huawei, Xiaomi, Oppo, Vivo, Samsung…) ofrecer un acceso adicional a su pantalla de inicio/segundo plano si el intent resuelve.

Respetar "no volver a preguntar" / no insistir en cada arranque.

Strings nuevos en composeResources `values` y `values-es`.

iOS/Desktop no aplican (expect/actual no-op o solo androidMain).

Fuera de alcance de este spec (se arreglan aparte como bugfix pequeño):
- quitar el chequeo `NET_CAPABILITY_VALIDATED` en `WeatherNotificationWorker`,
- cambiar `WeatherWidgetUpdateWorker.schedule` a `ExistingPeriodicWorkPolicy.UPDATE`,
- relanzar `CancellationException` en los workers.

Aclaración del mantenedor: "eso de la batería no puede ser solo para honor/huawei, debe salir para todos".

## Acceptance criteria (as written in source)

- El aviso de optimización de batería se muestra en todos los dispositivos Android mientras la app siga sujeta a optimización (no solo Honor/Huawei).
- El aviso lanza la solicitud de exención del sistema, con fallback a la pantalla de ajustes de optimización.
- En fabricantes con gestor de autoinicio propio se ofrece acceso adicional a esa pantalla, solo si el intent resuelve.
- No se insiste en cada arranque (opción "no volver a preguntar").
- Strings en `values` y `values-es`.
- iOS/Desktop sin cambios de comportamiento.

## Comments / discussion

<!-- (none — this repo has no issue tracker or comment thread to pull from) -->

(none — file/manual/url intake, no comment thread)

## Attachments

-

## Links

- Bugfix pequeño relacionado (fuera de este spec): `WeatherNotificationWorker` (chequeo de red validada, CancellationException), `WeatherWidgetUpdateWorker.schedule` (política KEEP → UPDATE).
