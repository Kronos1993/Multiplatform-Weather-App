# Suggested Commands

- Android debug build: `./gradlew :composeApp:assembleDebug`
- Desktop (JVM) run: `./gradlew :composeApp:run`
- iOS: no Gradle run target — build/run via Xcode using `iosApp/iosApp.xcodeproj` (or the IDE run
  configuration), which links the KMP `ComposeApp` framework produced by the `:composeApp` build.
- Full Gradle build (all targets): `./gradlew build`

## Darwin (macOS) shell notes
- Default shell is zsh.
- `find`/`grep` are BSD variants (no GNU-only flags like `-regextype posix-extended`).
- No `uv`/`uvx` preinstalled by default — installed via `brew install uv` for the Serena MCP server
  (see `.mcp.json`).
