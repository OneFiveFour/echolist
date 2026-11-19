# EchoList Design System Rules

## Figma Reference

- File: EchoList (key: `jt6unsrw5ase8hVhZOWGdJ`)
- Foundation: node `6:1780` (colors and typography)
- This foundation replaces all historical palettes and gradient backgrounds.
- Individual screen redesigns follow separately; legacy component layouts below are not new Figma specifications.

## Technology Stack

- Language: Kotlin
- Framework: Compose Multiplatform (Android, JVM, JS, WasmJS)
- UI Library: Material 3 (`compose.material3`)
- Build: Gradle with Kotlin DSL, version catalog
- Styling: Compose `MaterialTheme` with custom `ColorScheme`, `Typography`, `Shapes`
- State: Compose `State`, `StateFlow`, ViewModel
- DI: Koin
- Testing: Kotest (framework, assertions, property-based testing)

## Project Structure

```text
core/designsystem/src/commonMain/
  kotlin/net/onefivefour/echolist/core/designsystem/
    ui/theme/         # EchoListTheme, colors, typography, shapes, dimensions
    ui/components/    # Shared composables
    di/               # ThemeManager wiring
  composeResources/   # Fonts and shared icons
features/<feature>/src/commonMain/
  kotlin/net/onefivefour/echolist/feature/<feature>/ui/
  composeResources/   # Feature-owned resources
```

See `ARCHITECTURE.md` for the complete module map. The former Home screen is now the browser feature.

## Color Tokens

The only bundled color theme is `EchoListDefaultTheme` (display name EchoList).
Future themes remain swappable through `ColorTheme` / `ThemeManager`.

| Role | Light | Dark |
|---|---|---|
| background | #F6F7F4 | #101312 |
| surface | #FFFFFF | #181C1B |
| surfaceVariant | #E8ECE8 | #252B29 |
| primary | #315F59 | #91D5C9 |
| onPrimary | #FFFFFF | #0B3934 |
| secondary | #635A78 | #CBC1E7 |
| onSecondary | #FFFFFF | #342D47 |
| onBackground | #1A1D1C | #E8ECE9 |
| onSurface | #1A1D1C | #E8ECE9 |
| onSurfaceVariant | #56605C | #BAC3BE |
| error | #B42318 | #FFB4AB |
| onError | #FFFFFF | #690005 |
| outline | #747E79 | #89938E |
| outlineVariant | #CAD1CD | #3E4743 |
| folderColor | #B65E18 | #FFB56B |
| noteColor | #7152B8 | #CBB2FF |
| taskColor | #16766C | #72D5C7 |
| statusNormal | #56716B | #9DB9B2 |
| warning | #D99A08 | #F4C95D |
| onWarning | #302000 | #362A00 |

`background` is identical in Material and EchoList color schemes. `EchoListTheme` provides the shared solid background and system bar insets.
The old gradient component and its three gradient tokens have been removed.

Every Material color role absent from Figma is explicitly **Color.Magenta** in
both modes, including container, inverse, tint, scrim and fixed roles. This is an
intentional diagnostic: do not substitute Material defaults or invent colors to
hide it. New role colors require a design decision.

## Typography

`EchoListTheme.typography` exposes Material 3 `Typography`, configured by
`material3Typography()` with the existing Figma fonts and values. It is passed
directly to `MaterialTheme` and can be replaced through the `EchoListTheme`
`typography` parameter independently of the palette. There is no separate
EchoList typography type or adapter.

| M3 role | Font | Weight | Size / line height | Letter spacing |
|---|---|---|---|---|
| displayLarge | Space Grotesk | 650 | 72 / 71 sp | -4.32 sp |
| displayMedium | Space Grotesk | 650 | 52 / 56 sp | -2.34 sp |
| displaySmall | Space Grotesk | 620 | 42 / 46 sp | -1.89 sp |
| headlineLarge | Space Grotesk | 620 | 38 / 44 sp | -1.71 sp |
| headlineMedium | Space Grotesk | 620 | 31 / 37 sp | -1.4 sp |
| headlineSmall | Space Grotesk | 620 | 28 / 34 sp | -0.7 sp |
| titleLarge | Space Grotesk | 600 | 24 / 30 sp | -0.6 sp |
| titleMedium | Space Grotesk | 600 | 20 / 26 sp | -0.5 sp |
| titleSmall | Work Sans | 600 | 15 / 20 sp | 0 sp |
| bodyLarge | Work Sans | 400 | 16 / 24 sp | -0.4 sp |
| bodyMedium | Work Sans | 400 | 15 / 22 sp | 0 sp |
| bodySmall | Work Sans | 400 | 13 / 20 sp | 0 sp |
| labelLarge | Work Sans | 600 | 15 / 20 sp | 0 sp |
| labelMedium | Work Sans | 600 | 12 / 16 sp | 0 sp |
| labelSmall | Work Sans | 600 | 10 / 15 sp | 0 sp |

