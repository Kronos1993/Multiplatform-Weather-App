//
// Created by Marcos Guerra Liso on 4/24/26.
//

import Foundation
import ComposeApp

struct SuggestionStringResolver {

    // ── Entry points públicos ──────────────────────────────────────────────

    func resolveTitle(suggestion: WeatherSuggestionModel, locationName: String) -> String {
        switch suggestion.type {

        case SuggestionType.rain:
            return rainTitle(moment: suggestion.moment)

        case SuggestionType.uv:
            return suggestion.priority == SuggestionPriority.high
                ? loc("suggestion_uv_high_title")
                : loc("suggestion_uv_medium_title")

        case SuggestionType.heat:
            switch suggestion.priority {
            case SuggestionPriority.high:   return loc("suggestion_heat_high_title")
            case SuggestionPriority.medium: return loc("suggestion_heat_medium_title")
            default:                        return loc("suggestion_heat_low_title")
            }

        case SuggestionType.cold:       return loc("suggestion_cold_title")
        case SuggestionType.wind:       return loc("suggestion_wind_title")
        case SuggestionType.humidity:   return loc("suggestion_humidity_title")
        case SuggestionType.visibility: return loc("suggestion_visibility_title")

        case SuggestionType.tomorrowForecast:
            switch suggestion.priority {
            case SuggestionPriority.high: return loc("suggestion_tomorrow_rain_title")
            case SuggestionPriority.medium:
                return String(format: loc("suggestion_tomorrow_alert_title"), locationName)
            default: return loc("suggestion_tomorrow_clear_title")
            }

        case SuggestionType.morningSummary:
            switch suggestion.priority {
            case SuggestionPriority.high:   return loc("suggestion_morning_alert_title")
            case SuggestionPriority.medium: return loc("suggestion_morning_warning_title")
            default:                        return loc("suggestion_morning_normal_title")
            }

        default:
            return ""
        }
    }

    func resolveMessage(suggestion: WeatherSuggestionModel) -> String {
        let template = messageTemplate(suggestion: suggestion)
        return applyArgs(template: template, suggestion: suggestion)
    }

    // ── Templates por tipo y momento ──────────────────────────────────────

    private func messageTemplate(suggestion: WeatherSuggestionModel) -> String {
        switch suggestion.type {

        case SuggestionType.rain:
            return rainMessage(moment: suggestion.moment)

        case SuggestionType.uv:
            if suggestion.priority == SuggestionPriority.high {
                return uvHighMessage(moment: suggestion.moment)
            } else {
                return uvMediumMessage(moment: suggestion.moment)
            }

        case SuggestionType.heat:
            switch suggestion.priority {
            case SuggestionPriority.high:   return heatHighMessage(moment: suggestion.moment)
            case SuggestionPriority.medium: return heatMediumMessage(moment: suggestion.moment)
            default:                        return heatLowMessage(moment: suggestion.moment)
            }

        case SuggestionType.cold:
            return coldMessage(moment: suggestion.moment)

        case SuggestionType.wind:
            return windMessage(moment: suggestion.moment)

        case SuggestionType.humidity:
            return humidityMessage(moment: suggestion.moment)

        case SuggestionType.visibility:
            return visibilityMessage(moment: suggestion.moment)

        case SuggestionType.tomorrowForecast:
            switch suggestion.priority {
            case SuggestionPriority.high: return loc("suggestion_tomorrow_rain_message")
            case SuggestionPriority.medium:
                return suggestion.icon == "🧴"
                    ? loc("suggestion_tomorrow_uv_message")
                    : loc("suggestion_tomorrow_heat_message")
            default: return loc("suggestion_tomorrow_clear_message")
            }

        case SuggestionType.morningSummary:
            switch suggestion.icon {
            case "🌂": return loc("suggestion_morning_rain_message")
            case "🧴": return loc("suggestion_morning_uv_high_message")
            case "😎": return loc("suggestion_morning_uv_medium_message")
            case "💧": return loc("suggestion_morning_heat_high_message")
            default:   return loc("suggestion_morning_heat_medium_message")
            }

        default:
            return ""
        }
    }

    // ── Aplicar args al template ───────────────────────────────────────────

