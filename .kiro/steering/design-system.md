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

`background` is identical in Material and EchoList color schemes. Use
`EchoListBackground` for the shared solid background and system bar insets.
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
