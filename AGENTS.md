# EchoList agent instructions

## Start here

- Read `ARCHITECTURE.md` for the current module map and dependency rules. Check `settings.gradle.kts` and the owning module's sources before choosing a location for changes.
- Read `CLAUDE.md` for additional repository context and `.kiro/steering/design-system.md` when changing UI.
- Current code and `ARCHITECTURE.md` take precedence over historical `.kiro/specs` and outdated package paths. Treat unchecked historical tasks as context, not a verified backlog.

## Modules and packages

- Dependency direction: `apps/* -> app -> features/* -> core/*`. Features never depend on other features; core never depends on app or features. Core dependencies must remain acyclic.
- `app` owns composition, authentication state, serializable routes and navigation result channels. `apps/android`, `apps/desktop`, `apps/web` own platform launchers and packaging.
- Feature packages are `net.onefivefour.echolist.feature.<feature>`; shared packages are `net.onefivefour.echolist.core.<concern>`. Use `domain`, `data`, `di`, `ui` only where they own code.
- Features are `browser`, `tasklist`, `note`, `login`, `tasksettings`. App code uses public feature entrypoints and Koin modules; implementation types remain `internal`.
- Shared task models, repository contracts, remote access and mappers belong to `core:tasks`, including queries used by a future upcoming-tasks feature. Do not put shared task data in `features:tasklist`.
- `core:protocol` generates Wire messages from root `proto/`; never edit generated output or introduce generated service clients. `core:networking` owns Ktor/ConnectRPC, while `core:session` owns authentication and authenticated client wiring.
- SQLDelight schema and platform drivers belong to `core:database`; note cache orchestration belongs to `features:note`. Task repositories currently use remote access without a task cache.
- `core:notifications` calls a task-action port implemented by `core:tasks`. Preserve the legacy Android receiver class names documented in `ARCHITECTURE.md`.
- Reminder preferences/history live in `core:notifications`, separate from the note cache. Use its coordinator for scheduling and changes; preserve account isolation, occurrence deduplication and offline catch-up. Due-date reminders do not require recurrence.
- Supported targets are Android, JVM desktop, JS and WasmJS. There is no active iOS target. `composeApp/` is not an included Gradle module; do not use leftover build output as source.

## Naming conventions

- Boolean variables, properties and parameters must use a meaningful predicate prefix such as `is`, `are`, `has`, `can` or `should` (for example `isDesktop`, `isPasswordVisible`, `hasErrors`). Apply this to inferred Boolean types, delegated state and tests as well as explicitly typed Booleans. Name Boolean-returning functions as predicates where appropriate. Preserve names required by external APIs and overrides.

## Boundaries and verification

- UI depends on domain contracts; data implements them; DI wires implementations. Domain must not import UI, platform UI, Koin, Ktor, SQLDelight, generated protocol messages or data implementations.
- Repository operations return `Result<T>`. Preserve coroutine cancellation. Common code uses an injected dispatcher or `Dispatchers.Default`, not `Dispatchers.IO`.
- Use `EchoListTheme.*`, shared design tokens and module-owned resources for UI.
- Keep tests and test doubles in the owning module. When extending a shared interface, update its implementations and test doubles across modules.
- On Windows use `.\gradlew.bat verifyArchitecture` and affected module tests, e.g. `:core:tasks:jvmTest`. Root `jvmTest` runs JVM tests across modules.
- Platform build entrypoints are `:apps:android:assembleDebug`, `:apps:desktop:run`, `:apps:web:jsBrowserDevelopmentRun` and `:apps:web:wasmJsBrowserDevelopmentRun`.
