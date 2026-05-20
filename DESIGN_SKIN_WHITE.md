---
name: Minimalist Porcelain
colors:
  surface: '#f9f9fc'
  surface-dim: '#dadadc'
  surface-bright: '#f9f9fc'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f6'
  surface-container: '#eeeef0'
  surface-container-high: '#e8e8ea'
  surface-container-highest: '#e2e2e5'
  on-surface: '#1a1c1e'
  on-surface-variant: '#434656'
  inverse-surface: '#2f3133'
  inverse-on-surface: '#f0f0f3'
  outline: '#737688'
  outline-variant: '#c3c5d9'
  surface-tint: '#004ced'
  primary: '#003ec7'
  on-primary: '#ffffff'
  primary-container: '#0052ff'
  on-primary-container: '#dfe3ff'
  inverse-primary: '#b7c4ff'
  secondary: '#5b5f61'
  on-secondary: '#ffffff'
  secondary-container: '#e0e3e6'
  on-secondary-container: '#626567'
  tertiary: '#4c4e4f'
  on-tertiary: '#ffffff'
  tertiary-container: '#656666'
  on-tertiary-container: '#e4e5e5'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dde1ff'
  primary-fixed-dim: '#b7c4ff'
  on-primary-fixed: '#001452'
  on-primary-fixed-variant: '#0038b6'
  secondary-fixed: '#e0e3e6'
  secondary-fixed-dim: '#c4c7ca'
  on-secondary-fixed: '#191c1e'
  on-secondary-fixed-variant: '#44474a'
  tertiary-fixed: '#e2e2e2'
  tertiary-fixed-dim: '#c6c6c7'
  on-tertiary-fixed: '#1a1c1c'
  on-tertiary-fixed-variant: '#454747'
  background: '#f9f9fc'
  on-background: '#1a1c1e'
  surface-variant: '#e2e2e5'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.2'
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: '1.4'
    letterSpacing: 0.01em
  code-md:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  code-sm:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '400'
    lineHeight: '1.5'
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 40px
  container-max: 1440px
---

## Brand & Style
This design system is built upon the "Minimalist Porcelain" aesthetic—a philosophy that prioritizes clarity, breathability, and a high-fidelity tactile feel. The interface acts as a quiet, sophisticated canvas for the complex task of multiplatform translation. 

The target audience consists of developers and localization managers who require a tool that reduces cognitive load through expansive whitespace and a deliberate lack of visual noise. The emotional response is one of calm precision; the UI should feel as smooth and durable as polished porcelain while maintaining the high-energy utility of a professional development environment. The style is a hybrid of **Minimalism** and **Modern Corporate**, utilizing subtle depth to define functional areas without relying on heavy borders or intrusive dividers.

## Colors
The palette is dominated by "Porcelain White" and "Mist Gray," creating a high-key environment that feels airy and expansive. 

- **Primary:** An electric, professional blue (#0052ff) is used sparingly for call-to-actions, focus states, and progress indicators. It serves as the "active" pulse of the system.
- **Surface Strategy:** The primary background uses pure white (#ffffff), while secondary containers, sidebars, and inactive states use the soft gray (#f5f7fa) to create subtle tonal separation.
- **Semantic States:** Success and Warning states use high-vibrancy emerald and amber to ensure critical information is immediately scannable against the neutral backdrop.
- **Text Contrast:** Deep charcoal (#1a1c1e) is used for text rather than pure black to maintain a premium, softer reading experience.

## Typography
Typography in this design system is driven by the need for extreme legibility in technical contexts. **Inter** is the primary typeface, chosen for its neutral, tall x-height and exceptional performance on screens. 

- **Hierarchy:** Headlines use a semi-bold weight with slight negative letter-spacing to appear more compact and authoritative. 
- **Code Sections:** For translation keys, KMP metadata, and code snippets, **JetBrains Mono** is mandatory. It provides the necessary character distinction (like 0 vs O) essential for developers.
- **Scale:** The system uses a generous line-height (1.6) for body text to ensure that long translation strings remain easy to read and edit without eye strain.

## Layout & Spacing
The layout follows a **Fluid Grid** model with significant breathing room between major functional blocks.

- **Grid:** A 12-column system is used for desktop, collapsing to 4 columns for mobile.
- **Rhythm:** An 8px linear scale governs all padding and margins. 
- **Density:** To maintain the "airy" feel, a "Large" spacing preset (24px - 40px) is preferred between unrelated components, while internal component spacing remains tight (8px - 16px) to maintain grouping logic.
- **Adaptation:** On mobile, sidebars transition into bottom sheets or full-screen overlays to preserve the horizontal space needed for long-form translation text.

## Elevation & Depth
Elevation is achieved through **Ambient Shadows** rather than lines. This design system avoids heavy borders, preferring to use light-casting to define the Z-axis.

- **Surface Tiers:** The base layer is #ffffff. Floating elements (like cards or menus) use a very soft, diffused shadow (Blur: 20px, Spread: -4px, Opacity: 4% Black).
- **Interactive Depth:** Buttons and clickable cards should slightly "lift" on hover, increasing shadow spread, or "sink" slightly on press to simulate a tactile, physical response.
- **Focus:** Active input fields do not just change border color; they receive a soft 4px outer glow in the primary electric blue at 10% opacity to signal focus without harshness.

## Shapes
The shape language is defined by "Soft Precision." Following the `rounded-xl` specification, the system uses a 1.5rem (24px) corner radius for primary containers and cards.

- **Large Components:** Main content areas and cards use the 1.5rem radius.
- **Small Components:** Buttons, input fields, and chips use a smaller 0.5rem (8px) radius to maintain a sense of structural integrity for smaller targets.
- **Consistency:** Sharp corners (0px) are strictly prohibited, as they break the "Porcelain" metaphor. Even "ghost" borders or background tints should respect the rounded-xl hierarchy.

## Components
- **Buttons:** Primary buttons are solid Electric Blue with white text. Secondary buttons use a Mist Gray background with charcoal text. Both should have a minimum height of 48px to ensure accessibility.
- **Inputs:** Translation fields must be prominent. Use a Mist Gray background (#f5f7fa) for the container with no border in its default state. Upon focus, the background shifts to White (#ffffff) with a 2px Electric Blue stroke.
- **Cards:** White surfaces on Mist Gray backgrounds. Use the `rounded-xl` radius and the "elevation-low" ambient shadow.
- **Chips:** Used for "Language Tags" (e.g., EN, FR, DE). These are small, pill-shaped elements with a subtle 1px border and 12px Inter Medium text.
- **Translation Pairs:** A specialized component for this system. Use a split-view or stacked card layout where the "Source" and "Target" are clearly demarcated by a vertical Electric Blue accent line on the "Target" side to show active editing.
- **Lists:** Clean, borderless list items separated by 8px of whitespace rather than divider lines. Hover states are indicated by a shift to the secondary gray color.