Fonts are bundled in `composeResources/font/`; Space Grotesk includes exact static
weights 600, 620 and 650. All 15 Material roles are explicitly configured.

Use displaySmall for wide browser headings, headlineLarge/headlineMedium for
wide/narrow login headings, and headlineSmall for regular screen/editor titles.
Use titleLarge for dialogs, titleMedium for sections and titleSmall for card titles.
Note/task content uses bodyLarge; inputs and supporting descriptions use bodyMedium;
help, errors and previews use bodySmall. Buttons/navigation use labelLarge,
field labels/breadcrumbs/due tags use labelMedium, and counters use labelSmall.

Local typography overrides are limited to state (decoration, breadcrumb emphasis),
color/alignment, graphical text symbols and the editorial login hero. The hero scales
displayLarge proportionally and retains its special tracking and supporting text styles.

The label style does not uppercase content automatically; callers own text case.

## Adaptive layout (all four targets)

Use the official Compose Multiplatform Material 3 Adaptive APIs from
`org.jetbrains.compose.material3.adaptive:adaptive`, in `commonMain`.
`EchoListTheme` calls `currentWindowAdaptiveInfoV2()` and exposes its unmodified
`WindowAdaptiveInfo` as `EchoListTheme.adaptiveInfo`. This contains the official
`windowSizeClass` and `windowPosture`; no custom window measurement, breakpoint
calculation, Phone/Desktop enum or root layout wrapper is maintained.

The official width classes are Compact below 600 dp, Medium from 600 dp,
Expanded from 840 dp, Large from 1200 dp and Extra-large from 1600 dp. Use the
library's named constants and `isWidthAtLeastBreakpoint` /
`isHeightAtLeastBreakpoint`, not copied numeric thresholds in production code.
The former custom 720-dp boundary has been removed. Window information comes
from Compose's platform window APIs, reacts to native window/browser resizing,
rotation and density changes, and is independent of child-pane constraints.

Choose presentation per screen rather than reducing the shared information to
two global modes. Future navigation and list/detail screens should use the
official adaptive navigation/pane scaffolds where they fit their design. The
Login's editorial introduction is a screen-specific exception: it is shown at
Expanded width or greater; Compact and Medium share the form-only layout.
Additional size classes do not require inventing additional Login layouts.

Keep UI state and focus owners above adaptive branches. Login uses one form at
a stable composition location, with padding and arrangement defined in `ui.desktop`
and `ui.phone`, so input state, password visibility and focus survive size-class
transitions. Content scrolls when viewport/keyboard height is insufficient. Previews and renders only need `EchoListTheme`; resizing tests
must change platform `LocalWindowInfo` as well as render constraints, since the
official APIs intentionally use the window rather than a child layout's size.

## Login implementation

Figma page `4:779`: Desktop Light `10:2775`, Desktop Dark `10:3283`,
Phone Light `10:3537`, Phone Dark `10:3722`; error references `12:4305` and
`12:4504` specify the status-row arrangement. The logo and theme controls in
the error references are excluded by the user's explicit direction.

The introduction sidebar is the approved exception for manually specified
colors and editorial typography. It occupies 44% of the wide Login layout; its internal
heading and illustration scale down fluidly within that mode. The form is
capped at 448 dp, centered alongside the introduction and bottom-aligned in
the form-only layout. It uses
theme fonts, semantic colors, `ElFormTextField` with external labels and the
shared `ElButton`. Form typography follows the shared M3 roles above.
Shared theme shapes `large` (15 dp) and `extraLarge` (17 dp) support fields and
buttons. Existing legacy outlined fields and default pill buttons are retained
for screens whose redesign has not happened yet.