    private func applyArgs(template: String, suggestion: WeatherSuggestionModel) -> String {
        let resolved: [CVarArg] = suggestion.args.map { arg in
            switch arg {
            case let t as SuggestionArg.Temperature: return "\(t.value)"
            case let p as SuggestionArg.Percentage:  return "\(p.value)"
            case let w as SuggestionArg.WindSpeed:   return "\(w.value)"
            case let d as SuggestionArg.Distance:    return "\(d.value)"
            case let t as SuggestionArg.Text:        return t.value
            case let u as SuggestionArg.Uv:          return uvLevelString(level: u.level)
            default: return ""
            }
        }

        // Native Foundation positional formatting (%1$@, %2$@, ...) — the
        // iOS counterpart to NSLocalizedString, not a hand-rolled reimplementation
        // of Android's %1$s substring replace.
        return String(format: template, arguments: resolved)
    }

    // ── Helpers por tipo ──────────────────────────────────────────────────

    private func rainTitle(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_rain_title_morning")
        case DayMoment.afternoon: return loc("suggestion_rain_title_afternoon")
        case DayMoment.evening:   return loc("suggestion_rain_title_evening")
        default:                  return loc("suggestion_rain_title_night")
        }
    }

    private func rainMessage(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_rain_morning_message")
        case DayMoment.afternoon: return loc("suggestion_rain_afternoon_message")
        case DayMoment.evening:   return loc("suggestion_rain_evening_message")
        default:                  return loc("suggestion_rain_night_message")
        }
    }

    private func uvHighMessage(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_uv_high_morning_message")
        case DayMoment.afternoon: return loc("suggestion_uv_high_afternoon_message")
        case DayMoment.evening:   return loc("suggestion_uv_high_evening_message")
        default:                  return loc("suggestion_uv_high_night_message")
        }
    }

    private func uvMediumMessage(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_uv_medium_morning_message")
        case DayMoment.afternoon: return loc("suggestion_uv_medium_afternoon_message")
        case DayMoment.evening:   return loc("suggestion_uv_medium_evening_message")
        default:                  return loc("suggestion_uv_medium_night_message")
        }
    }

    private func heatHighMessage(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_heat_high_morning_message")
        case DayMoment.afternoon: return loc("suggestion_heat_high_afternoon_message")
        case DayMoment.evening:   return loc("suggestion_heat_high_evening_message")
        default:                  return loc("suggestion_heat_high_night_message")
        }
    }

    private func heatMediumMessage(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_heat_medium_morning_message")
        case DayMoment.afternoon: return loc("suggestion_heat_medium_afternoon_message")
        case DayMoment.evening:   return loc("suggestion_heat_medium_evening_message")
        default:                  return loc("suggestion_heat_medium_night_message")
        }
    }

    private func heatLowMessage(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_heat_low_morning_message")
        case DayMoment.afternoon: return loc("suggestion_heat_low_afternoon_message")
        case DayMoment.evening:   return loc("suggestion_heat_low_evening_message")
        default:                  return loc("suggestion_heat_low_night_message")
        }
    }

    private func coldMessage(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_cold_morning_message")
        case DayMoment.afternoon: return loc("suggestion_cold_afternoon_message")
        case DayMoment.evening:   return loc("suggestion_cold_evening_message")
        default:                  return loc("suggestion_cold_night_message")
        }
    }

    private func windMessage(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_wind_morning_message")
        case DayMoment.afternoon: return loc("suggestion_wind_afternoon_message")
        case DayMoment.evening:   return loc("suggestion_wind_evening_message")
        default:                  return loc("suggestion_wind_night_message")
        }
    }

    private func humidityMessage(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_humidity_morning_message")
        case DayMoment.afternoon: return loc("suggestion_humidity_afternoon_message")
        case DayMoment.evening:   return loc("suggestion_humidity_evening_message")
        default:                  return loc("suggestion_humidity_night_message")
        }
    }

    private func visibilityMessage(moment: DayMoment) -> String {
        switch moment {
        case DayMoment.morning:   return loc("suggestion_visibility_morning_message")
        case DayMoment.afternoon: return loc("suggestion_visibility_afternoon_message")
        case DayMoment.evening:   return loc("suggestion_visibility_evening_message")
        default:                  return loc("suggestion_visibility_night_message")
        }
    }

    private func uvLevelString(level: UvIndexLevel) -> String {
        switch level {
        case UvIndexLevel.low:      return loc("uv_level_low")
        case UvIndexLevel.medium:   return loc("uv_level_moderate")
        case UvIndexLevel.high:     return loc("uv_level_high")
        case UvIndexLevel.veryHigh: return loc("uv_level_very_high")
        default:                    return loc("uv_level_extreme")
        }
    }

    // ── Shorthand ─────────────────────────────────────────────────────────
    private func loc(_ key: String) -> String {
        NSLocalizedString(key, comment: "")
    }
}
