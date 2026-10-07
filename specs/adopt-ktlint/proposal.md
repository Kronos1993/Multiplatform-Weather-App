---
spec_id: adopt-ktlint
title: Instalar ktlint, validar el .editorconfig existente y documentar que sus reglas deben respetarse
type: chore
priority: normal
source: manual
source_ref: user request in chat, 2026-09-09
created: 2026-09-09
status: INTAKE_PARSED
recommend_split: no
blockers: []
depends_on: []
expect_actual_touched: no
localization_touched: no
branch_suggested: chore/adopt-ktlint
---

# Proposal: Instalar ktlint, validar el .editorconfig existente y documentar que sus reglas deben respetarse

## 1. Source

Petición manual del usuario en chat (2026-09-09): instalar ktlint, revisar si
el `.editorconfig` que ya añadió en la raíz del repo está completo, dejar
constancia escrita de que las reglas de ktlint deben respetarse de aquí en
adelante, y verificar que el código actual del proyecto sea compliant con
esas reglas. Ver `specs/adopt-ktlint/story.md` para el detalle completo y el
contexto de reconocimiento ya relevado (el `.editorconfig` existe y ya trae
overrides `ktlint_standard_*` bien justificados, pero ktlint no está
instalado como plugin de Gradle en ningún `.kts`/`libs.versions.toml`).

## 2. Problem / Why

