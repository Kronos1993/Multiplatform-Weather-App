---
spec_id: adopt-ktlint
source: manual
source_ref: user request in chat, 2026-09-09
fetched_at: 2026-09-09
fetched_by: /spec-new
---

# Story: Instalar ktlint y verificar cumplimiento del proyecto

<!--
RAW INTAKE — DO NOT EDIT AFTER FETCH

This file preserves the source as it arrived, so future readers can
audit what /spec-plan worked from. /spec-plan reads this and produces
proposal.md; story.md is append-only thereafter.
-->

## Metadata

| Field | Value |
|-------|-------|
| Type | chore |
| Priority | |
| Created | 2026-09-09 |

## Description

Instale ktlint. Ya annadi un .editorconfig. Revisalo a ver si esta completo y deja constancia de que hay que respetar las reglas de ktlint. Revisa que el proyecto sea complaint con eso. Crea un spec.

(Traducción de trabajo para contexto de /spec-plan: instalar ktlint como
herramienta de estilo del proyecto —probablemente vía plugin de Gradle—,
revisar si el `.editorconfig` que el usuario ya añadió en la raíz del repo
está completo para las reglas de ktlint que se quieren aplicar/desactivar,
dejar constancia por escrito de que las reglas de ktlint deben respetarse
de aquí en adelante (documentación del proyecto: CLAUDE.md / conventions,
o donde /spec-plan determine que corresponde), y verificar que el código
actual del proyecto sea compliant con esas reglas —reportando o
corrigiendo violaciones encontradas.)

## Contexto relevado antes de crear este spec (no pedido explícitamente, hallazgos de reconocimiento)

- `.editorconfig` en la raíz del repo YA EXISTE (el usuario lo añadió antes
  de este mensaje). Contiene:
  - Reglas base para `*.{kt,kts}`: `indent_size = 4`, `insert_final_newline = true`,
    `max_line_length = 140`.
  - Overrides `ktlint_standard_*` deshabilitados con comentarios justificando
    cada uno:
    - `function-naming` (funciones `@Composable` en PascalCase — convención
      de Compose).
    - `package-name` (subpaquetes en snake_case bajo `features/`, convención
      estructural deliberada).
    - `filename` (archivos `expect`/`actual` nombrados según el `expect`,
      con sufijos `.android.kt`/`.ios.kt`/`.jvm.kt`, no según la clase
      `actual`).
    - Un bloque completo de reglas de wrapping/formato multilínea
      (`function-signature`, `class-signature`, `argument-list-wrapping`,
      `parameter-list-wrapping`, `multiline-expression-wrapping`,
      `chain-method-continuation`, `function-expression-body`,
      `if-else-wrapping`, `multiline-if-else`, `binary-expression-wrapping`,
      `enum-wrapping`, `when-entry-bracing`, `statement-wrapping`,
      `function-literal`, `property-wrapping`) deshabilitado como grupo por
      conflicto con el formateador por defecto de IntelliJ, con nota de
      "revisar si el proyecto alguna vez adopta `ktlint -F` como
      herramienta de formato de referencia".
- ktlint NO está instalado todavía como plugin de Gradle: no aparece en
  `build.gradle.kts` (raíz), `composeApp/build.gradle.kts`, ni en
  `gradle/libs.versions.toml`. Es decir, el `.editorconfig` fue preparado
  de antemano pero la herramienta que lo consume aún no está conectada al
  build.
- El nombrado `ktlint_standard_*` (con prefijo `ktlint_`) corresponde al
  esquema de configuración vía `.editorconfig` de ktlint 0.50+/1.x — implica
  que la versión de ktlint a instalar debería ser compatible con ese
  esquema (no la sintaxis antigua de archivo `.ktlint`).
- `CLAUDE.md` (raíz) declara hoy, en la sección "Conventions": *"No
  detekt/ktlint/`.editorconfig` — no automated style enforcement; match
  surrounding style."* — esta afirmación queda desactualizada en cuanto
  se instale ktlint y debe actualizarse como parte de este trabajo (es el
  lugar natural para "dejar constancia" de que las reglas de ktlint deben
  respetarse).
- El repo no tiene detekt ni suite de tests (confirmado en `CLAUDE.md` y
  memorias de Serena `task_completion`) — la verificación de "done" para
  este cambio será: build verde + ejecución de ktlint sin (o con)
  violaciones reportadas, no una suite de tests.
- No hay pipeline de CI visible en este repo (a confirmar en `/spec-plan`)
  donde ktlint deba engancharse como gate automático; si existe, debe
  evaluarse si el check de ktlint se agrega ahí también.

## Acceptance criteria (as written in source)

<!-- El usuario no listó AC explícitos con viñetas; se infieren de la
     descripción en prosa. /spec-plan debe refinarlos en proposal.md §8. -->

- ktlint queda instalado y ejecutable en el proyecto (comando de Gradle
  para correr el check, y opcionalmente el format).
- El `.editorconfig` existente queda revisado; si falta algo para que las
  reglas ktlint deseadas por el proyecto se apliquen correctamente, se
  completa.
- Queda constancia escrita (en la documentación del proyecto) de que las
  reglas de ktlint deben respetarse de ahora en adelante.
- Se verifica el cumplimiento (compliance) del código actual del proyecto
  contra ktlint, y se reportan o corrigen las violaciones encontradas.

## Comments / discussion

(none — file/manual/url intake, no comment thread)

## Attachments

-

## Links

-
