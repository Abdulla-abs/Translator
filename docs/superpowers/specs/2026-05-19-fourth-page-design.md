# Fourth Page (About) — Design Spec

**Date:** 2026-05-19  
**Source:** `androidPages/fourth_page/code.html`  
**Route:** `RootRoute.About` → `AboutScreen`

## Goal

Replace the placeholder About screen with a scrollable marketing-style page aligned with the fourth_page mockup: hero branding, open-source narrative, license grid, acknowledgments, and footer.

## Sections

1. **Hero** — App icon, name, version (`appVersionLabel()`), status chips (PRODUCTION READY, KOTLIN 2.3), privacy note (Chinese).
2. **Project row** — Open Source card + GitHub link; Building Together visual card (gradient, no external image).
3. **Licenses** — Featured 3-up grid (Ktor, Compose Multiplatform, xmlutil); “View All Licenses” dialog with extended list.
4. **Acknowledgments** — Avatar initials row + org grid (@jetbrains, etc.).
5. **Footer** — Kotlin community tagline.

## Platform

- `rememberOpenExternalUrlHandler()` for GitHub link (JVM Desktop / Android Intent / iOS openURL).
- Reuse `AppGlassCard`, `AppSpacing`, Material3 theme tokens.

## Out of scope

- Top app bar / bottom nav (owned by `AppRoot`).
- Real contributor photos or network images.
