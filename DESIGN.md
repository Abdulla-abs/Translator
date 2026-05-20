---
name: Developer-Centric KMP System
colors:
  surface: '#0b1326'
  surface-dim: '#0b1326'
  surface-bright: '#31394d'
  surface-container-lowest: '#060e20'
  surface-container-low: '#131b2e'
  surface-container: '#171f33'
  surface-container-high: '#222a3d'
  surface-container-highest: '#2d3449'
  on-surface: '#dae2fd'
  on-surface-variant: '#cac3d8'
  inverse-surface: '#dae2fd'
  inverse-on-surface: '#283044'
  outline: '#948ea1'
  outline-variant: '#494455'
  surface-tint: '#cdbdff'
  primary: '#cdbdff'
  on-primary: '#370095'
  primary-container: '#7f52ff'
  on-primary-container: '#fffdff'
  inverse-primary: '#6835e7'
  secondary: '#b4c5ff'
  on-secondary: '#002a78'
  secondary-container: '#0053db'
  on-secondary-container: '#cdd7ff'
  tertiary: '#ffb783'
  on-tertiary: '#4f2500'
  tertiary-container: '#b55d00'
  on-tertiary-container: '#fffcff'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e8deff'
  primary-fixed-dim: '#cdbdff'
  on-primary-fixed: '#20005f'
  on-primary-fixed-variant: '#4f02cf'
  secondary-fixed: '#dbe1ff'
  secondary-fixed-dim: '#b4c5ff'
  on-secondary-fixed: '#00174b'
  on-secondary-fixed-variant: '#003ea8'
  tertiary-fixed: '#ffdcc5'
  tertiary-fixed-dim: '#ffb783'
  on-tertiary-fixed: '#301400'
  on-tertiary-fixed-variant: '#703700'
  background: '#0b1326'
  on-background: '#dae2fd'
  surface-variant: '#2d3449'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-base:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  code-base:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  code-sm:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  label-caps:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
  gutter: 16px
  margin: 24px
---

## Brand & Style

This design system is engineered for efficiency, precision, and the technical rigor required by cross-platform developers. It targets a highly literate technical audience who values performance over decoration. 

The visual style is **Functional Minimalism** with a **Corporate Modern** foundation. It prioritizes information density and clarity, using a systematic approach to layout and hierarchy. The aesthetic is clean and rhythmic, evoking a sense of "IDE-native" familiarity while remaining modern enough for a standalone productivity tool. By blending high-contrast accents with a sophisticated neutral palette, the design system ensures that complex translation workflows feel structured and manageable.

## Colors

The palette is anchored by "Kotlin Purple" as the primary brand driver, symbolizing the cross-platform nature of the tool. "Developer Blue" is utilized as a secondary action color for primary interactive paths and utility-based calls to action. 

The system defaults to a deep slate **dark mode** to reduce eye strain during long development sessions, but it is built to support a high-contrast light mode.
- **Surface Strategy:** Use subtle tonal shifts (e.g., `#0F172A` to `#1E293B`) to differentiate between sidebars, editors, and modal layers.
- **Functional Colors:** Standardized success, warning, and error states provide immediate feedback on build or translation status.

## Typography

Typography is used to distinguish between "Content" and "Data." 
- **Inter** handles all UI labels, navigation, and instructional text, providing high legibility at small sizes.
- **JetBrains Mono** is reserved strictly for technical data: translation keys, XML/JSON snippets, file paths, and terminal output.

For mobile-responsive views, `display-lg` should scale down to 32px, while body text remains consistent at 16px to maintain touch-target readability.

## Layout & Spacing

This design system utilizes an **8px base unit** to ensure mathematical consistency across all platforms (iOS, Android, Web). 

- **Grid:** A 12-column fluid grid is used for desktop views. On tablet, this shifts to an 8-column layout, and on mobile, a 4-column layout with 16px side margins.
- **Information Density:** For data-heavy views (like translation tables), the spacing can be compressed to a 4px (XS) scale to maximize screen real estate.
- **Safe Zones:** Always maintain a minimum of 16px gutter between UI modules to prevent visual clutter in complex configurations.

## Elevation & Depth

Hierarchy is established through **Tonal Layering** supplemented by **Ambient Shadows**. 

1. **Background (Level 0):** The primary canvas color.
2. **Surface (Level 1):** Main content areas, navigation sidebars. Use a slightly lighter/darker tint than the background.
3. **Cards & Modules (Level 2):** Elevated via a subtle 1px border and a low-opacity shadow (e.g., `box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1)`).
4. **Modals & Tooltips (Level 3):** High elevation with a distinct backdrop blur (12px) to focus the developer's attention on the active task.

In dark mode, depth is primarily communicated via border-top highlights rather than heavy drop shadows to maintain a crisp look.

## Shapes

The shape language balance approachable modernism with professional rigidity. 
- **Containers & Cards:** Use a 12px (`rounded-lg`) corner radius to soften the technical nature of the content.
- **Buttons & Inputs:** Use an 8px (`base`) radius for a precise, "clickable" feel.
- **Selection Indicators:** Small indicators (like active tabs or focus states) use a 2px radius for extreme sharpness.

All shapes should follow a consistent 1.5px stroke width for outlines and dividers.

## Components

### Buttons & Controls
- **Primary Action:** Solid "Kotlin Purple" background with white text.
- **Segmented Controls:** Used for switching between translation strategies (e.g., Google, DeepL, Manual). The active state should have a slight elevation and a higher contrast background.
- **Toggle Switches:** Compact and high-contrast, following the secondary "Developer Blue" for the "On" state.

### File & Translation Cards
- File items are represented as cards with a `code-sm` font for the file extension and a linear progress bar indicating translation completion.
- Cards should change border color on hover to indicate interactivity.

### Progress Bars
- Linear and thin (4px height). 
- Use a subtle pulse animation for "In Progress" states and a static color change for "Complete."

### Input Fields
- Monospaced text for the input value. 
- 1px border that glows "Developer Blue" on focus. 
- Support for "Clear" actions and "Copy to Clipboard" icons within the field suffix.

### Icons
- Use **stroke-based icons** with a consistent 2px weight. Avoid filled icons unless used for "Warning" or "Error" status indicators.