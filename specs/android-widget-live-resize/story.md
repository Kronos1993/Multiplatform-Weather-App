---
spec_id: android-widget-live-resize
source: manual
source_ref: dictado por el usuario en chat, 2026-08-18
fetched_at: 2026-08-18
fetched_by: /spec-new
---

# Story: Redimensionado en vivo de los widgets de Android (drag-resize)

<!--
RAW INTAKE — DO NOT EDIT AFTER FETCH

This file preserves the source as it arrived, so future readers can
audit what /spec-plan worked from. /spec-plan reads this and produces
proposal.md; story.md is append-only thereafter.
-->

## Metadata

| Field | Value |
|-------|-------|
| Type | story |
| Priority | |
| Created | 2026-08-18 |

## Description

Vamos a redactar una historia para que los widgets sean más responsivos. No es eliminar los que ya hay, sino que sean responsive y se cambien según el usuario los agranda o disminuye.

Alcance según el usuario, aclarado durante el intake: no eliminar ni reemplazar los widgets existentes (Small/Medium/Large/SmallWithDigitalClock/SmallWithAnalogClock) — deben seguir funcionando. El objetivo es que, cuando el usuario agranda o achica un widget ya colocado en la pantalla de inicio (arrastrando sus manijas de resize), el layout/contenido se adapte dinámicamente al nuevo tamaño en vez de quedar fijo al tamaño con el que fue colocado originalmente.

Contexto de origen: esta historia es el seguimiento explícito de **OQ-1** en la historia ya archivada `specs/_archive/android-widget-responsive-sizing/proposal.md` (PR #17, mergeada 2026-08-18). Esa historia cubrió tipografía/iconos/espaciado responsivos entre las 3 variantes instalables (Small/Medium/Large) pero excluyó deliberadamente el redimensionamiento **en vivo** de un widget ya colocado, porque `BaseWeatherWidget` usa el `SizeMode.Single` por defecto de Glance en vez de `SizeMode.Responsive`/`SizeMode.Exact`. OQ-1 la dejó marcada como seguimiento no bloqueante, no bloqueante para esa spec, pero sí el ask de esta.

No se dieron criterios de aceptación explícitos adicionales más allá de lo dictado arriba — `/spec-plan` deberá refinarlos, apoyándose en el análisis técnico ya hecho en OQ-1 (migración a `SizeMode.Responsive`/`SizeMode.Exact` como mecanismo probable) y en los componentes ya existentes en `widget/components/WidgetComponents.kt` / `WidgetTheme.kt` (incluyendo el sistema de tokens de tipografía/espaciado por `WidgetSizeClass` ya construido en la spec anterior).

## Acceptance criteria (as written in source)

<!-- No se dieron AC explícitos en el formato fuente; /spec-plan debe
     derivarlos en proposal.md §8 a partir de la Description y del
     análisis de OQ-1 en la spec archivada. -->

-

## Comments / discussion

(none — file/manual/url intake, no comment thread)

## Attachments

-

## Links

- `specs/_archive/android-widget-responsive-sizing/proposal.md` — spec previa, ver OQ-1 (redimensionado en vivo, excluido de esa spec)
- PR #17 (https://github.com/Kronos1993/Multiplatform-Weather-App/pull/17) — implementación de la spec previa (tipografía/iconos/espaciado por variante)
