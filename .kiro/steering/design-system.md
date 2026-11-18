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

`EchoListTheme.typeScale` exposes the six Figma levels. Its default is provided
by `echoListTypography()` and can be replaced through the `EchoListTheme`
`typeScale` parameter independently of the palette.

| Level | Font | Weight | Size / line height | Letter spacing |
|---|---|---|---|---|
| display | Space Grotesk | 650 | 40 / 44 sp | -1 sp |
| titleLarge | Space Grotesk | 620 | 28 / 34 sp | -0.7 sp |
| title | Space Grotesk | 600 | 20 / 26 sp | -0.5 sp |
| body | Work Sans | 400 | 16 / 24 sp | -0.4 sp |
| bodySmall | Work Sans | 400 | 14 / 20 sp | -0.35 sp |
| label | Work Sans | 600 | 13 / 16 sp | 0.52 sp |

All fonts are bundled in `composeResources/font/`. Space Grotesk's exact static
weights 600, 620 and 650 are instantiated from the Google Fonts variable font,
`ofl/spacegrotesk/SpaceGrotesk[wght].ttf`; its SIL Open Font License is bundled
alongside them. Static instances preserve the intermediate weights across all
four targets without relying on platform variable-font support or synthesis.

`EchoListTheme.typography` remains the Material adapter. All 15 Material slots
are explicitly mapped, with no default font fallback:

- displayLarge / displayMedium / displaySmall → display
- headlineLarge / headlineMedium / headlineSmall / titleLarge → titleLarge
- titleMedium / titleSmall → title
- bodyLarge / bodyMedium → body
- bodySmall → bodySmall
- labelLarge / labelMedium / labelSmall → label

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
shared `ElButton`. Form text sizes/tracking follow the screen's Figma values.
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
container/content colors remain `primary` / `onPrimary`. Other disabled buttons
retain their usual appearance; `ElButton` accepts explicit Material colors.
The URL's `https://` default, saved URL behavior and all business logic remain
unchanged.

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
| xxxl        | 40dp  |
| iconSmall   | 36dp  |
| iconMedium  | 40dp  |
| borderWidth | 1dp   |

Accessed via `LocalEchoListDimensions.current` inside composables.

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
