---
name: Geek Abyss
colors:
  surface: '#131313'
  surface-dim: '#131313'
  surface-bright: '#3a3939'
  surface-container-lowest: '#0e0e0e'
  surface-container-low: '#1c1b1b'
  surface-container: '#201f1f'
  surface-container-high: '#2a2a2a'
  surface-container-highest: '#353534'
  on-surface: '#e5e2e1'
  on-surface-variant: '#b9cacb'
  inverse-surface: '#e5e2e1'
  inverse-on-surface: '#313030'
  outline: '#849495'
  outline-variant: '#3a494b'
  surface-tint: '#00dbe7'
  primary: '#e1fdff'
  on-primary: '#00363a'
  primary-container: '#00f2ff'
  on-primary-container: '#006a71'
  inverse-primary: '#00696f'
  secondary: '#d7ffc5'
  on-secondary: '#053900'
  secondary-container: '#2ff801'
  on-secondary-container: '#0f6d00'
  tertiary: '#f7f8f8'
  on-tertiary: '#2f3131'
  tertiary-container: '#dbdbdb'
  on-tertiary-container: '#5e6060'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#74f5ff'
  primary-fixed-dim: '#00dbe7'
  on-primary-fixed: '#002022'
  on-primary-fixed-variant: '#004f54'
  secondary-fixed: '#79ff5b'
  secondary-fixed-dim: '#2ae500'
  on-secondary-fixed: '#022100'
  on-secondary-fixed-variant: '#095300'
  tertiary-fixed: '#e2e2e2'
  tertiary-fixed-dim: '#c6c6c7'
  on-tertiary-fixed: '#1a1c1c'
  on-tertiary-fixed-variant: '#454747'
  background: '#131313'
  on-background: '#e5e2e1'
  surface-variant: '#353534'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  code-lg:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  code-sm:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  label-caps:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.08em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 48px
  gutter: 16px
  container_max_width: 1440px
---

## Brand & Style

The design system is engineered for high-performance developer workflows, specifically focusing on Kotlin Multiplatform (KMP) translation. The aesthetic is "Technological Minimalism"—a refined take on the classic terminal environment. It evokes a sense of deep-focus "flow state" by utilizing a near-black canvas that reduces eye strain during long coding sessions.

The style is a blend of **Minimalism** and **Modern Brutalism**. It prioritizes information density and technical precision over decorative elements. Visual interest is generated through high-contrast accents and subtle luminescence rather than complex gradients or shadows. The interface should feel like a premium command-line interface: fast, reliable, and unyielding.

## Colors

The palette is anchored in absolute blacks to maximize the contrast of functional elements. 

- **Primary (#00f2ff):** Used for primary actions, focus states, and active translation keys. It represents the "system" state.
- **Secondary (#39ff14):** Reserved for success states, completed translations, and terminal-style prompts.
- **Neutral/Background:** The base is a pure Obsidian (#050505), with Surface layers in Deep Charcoal (#0a0a0a) to create subtle structural separation.
- **Borders (#1a1a1a):** This specific shade is used for all structural dividers, providing a "ghost" framework that is visible but non-distracting.

## Typography

This design system uses a dual-font strategy to distinguish between UI navigation and technical content.

- **Inter** is the workhorse for the interface, chosen for its exceptional legibility at small sizes and neutral character.
- **JetBrains Mono** is utilized for all data-rich elements, including translation keys, code snippets, metadata, and labels. This reinforces the developer-centric nature of the tool.

Hierarchy is established through weight and color rather than significant size shifts. Most UI text should remain between 12px and 14px to maintain high information density. Headers should be kept tight with negative letter spacing.

## Layout & Spacing

The system follows a strict **4px baseline grid**. Layouts are primarily fluid to accommodate long translation strings and variable code lengths, but they are constrained by a 1440px max-width container on desktop to ensure readability.

- **Grid:** A 12-column system is used for dashboard layouts, while translation editors use a 2-column or 3-column split view (Source | Target | Metadata).
- **Margins:** Standard outer margin is 24px on desktop, scaling down to 16px on mobile.
- **Density:** High density is preferred. Gutters should stay at a consistent 16px to maximize horizontal space for code side-by-side comparisons.

## Elevation & Depth

In this design system, depth is expressed through **layering and borders** rather than shadows. 

1.  **Z-0 (Base):** The #050505 background.
2.  **Z-1 (Surfaces):** Cards and panels use #0a0a0a with a 1px solid border of #1a1a1a.
3.  **Active State:** Elements being edited or focused gain a primary color (#00f2ff) border.
4.  **Luminescence:** Primary buttons and active indicators use a `drop-shadow` glow (e.g., `0 0 8px rgba(0, 242, 255, 0.4)`) to simulate a cathode-ray tube (CRT) or neon effect, making them pop against the dark void.

## Shapes

The shape language is sharp and geometric. All UI components use a `rounded-sm` (4px) corner radius. This provides just enough softness to prevent the UI from feeling aggressive while maintaining a precise, technical "blueprint" look. 

Buttons, input fields, and tags all share this consistent 4px radius. Circular shapes are strictly forbidden, except for status indicators or user avatars.

## Components

- **Buttons:** Primary buttons are filled Cyan (#00f2ff) with black text and a subtle glow. Secondary buttons are outlined with Cyan or Green. Ghost buttons use JetBrains Mono text with no background.
- **Input Fields:** Flat #0a0a0a background with a #1a1a1a border. On focus, the border changes to Cyan. Use JetBrains Mono for all input text.
- **Chips/Tags:** Used for KMP platform identifiers (e.g., `ios`, `android`, `desktop`). Small 1px borders, uppercase JetBrains Mono text.
- **Translation Cards:** Subtle #1a1a1a border. The source text is static, while the target text area has a slightly lighter background to indicate interactivity.
- **Scrollbars:** Custom thin scrollbars in #1a1a1a with a #333333 thumb to minimize visual noise.
- **Status Indicators:** Small 8px squares (not circles) using Secondary Green for "Synced" and Warning Amber for "Conflict".