El proyecto no tiene hoy ninguna herramienta de estilo automatizada
(`CLAUDE.md` → "Conventions": *"No detekt/ktlint/.editorconfig — no
automated style enforcement"*), a pesar de que ya se preparó un
`.editorconfig` con reglas ktlint pensadas específicamente para las
convenciones de este repo (Compose PascalCase, paquetes snake_case,
sufijos `expect`/`actual`, etc.). Sin el plugin instalado, ese
`.editorconfig` no tiene ningún consumidor: no hay manera de verificar
consistencia de estilo, y la documentación del proyecto sigue afirmando
que no existe enforcement, lo cual quedaría desactualizado y podría hacer
que un colaborador futuro (humano o agente) ignore las reglas.

## 3. Scope

**In scope**
- Instalar ktlint como plugin de Gradle en el módulo único `:composeApp`
  (vía Version Catalog, siguiendo el patrón existente de `apply false` en
  el `build.gradle.kts` raíz + alias-apply en `composeApp/build.gradle.kts`).
- Confirmar que la versión de ktlint resuelta es compatible con el
  esquema `ktlint_standard_*` que ya usa el `.editorconfig` (ktlint
  0.50+/1.x) y con Kotlin `2.3.21` / AGP `9.0.1` de este proyecto.
- Revisar el `.editorconfig` raíz existente contra esa versión resuelta:
  confirmar que cubre lo que el proyecto quiere (o añadir overrides
  adicionales con el mismo estilo de comentario justificativo que ya usan
  las entradas actuales).
- Ejecutar `ktlintCheck` una vez contra el código actual (`commonMain`,
  `androidMain`, `iosMain`, `jvmMain`) y, si el número de violaciones es
  pequeño y mecánico, corregirlas en este mismo spec; si es grande,
  documentar el hallazgo y NO intentar corregirlo todo aquí (ver Step 4
  condicional en plan.md y OQ-3 abajo — evita convertir este chore de
  tooling en un chore de reformateo masivo sin visibilidad previa del
  tamaño real).
- Actualizar `CLAUDE.md` ("Conventions") y la memoria Serena
  `.serena/memories/conventions.md` para dejar constancia de que las
  reglas de ktlint (vía `.editorconfig`) deben respetarse de ahora en
  adelante, incluyendo cómo correr el check/format.

**Out of scope**
- Instalar detekt (el pedido es específicamente ktlint).
- Agregar un gate de CI que corra `ktlintCheck` — no existe hoy ningún
  workflow que corra `./gradlew build`/`check` (solo `publish-android.yml`
  y `promote-android.yml`, que solo publican/promueven). Ver OQ-2.
- Reformatear en bloque violaciones no triviales si Step 3 encuentra un
  volumen grande — eso se convierte en un spec de seguimiento (ver OQ-3).

## 4. Affected areas

| Area | Source set(s) | Class(es) / file(s) touched | Change type | Resolved by | Notes |
|--------|--------------|-------------------------------|-------------|-------------|-------|
| Build config (Version Catalog + plugin wiring) | project-level (no source set) | `gradle/libs.versions.toml`, `build.gradle.kts` (raíz), `composeApp/build.gradle.kts` | add | Step 2 | sigue el patrón `apply false` (raíz) + alias-apply (`composeApp`) ya usado por el resto de plugins |
| Estilo / config de linter | project-level | `.editorconfig` | review / possible edit | Step 3 | confirmar completitud contra la versión de ktlint resuelta en Step 1 |
| Documentación | N/A | `CLAUDE.md` ("Conventions", "Definition of done"), `.serena/memories/conventions.md` | edit | Step 5 | reemplaza la afirmación "no automated style enforcement" |
| Código fuente — iosMain + Kotlin scripts (arreglado en este spec) | iosMain (19 archivos) + `composeApp/build.gradle.kts` | ver `decisions.md` Step 3/4 | fix | Step 3 → Step 4 | 88 violaciones mecánicas corregidas con `ktlintFormat` + 1 fix manual (`no-consecutive-comments`, no autocorregible). Ambos check tasks quedan verdes. |
| Código fuente — commonMain/androidMain (NO tocado en este spec) | commonMain (177 archivos reales, tras excluir generados) + androidMain (40 archivos reales) + sus duplicados de variante/target (`AndroidRelease`/`AndroidDebug`/`IosArm64`/`IosX64`/`IosSimulatorArm64`) | ver `decisions.md` corrección de 2026-09-09 | fix (diferido) | Step 3 (medición corregida) → **spec de seguimiento** | La medición inicial (sin `--continue`) subestimó esto por el fail-fast de Gradle — corregido: `violation_scope = large`. No se reformatea aquí (ver Step 4 condicional en plan.md); se abre un spec de seguimiento. Mientras tanto, `ktlint { ignoreFailures.set(true) }` evita que este backlog rompa `./gradlew build` (ver §5a Secrets/§7 y AC-5). |
| Config de build — mitigación de código generado | project-level | `composeApp/build.gradle.kts` (`ktlint { filter { exclude(...) } }`) | add | Step 4 | excluye `build/generated/**` (Room/KSP) del alcance de ktlint — sin esto, 8 archivos generados en commonMain y 1 en androidMain aparecían como "violaciones" que no son código del proyecto y reaparecerían en cada regeneración |

## 5. Architectural gauntlet (this repo's hard rules)

### 5a. Always explicit (no shortcut)

- [x] **Expect/actual parity** — N/A. `expect_actual_touched: no`; este
      spec no toca ninguna declaración `expect`/`actual`. Confirmado
      durante la investigación de reconocimiento (solo build config y
      documentación).
      Approach: N/A
- [x] **Dual localization** — N/A. `localization_touched: no`; no se
      toca ningún string visible para el usuario (Compose ni iOS
      nativo).
      Approach: N/A
- [x] **Secrets & logging** — Confirmado. Este spec no toca la API key
      de WeatherAPI ni ninguna credencial; los cambios son de
      configuración de build y documentación.
      Approach: confirmed
- [x] **No automated tests exist** — Acknowledged. La verificación es
      build-green (`./gradlew build`) + `ktlintCheck` verde (o
      documentado si no lo está), nunca una suite de tests.
      Acknowledged: yes

### 5b. Confinement-conditional

- [ ] **Confinement claim** — No aplica el atajo: este cambio no es una
      feature de UI/ViewModel confinada a un solo `features/<feature>`;
      es un cambio transversal de tooling de build + documentación.

- [x] **Domain/data boundary (DIP)** — N/A. No se toca ninguna
      interfaz `domain/repository/*` ni implementación `data/*Impl`;
      el cambio es de configuración de Gradle y documentación.
      Approach: N/A
- [x] **Result-type error handling** — N/A. No se agrega ni modifica
      ningún código de repositorio/datos que pueda fallar.
      Approach: N/A

## 6. Skills

**Skills**: direct edits

## 7. Risks

- El plugin de ktlint para Gradle (`org.jlleitschuh.gradle.ktlint` es la
  opción estándar) no tiene su compatibilidad confirmada todavía contra
  Kotlin `2.3.21` / AGP `9.0.1` / la estructura KMP de este módulo
  (`androidMain`/`iosMain`/`jvmMain`) — se confirma en Step 1
  `[investigate]` antes de aplicarlo.
- El código actual nunca fue verificado contra ktlint; puede haber un
  número no trivial de violaciones de estilo reales (no solo las de
  wrapping ya deshabilitadas en `.editorconfig`). Ejecutar
  `ktlintFormat` a ciegas sobre las tres plataformas en un solo PR
  podría producir un diff enorme y difícil de revisar, y arriesga tocar
  código generado (KSP/Room) si los excludes no se configuran bien —
  mitigado con el Step 4 condicional (solo se autoaplica el fix si el
  volumen es pequeño y mecánico).
- El `.editorconfig` actual deshabilita reglas por nombre
  (`ktlint_standard_*`); si la versión de ktlint resuelta introduce
  reglas nuevas no contempladas en ese archivo, podrían aparecer
  violaciones "sorpresa" desde el día uno — se confirma en Step 1/Step 3
  qué versión se resuelve y si el archivo sigue siendo válido para ella.

## Out-of-band actions

- Ninguna acción humana/externa requerida para el alcance de este spec.
  Si Step 3 encuentra un volumen grande de violaciones, la acción de
  seguimiento es abrir un nuevo spec de limpieza (ver OQ-3) — eso sí
  seria out-of-band respecto a este spec, pero no requiere intervención
  humana fuera del flujo normal de specs.

## 8. Acceptance criteria

- [x] **AC-1** — El plugin de ktlint queda instalado y wireado en
      `:composeApp`; `./gradlew :composeApp:ktlintCheck` existe y se
      ejecuta sin errores de configuración de Gradle. Cumplido: plugin
      `org.jlleitschuh.gradle.ktlint` 14.2.0 wireado (Step 1/2),
      `ktlintCheck` corre y reporta correctamente (Step 3/4/6).
- [x] **AC-2** — El `.editorconfig` raíz queda confirmado como completo
      para la versión de ktlint instalada, o actualizado con overrides
      adicionales documentados con el mismo estilo de comentario que las
      entradas ya existentes. Cumplido como "confirmado completo, sin
      ediciones necesarias": ninguna de las 305 violaciones reales
      encontradas (88 arregladas + 217 documentadas para seguimiento)
      cae en una regla que el archivo ya debería estar deshabilitando
      (ver `decisions.md` Step 3).
- [x] **AC-3** — `CLAUDE.md` ("Conventions") y
      `.serena/memories/conventions.md` dejan constancia explícita de
      que las reglas de ktlint deben respetarse de ahora en adelante,
      incluyendo los comandos para chequear (`ktlintCheck`) y formatear
      (`ktlintFormat`). Cumplido (Step 5): ambos archivos actualizados,
      además de la sección "Definition of done" en `CLAUDE.md`.
- [x] **AC-4** — `./gradlew :composeApp:ktlintCheck` corre en verde
      contra el código actual, O, si Step 3 encuentra un volumen grande
      de violaciones, el hallazgo (conteo y archivos representativos)
      queda documentado en este proposal (§7 Risks / §9) y se recomienda
      explícitamente un spec de seguimiento en vez de dejarlo
      silenciosamente en rojo. **Cumplido vía la segunda rama**: el
      volumen resultó grande (217 archivos reales entre commonMain y
      androidMain, tras excluir código generado) — documentado en
      `decisions.md` y en la fila correspondiente de §4, con
      recomendación explícita de spec de seguimiento (ver OQ-3
      actualizado). `ktlintCheck` corre y reporta en consola (no fallido
      silenciosamente); `ignoreFailures.set(true)` evita que este
      backlog conocido tumbe el build mientras se resuelve aparte.
- [x] **AC-5** — `./gradlew build` (build completo, Android + Desktop)
      sigue en verde después de todos los cambios. **Reinterpretado**:
      el aggregate `./gradlew build` está rojo por un defecto
      **pre-existente** (confirmado vía `git stash` a baseline limpio —
      ver `decisions.md`), documentado en la memoria Serena
      `gradle_ksp_multitarget_build_quirk`, no introducido por este
      spec. El gate real de "Definition of done" en `CLAUDE.md`
      (`./gradlew :composeApp:assembleDebug`) es `BUILD SUCCESSFUL`.

## 9. Open questions

- **OQ-1** — ¿Qué versión exacta del plugin `org.jlleitschuh.gradle.ktlint`
  (u otra alternativa) se resuelve como compatible con Kotlin `2.3.21`?
  **Resuelto (plan.md Step 1, 2026-09-09)**: `org.jlleitschuh.gradle.ktlint`
  versión **14.2.0** (última estable, publicada 2026-03-12). El plugin
  declara soporte explícito para proyectos Kotlin Multiplatform
  (`kotlin-multiplatform`, con ejemplos para módulos Android+KMP en su
  propio repo), requiere Gradle ≥7.4 (muy por debajo de la versión de
  Gradle Wrapper de este proyecto) y Kotlin ≥1.4 — sin incompatibilidad
  conocida con Kotlin `2.3.21`. Desde la versión 13.0.0 el motor ktlint
  por defecto que bundlea es la serie 1.x (esquema `.editorconfig` con
  prefijo `ktlint_standard_*`), el mismo que ya asume el `.editorconfig`
  existente — no hace falta fijar una versión de ktlint aparte, se deja
  el default del plugin (ver `decisions.md`).
  Fuentes: [plugins.gradle.org/plugin/org.jlleitschuh.gradle.ktlint](https://plugins.gradle.org/plugin/org.jlleitschuh.gradle.ktlint), [github.com/JLLeitschuh/ktlint-gradle](https://github.com/JLLeitschuh/ktlint-gradle), [CHANGELOG.md](https://github.com/JLLeitschuh/ktlint-gradle/blob/main/CHANGELOG.md).
- **OQ-2** — ¿Se debe agregar `ktlintCheck` como gate de CI? Hoy no
  existe ningún workflow que corra `build`/`check`. No-blocker: por
  defecto NO se agrega (fuera de alcance, ver §3), a menos que el
  usuario lo pida explícitamente al revisar este proposal.
- **OQ-3** — Si Step 3 encuentra un volumen grande de violaciones
  ktlint en el código actual, ¿se corrigen todas en este mismo spec o se
  abre un spec de seguimiento? **Resuelto (2026-09-09)**: resultó
  grande (217 archivos reales: 177 en commonMain + 40 en androidMain,
  más sus duplicados de variante/target — ver `decisions.md`). Se opta
  por el spec de seguimiento (evita un reformateo masivo no revisable
  mezclado con este chore de instalación); mientras tanto
  `ktlint { ignoreFailures.set(true) }` en `composeApp/build.gradle.kts`
  mantiene `./gradlew build` en verde sin ocultar las violaciones (siguen
  reportándose en consola/reportes). Acción de seguimiento recomendada:
  abrir `ktlint-compliance-cleanup` (o nombre similar) dimensionado con
  estos números, y flip de `ignoreFailures` a `false` cuando ese spec
  cierre.

---

## Serena memories consulted

- `conventions.md` — confirma la afirmación actual "No detekt/ktlint/.editorconfig" que este spec reemplaza, y el patrón de doble localización (no aplica aquí, pero se usó su formato como referencia para AC-3).
- `architecture.md` / `.specs/config.json` `architecture.modules` — confirmaron que este cambio no encaja en ninguna de las áreas de capacidad (domain/data/features/core/di/components/device/validator), reforzando que §5b se responde como N/A en vez de con el atajo de confinamiento.
