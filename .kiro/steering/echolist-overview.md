# EchoList — App Overview & Core Principles

## What is EchoList?

EchoList is a cross-platform productivity app for organizing notes and task lists inside a hierarchical folder structure. Users authenticate against a self-hosted backend, then browse, create, edit, and delete folders, notes, and task lists. Think of it as a personal knowledge/task manager with a file-system metaphor.

## Target Platforms

Android, Desktop (JVM), Web (JS), Web (WasmJS) — all from a single Kotlin codebase.

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.x |
| UI framework | Compose Multiplatform + Material 3 |
| Navigation | AndroidX Navigation 3 (`androidx.navigation3`) |
| Networking | Ktor HTTP client → custom ConnectRPC client (protobuf over HTTP) |
| Serialization | Wire (protobuf code-gen), kotlinx.serialization (JSON / nav state) |
| Local cache | SQLDelight (multiplatform SQL) |
| DI | Koin |
| State management | Compose `State`, `StateFlow`, Koin-injected ViewModels |
| Testing | Kotest (framework + assertions + property-based testing) |
| Code quality | Detekt + ktlint |

## Architecture

The current module map and dependency rules are documented in `ARCHITECTURE.md` at the repository root.

`apps/* -> app -> features/* -> core/*`. Features never depend on each other; core never depends on features or app. Every feature uses `data`, `di`, `domain`, `ui` packages where it owns code. Libraries support Android, JVM, JS and WasmJS through build conventions in `build-logic`.

Shared responsibilities are split into focused core modules. In particular, task repositories are shared for the upcoming due-date screen, while note caching and browser folder operations belong to their features. `verifyArchitecture` enforces dependency direction, cycles and domain/UI boundaries.

### Data flow

1. UI layer observes `StateFlow` from ViewModels.
2. ViewModels call repository interfaces defined in `domain/repository/`.
3. Repository implementations in `data/repository/` coordinate between remote data sources and the local SQLDelight cache.
4. Remote data sources use `ConnectRpcClient` (a thin wrapper around Ktor) to talk protobuf over HTTP to the backend.
5. Wire-generated message classes live in `build/generated/wire` — never hand-edit them.

### Domain model

| Entity | Description |
|---|---|
| `Folder` | A directory node with a path and name |
| `Note` | A text document with title, content, and timestamp |
| `TaskList` | A named list of `MainTask` items (each with optional `SubTask`s) |
| `FileEntry` | A union type representing any item in a folder listing (folder, note, or task list) with type-specific `FileMetadata` |

### Navigation

Routes are `@Serializable` data classes/objects implementing `NavKey`:
- `BrowserRoute(parentDir)` — folder browser at a given path
- `EditNoteRoute(parentDir, noteId?)` — note editor
- `EditTaskListRoute(parentDir, taskListId?, taskListScreenId)` — task list editor
- `MainTaskSettingsRoute(..., taskListScreenId)` — settings returning changes to the owning task-list screen
- Authentication is a top-level app state; features do not own app routes.

The back stack is managed via `rememberNavBackStack` with a custom `SavedStateConfiguration`.

### Authentication

JWT-based. Tokens are stored in platform-specific `SecureStorage`. An `AuthInterceptor` Ktor plugin attaches the access token and handles refresh/expiry. Auth state (`Loading → Unauthenticated → Authenticated`) drives top-level UI switching in `App.kt`.

## Coding Principles

### Kotlin idioms
- Prefer `data class` for value types, `sealed interface` for closed hierarchies.
- Use `Result<T>` for operations that can fail — no thrown exceptions across layer boundaries.
- Coroutines everywhere; use `Dispatchers.Default` for repository work, never `Dispatchers.IO` in common code (it doesn't exist on all targets).
- Keep `expect`/`actual` declarations minimal — push platform differences to the edges.

### Compose UI
- All composables are stateless: receive state, emit callbacks.
- Access theme tokens exclusively through `EchoListTheme.*` (see `design-system.md` for details).
- Use `EchoListDimensions` tokens for all spacing — no magic `dp` literals.
- Screens are composed of small, focused composables (e.g., `FolderItem`, `NoteItem`, `BreadcrumbBar`).

### Dependency injection
- Each feature and shared concern exposes a Koin module; `app` aggregates them with platform bindings.
- ViewModels are created via `koinViewModel<T>()` in composables.
- Repositories are bound as interfaces → implementations (`single<FileRepository> { FileRepositoryImpl(...) }`).

### Networking
- Proto files live in `/proto` at the project root. Wire generates Kotlin message classes at build time.
- gRPC service stubs are pruned — the app uses a custom `ConnectRpcClient` interface instead.
- Never import or depend on generated gRPC service clients.
- Network configuration (base URL, timeouts, retries) is provided by `NetworkConfigProvider` backed by `SecureStorage`.

### Persistence
- SQLDelight database is named `EchoListDatabase` (package `net.onefivefour.echolist.cache`).
- Database drivers are provided per-platform via Koin (`DatabaseModule.android.kt`, etc.).
- Cache layer is accessed through `CacheDataSource` interface.

### Testing
- Use Kotest framework with JUnit 5 runner on JVM.
- Property-based tests use `kotest-property`.
- Mock HTTP responses with `ktor-client-mock`.
- Tests live in `commonTest` (shared) and `jvmTest` (JVM-specific with full Kotest runner).

### Build & quality
- Gradle with Kotlin DSL and a version catalog (`libs.versions.toml`).
- Detekt for static analysis, ktlint for formatting — both run on all source sets.
- Build commands: `./gradlew :apps:android:assembleDebug` (Android), `./gradlew :apps:desktop:run` (Desktop), `./gradlew :apps:web:wasmJsBrowserDevelopmentRun` (Web).
