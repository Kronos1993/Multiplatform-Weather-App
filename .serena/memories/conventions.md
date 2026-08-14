# Conventions

- Package root `com.kronos.multiplatform.weatherapp`; Android `namespace`/`applicationId` match it.
- Localized strings live in two places that must be kept in sync **manually**:
  1. Compose Multiplatform resources: `composeApp/src/commonMain/composeResources/values[-es]/*.xml`,
     split by topic (`strings.xml`, `strings_about.xml`, `strings_suggestions.xml`,
     `strings_loading_dialog.xml`, `preference_strings.xml`, `months.xml`) — used by the Compose UI.
  2. `iosApp/en.strings` + `iosApp/es.strings` — used by native iOS code (notifications, widgets),
     resolved via `SuggestionStringResolver.swift`.
  Adding a user-facing string touched from both sides means editing both locations.
- Error handling: prefer the sealed `Result` type (`mem:architecture`) over throwing; ViewModels
  catch/convert failures into sealed screen states rather than letting exceptions reach the UI.
- No detekt/ktlint/`.editorconfig` configured — no automated style enforcement; match surrounding
  code style by convention.
- No test source sets exist yet — don't assume a test command exists (`mem:task_completion`).
