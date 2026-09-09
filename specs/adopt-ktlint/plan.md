---
spec_id: adopt-ktlint
generated_by: /spec-plan
generated_at: 2026-09-09T00:00:00Z
---

> **Blockers**: none
> **Depends on**: none
>
> /spec-implement refuses to start while any blocker remains OR any
> dependency is not yet archived. To clear:
> - blocker → resolve the OQ in proposal.md §9, remove its ID from `blockers:`
> - dependency → wait for the depended spec to land in `specs/_archive/`,
>   then remove its ID from `depends_on:`
>
> Re-run /spec-plan after either to regenerate this banner.

# Plan: Instalar ktlint, validar el .editorconfig existente y documentar que sus reglas deben respetarse

## Strategy

Instalar primero el plugin (Step 1-2), después revisar el `.editorconfig`
existente contra la versión realmente resuelta (Step 3), corregir
violaciones solo si el volumen es pequeño y mecánico (Step 4,
condicional — evita convertir este chore en un reformateo masivo sin
dimensionar), dejar constancia escrita en la documentación (Step 5), y
cerrar con una verificación de build completo (Step 6). El orden importa:
no tiene sentido revisar compliance (Step 3) antes de que el plugin esté
realmente wireado (Step 2).

## Steps

### Step 1 — Confirmar plugin y versión de ktlint compatible [investigate]

- **Files / symbols**:
  - `gradle/libs.versions.toml` — versión actual de `kotlin` (`2.3.21`) y `agp` (`9.0.1`) a respetar como baseline de compatibilidad.
- **Question(s) to answer**:
  - ¿Qué versión estable de `org.jlleitschuh.gradle.ktlint` (o alternativa justificada) es compatible con Kotlin `2.3.21` y con un módulo KMP con `androidTarget` + `iosArm64/iosX64/iosSimulatorArm64` + JVM (desktop)? (resuelve OQ-1 de proposal.md)
  - ¿Esa versión del plugin usa el motor ktlint 1.x (esquema `.editorconfig` con prefijo `ktlint_standard_*`, el mismo que ya usa el `.editorconfig` existente) o requiere alguna opción explícita para activarlo?
- **Outputs to record**: versión elegida del plugin + motor ktlint, anotada en proposal.md §9 (resolución de OQ-1) y usada como valor concreto en Step 2. Marcador `Step 1 result: plugin_version = <valor>`.
- **Why**: sin esto no se puede escribir un número de versión real en el Version Catalog — evita instalar una versión incompatible con Kotlin 2.3.21 o con el esquema de reglas que el `.editorconfig` ya asume.

### Step 2 — Agregar el plugin de ktlint al Version Catalog y wirearlo en :composeApp [implement]

- **Skill**: direct edits
- **Area(s)**: build config
- **Files / symbols**:
  - `gradle/libs.versions.toml` — agregar entrada en `[versions]` (p. ej. `ktlint = "<Step 1 result>"`) y en `[plugins]` (`ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }`, o el id confirmado en Step 1).
  - `build.gradle.kts` (raíz) — agregar `alias(libs.plugins.ktlint) apply false` al bloque `plugins { ... }`, siguiendo el mismo patrón que el resto de plugins ahí declarados.
  - `composeApp/build.gradle.kts` — agregar `alias(libs.plugins.ktlint)` al bloque `plugins { ... }` existente (junto a `kotlinMultiplatform`, `androidApplication`, etc.).
- **Skill args / inputs**: none
- **Why**: sigue el patrón de wiring ya usado por todos los demás plugins de este proyecto (apply false en raíz, alias-apply en el módulo único `:composeApp`).
- **Verification**: `./gradlew :composeApp:ktlintCheck` resuelve como tarea válida (no falla por "task not found" ni por error de configuración de Gradle) — no hace falta que esté en verde todavía, solo que exista y corra.

### Step 3 — Revisar completitud del .editorconfig y medir compliance actual [investigate]

- **Files / symbols**:
  - `.editorconfig` (raíz) — comparar los overrides `ktlint_standard_*` existentes contra el ruleset real de la versión resuelta en Step 1.
  - Salida de `./gradlew :composeApp:ktlintCheck` (o `ktlintKotlinScriptCheck`/`ktlintCommonMainSourceSetCheck` según cómo el plugin particione las tareas) corrida contra `commonMain`, `androidMain`, `iosMain`, `jvmMain`.
- **Question(s) to answer**:
  - ¿El `.editorconfig` existente sigue siendo válido/completo para la versión de ktlint instalada, o hay reglas nuevas que el proyecto también querría deshabilitar con el mismo criterio ya documentado en el archivo (convención deliberada, no error)?
  - ¿Cuántas violaciones reales aparecen, y son mecánicas (espacios, imports, líneas en blanco) o requieren juicio (nombres, estructura)? Clasificar como `violation_scope = small` (arreglable de forma mecánica y acotada, p. ej. mediante `ktlintFormat` con revisión de diff) o `violation_scope = large` (requiere un spec de seguimiento dimensionado aparte).