Field and authentication errors are collected into one summary with exactly
one error icon. The summary sits in a reserved area above Server address,
between the fixed heading and the fields; multiple messages appear as a list.
The reserved height is 96 dp scaled by the font scale, exists even without an
error, and scrolls internally for long messages. Neither the header, inputs nor
Connect changes position when errors or loading appear/disappear. Phone bottom
padding is independent of validation state. The whole form still scrolls in a
short viewport or above the keyboard.

Existing validation messages and authentication error mapping are preserved;
errors also remain associated with fields through accessibility semantics.
Focused fields use `primary`; error fields use `error`. During login, Connect
is disabled and its trailing arrow becomes a progress indicator, while its
container/content colors remain `primary` / `onPrimary`. Shared `ElButton` owns
these colors (including disabled states), the extra-large shape, medium shadow,
52-dp minimum height and content padding; callers supply behavior and content.
The URL's `https://` default, saved URL behavior and all business logic remain
unchanged.

Login form field gaps use `dimensions.l`, the action gap uses `dimensions.xl`,
and icons and control decorations use shared dimension tokens in both layouts.
Only heading typography and the outer form arrangement/padding adapt with `isDesktop`.
The desktop form has `xxxl` horizontal and `xxl` vertical padding; phone uses
`xl` side/top padding and `xxxl` bottom padding. The reserved error slot remains
96 dp (`xxxl * 2`) scaled by font scale. The editorial hero remains independent.

## Spacing & Sizing Tokens (EchoListDimensions)

| Token       | Value |
|-------------|-------|
| xxs         | 2dp   |
| xs          | 4dp   |
| s           | 8dp   |
| m           | 12dp  |
| l           | 16dp  |
| xl          | 24dp  |
| xxl         | 32dp  |
| xxxl        | 48dp  |
| iconSmall   | 36dp  |
| iconMedium  | 40dp  |
| borderWidth | 1dp   |
| formFieldMinHeight | 52dp |
| formFieldLeadingIconWidth | 22dp |
| buttonMinHeight | 52dp |
| buttonPaddingStart | 21dp |
| buttonPaddingEnd | 14dp |

Accessed via `LocalEchoListDimensions.current` inside composables.

`ElFormTextField` owns its sizing, spacing, colors and typography. It uses
`formFieldMinHeight` (52 dp) on all window sizes and grows with its content.
Callers supply content and input behavior rather than field heights.

## Shapes

| Shape  | Corner Radius |
|--------|---------------|
| small  | 8dp (s)       |
| medium | 12dp (m)      |
| large | 15dp |
| extraLarge | 17dp |

## Component Patterns

- All composables are stateless — they receive UI state and emit callbacks
- Use `EchoListTheme.materialColors` for all color references (not raw `MaterialTheme.colorScheme`)
- Use `EchoListTheme.typography` for all text styles (not raw `MaterialTheme.typography`)
- Use `EchoListTheme.dimensions` for spacing and sizing (not raw `LocalDimensions.current` or `LocalEchoListDimensions.current`)
- Use `EchoListTheme.shapes` for corner rounding (not raw `MaterialTheme.shapes`)
- Folder cards use primary-colored borders; file items use secondary-colored borders
- Icon containers in folder cards: primary background, small shape
- Icon containers in file items: 5% opacity primary background, small shape

## Theme Architecture

- `ColorTheme` data class holds name + light/dark `ColorScheme`
- `ThemeManager` holds available themes and selected theme as `StateFlow`
- `EchoListTheme` composable resolves light/dark variant and applies via `MaterialTheme`
- Adding a new theme = creating a new `ColorTheme` instance, no other code changes

## Figma-to-Code Guidelines

- Figma MCP outputs React + Tailwind — always convert to Kotlin Compose with MaterialTheme tokens
- Never install Tailwind or any web dependencies
- Map Tailwind colors to `MaterialTheme.colorScheme` roles
- Map Tailwind spacing to `EchoListDimensions` tokens
- Map Tailwind font classes to `MaterialTheme.typography` styles
- Reuse existing composables instead of duplicating
- Strive for 1:1 visual parity with the Figma design