- **Outputs to record**: conteo de violaciones + archivos representativos, anotado en proposal.md §7 Risks. Marcador `Step 3 result: violation_scope = small|large`. Si se detecta que falta un override en `.editorconfig`, se edita en este mismo step (edición de config, no de código fuente — no requiere Step 4).
- **Why**: dimensiona el Step 4 condicional y responde AC-2/AC-4 con evidencia real en vez de asumir que el archivo ya escrito por el usuario es suficiente.

### Step 4 — Corregir violaciones ktlint encontradas [implement, conditional]

- **Condition**: "Step 3 result: violation_scope = small"
- **Skill**: direct edits
- **Area(s)**: el/los área(s) de código fuente identificadas en Step 3 (`commonMain`/`androidMain`/`iosMain`/`jvmMain` — TBD hasta Step 3)
- **Files / symbols**:
  - TBD — la lista exacta de archivos la produce Step 3. Antes de aplicar, confirmar que ningún archivo generado (KSP/Room `build/generated/**`) está incluido en el alcance de `ktlintCheck`/`ktlintFormat` (excluir vía `ktlint { filter { exclude(...) } }` en `composeApp/build.gradle.kts` si el plugin no los excluye por defecto).
- **Why**: cierra el compliance real del código pedido en la historia, pero solo cuando el volumen es pequeño y mecánico — evita mezclar un reformateo masivo no revisable con un chore de instalación de tooling.
- **Verification**: `./gradlew :composeApp:ktlintCheck` verde después del fix; diff revisado a mano para confirmar que no se tocó ningún archivo generado ni se cambió comportamiento (solo estilo).

<!-- Si Step 3 resulta en violation_scope = large, este Step 4 se marca
     "skipped: large violation count — ver OQ-3, abrir spec de
     seguimiento (p. ej. ktlint-compliance-cleanup) para el reformateo
     dimensionado aparte". No se intenta arreglar todo aquí. -->

### Step 5 — Dejar constancia escrita de las reglas de ktlint [implement]

- **Skill**: direct edits
- **Area(s)**: documentación
- **Files / symbols**:
  - `CLAUDE.md` — sección "Conventions": reemplazar la línea *"No detekt/ktlint/`.editorconfig` — no automated style enforcement; match surrounding style."* por una que declare que ktlint SÍ está instalado y sus reglas (vía `.editorconfig`) deben respetarse, con los comandos `./gradlew :composeApp:ktlintCheck` / `./gradlew :composeApp:ktlintFormat`. También agregar la verificación de ktlint a la sección "Definition of done for a change".
  - `.serena/memories/conventions.md` — línea 13 (misma afirmación desactualizada): proponer la edición equivalente y esperar aprobación del usuario antes de escribir la memoria (regla de "Serena Memory Hygiene" de `CLAUDE.md`: mostrar el contenido propuesto, esperar aprobación, `write_memory`).
- **Skill args / inputs**: none
- **Why**: es el pedido explícito de "dejar constancia" — sin esto, la documentación seguiría contradiciendo la realidad del proyecto.
- **Verification**: ambos archivos ya no contienen la afirmación "no automated style enforcement"; el texto nuevo es consistente entre `CLAUDE.md` y la memoria Serena.

### Step 6 — Build completo en verde [verify]

- **What to check**: `./gradlew build` (Android + Desktop) después de todos los cambios anteriores; adicionalmente `./gradlew :composeApp:ktlintCheck` (verde, o documentado como pendiente de un spec de seguimiento si Step 3 fue `large`).
- **Pass criteria**: ambos comandos terminan con `BUILD SUCCESSFUL` (o, para `ktlintCheck`, terminan en verde salvo por violaciones ya documentadas explícitamente como fuera de alcance en proposal.md §7/§9 — nunca un fallo silencioso sin registrar).

## Dependencies

- Step 2 depende de la versión confirmada en Step 1.
- Step 3 depende de que el plugin ya esté wireado (Step 2) — no se puede correr `ktlintCheck` antes de que exista la tarea.
- Step 4 depende del resultado (`violation_scope`) de Step 3.
- Step 6 depende de que todos los steps anteriores (incluido el Step 4 condicional, corrido o explícitamente saltado) hayan terminado.

## Out-of-band actions

- Si Step 3 resulta en `violation_scope = large`, abrir un nuevo spec de
  seguimiento (p. ej. `ktlint-compliance-cleanup`) para el reformateo
  dimensionado — no requiere intervención humana fuera del flujo normal
  de specs, pero sí una decisión explícita del usuario sobre prioridad.

## Rollback

Todos los cambios son locales al working tree de esta rama (Version
Catalog, dos `build.gradle.kts`, `.editorconfig`, `CLAUDE.md`, y
opcionalmente archivos fuente si Step 4 corrió). Si algún step falla a
mitad de camino, `git restore` sobre los archivos listados arriba
revierte el spec por completo; no hay migración de base de datos ni
estado externo involucrado.
