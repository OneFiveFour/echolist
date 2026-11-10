# EchoList module architecture

EchoList targets Android, Desktop/JVM, JavaScript and WasmJS. Each feature is a Kotlin Multiplatform library in the same Gradle multi-project build. iOS targets and the Xcode application have been removed.

## Structure

```text
apps/
  android/          Android activity, manifest, signing and packaging
  desktop/          Desktop entrypoint and native packaging
  web/              JS/Wasm entrypoint and HTML
app/                Composition root, session state and Navigation 3
features/
  browser/          Folder listing and folder operations; previously Home
  tasklist/         Task-list editor and autosave
  note/             Note editor, cache access and offline synchronization
  login/            Login form and validation
  tasksettings/     Due date, recurrence form and notification preferences
  tasknotifications/ Upcoming dated MainTasks and device-local notification toggles
core/
  designsystem/     Theme, fonts, shared UI components and icons
  files/            Paths, directory-change notifications, desktop storage paths
  tasks/            Shared task models, repository, reminder/completion rules
  recurrence/       Recurrence rule data and RRULE conversion
  notifications/    Platform scheduling, permission handling and task-action port
  session/          Authentication, token storage, refresh and session events
  networking/       Ktor/ConnectRPC, logging, errors and backend URL contract
  protocol/         Wire-generated messages from /proto
  database/         SQLDelight schema and platform drivers
build-logic/        Shared Kotlin/Compose library conventions and architecture check
```

Within a feature, packages use `net.onefivefour.echolist.feature.<feature>` and the four familiar categories:

| Package | Responsibility |
|---|---|
| `domain` | Feature-owned models, operation parameters and repository contracts |
| `data` | Implementations, remote/cache access and mapping |
| `di` | One public Koin module wiring the feature |
| `ui` | Public feature composable, internal ViewModels, states and components |

Only populated layers have packages. Login reuses authentication from `core:session`; tasklist and tasksettings reuse task contracts from `core:tasks`. Empty repositories, use cases or forwarding classes are not added merely to fill a layer. Feature-specific data remains in its feature; sharing requires an actual cross-feature responsibility.

## Dependency rules

```text
platform apps -> app -> features -> core
                   \------------> core
platform apps ------------------> core (platform integration only)
```

- Features never depend on other features. They expose `BrowserFeature`, `TaskListFeature`, `NoteFeature`, `LoginFeature`, `TaskSettingsFeature`, `TaskNotificationsFeature` and their Koin modules.
- The app owns routes and navigation; features receive values, event callbacks or a typed result stream/sink.
- Core modules never depend on features or the app. Core-to-core dependencies form a directed acyclic graph.
- `ui` depends on domain contracts, not data implementations. `data` implements domain contracts; `di` connects both.
- Domain packages do not import Compose, platform UI, Koin, Ktor, SQLDelight, generated protocol messages, `data`, `di` or `ui`.
- Feature implementation types are `internal`. Runtime dependencies use `implementation`; dependencies appearing in public entrypoints use `api` where needed.
- `verifyArchitecture` checks Gradle project dependencies, cycles and production import boundaries. It is attached to root `check` and release validation. Kotlin visibility provides an additional boundary.

Principal core dependencies:

| Module | Other production core modules |
|---|---|
| `designsystem`, `files`, `protocol`, `recurrence`, `notifications` | None |
| `database` | `files` on Desktop |
| `networking` | None |
| `session` | `networking`, `protocol`; `files` on Desktop |
| `tasks` | `networking`, `protocol`, `files`, `notifications` |

Notification code calls `TaskCompletionHandler`; `core:tasks` supplies its implementation. Thus notification infrastructure does not depend on task repositories. Login/refresh use a separate unauthenticated HTTP client, while normal requests use the authenticated client; dependency resolution has no auth-client cycle.

## Navigation and results

`app` defines `BrowserRoute`, `EditNoteRoute`, `EditTaskListRoute`, `MainTaskSettingsRoute` and `TaskNotificationsRoute`. The browser route preserves the former serialized HomeRoute name for saved-state compatibility.

Every task-list navigation entry has a persistent `editorId`. The app's `TaskSettingsChannels` ViewModel owns one queue per editor, surviving configuration changes. Settings send `TaskSettingsChanges` to the calling editor's sink. This supports unsaved task IDs, queues results while the editor is covered, and isolates two editors of the same task. Popping an editor or logging out closes its queue; a late permission response cannot recreate it. The former application-wide result bus exists only as a local test fixture.

The Task Notifications screen is a separate feature reached from the browser's bell button.
It lists open MainTasks with a due date strictly after today's UTC calendar date, whether
recurring or one-time. It uses `TaskListRepository.listUpcomingMainTasks(fromDate)` in
`core:tasks`, backed by `TaskListService/ListUpcomingMainTasks`, with tomorrow's UTC date
as the inclusive cutoff. The API excludes completed and undated tasks; the repository
preserves server order and fails the whole query on invalid due dates. There is no folder
traversal or per-task-list fetch for this screen. The UI displays each task's next due date
as the shared design-system pill with localized relative time and urgency colors, and sorts
tasks by due date ascending, preserving server order for equal dates. Tasklist uses the same
pill and platform formatters. The upcoming response supplies the optional `is_recurring`
flag, which is preserved in the shared domain model and drives the pill's recurrence icon.
A missing flag from an older backend remains unknown and does not display the icon.

The screen reads and writes device-local preferences through `NotificationCoordinator`
and requests the existing notification synchronization after enabling a task. Disabled
upcoming tasks remain visible so notifications can be enabled again. This screen's future
cutoff does not change reminder synchronization, which also handles overdue tasks for
offline catch-up. A future upcoming-tasks screen can reuse the same `core:tasks` query
and open tasksettings through the app without depending on `features:tasklist`.

## Device reminders

`core:notifications` owns device-local preferences, reminder metadata, delivery history and synchronization diagnostics. Android and Desktop use a dedicated SQLDelight `NotificationDatabase` (`echolist-notifications.db` on Android, `~/.echolist/notifications.db` on Desktop); browsers persist the same snapshot in localStorage. This database is independent of the note cache in `core:database`. Preferences are scoped by backend/user and task ID, and survive process restarts. The existing default for tasks without an explicit local preference remains enabled; the future first-login wizard can set those preferences explicitly.

`NotificationCoordinator` serializes preference changes, task-list updates, alarm delivery and session transitions. A revision and outstanding-mutation guard prevent an older remote snapshot from overwriting more recent local changes. Only a fully validated successful snapshot can cancel missing tasks. Failed fetches retain existing schedules and permit offline catch-up from stored metadata. Each task keeps its delivered due dates, so a repeated refresh does not notify again for the same occurrence. Already visible notifications survive ordinary cleanup of old schedules; explicit task completion, disabling or logout dismisses them.

The shared `NotificationSyncHandler` is implemented in `core:tasks`, through a port owned by notifications; notifications never depends on the task repository. The notification-specific query requests the full supported date range from `0000-01-01`, rather than a moving today cutoff, so missed and remotely backdated open tasks are included. User-facing dates retain the calendar day from the protocol and become local-midnight instants only at the scheduling boundary. Stored synchronization timestamps and all transmitted dates use UTC conventions. Once due, a reminder is delivered at the next opportunity, including after a missed day or restored permission. Recurrence is optional; a valid due date and device preference are sufficient.

Android uses one inexact AlarmManager alarm per account/task, identified by a URI containing the full IDs, plus a unique daily WorkManager synchronization job targeted at the next local midnight. App login/resume and successful task-list mutations request the same synchronization. Periodic work has no network constraint so cached reminders can be delivered offline; network failures retry with WorkManager backoff. Boot, app replacement and clock/timezone broadcasts restore from persisted state and recalculate the next local midnight. Native alarms are validated against current account, preference, due date and delivery history before posting. Notification tags use the complete account/task identity; hash-based IDs are only used to retire legacy alarms.

Android notification actions enqueue unique persistent work constrained to network availability. Account and occurrence checks prevent late actions or retries from completing a different user or the next recurring occurrence. User notification/channel permissions remain authoritative. OS notification posting and database writes cannot form a single transaction: deterministic notification tags and `setOnlyAlertOnce` limit duplicate visible alerts in the small crash window after posting and before saving history.

Tapping an Android reminder opens its task list through an explicit activity PendingIntent.
The notification module owns the account/list payload; the Android launcher handles cold
starts and new intents, retaining unconsumed navigation across recreation and login.
The app checks the current account before navigating and reuses an existing task-list editor
when possible. This does not invoke completion; the separate Done action still enqueues
the existing completion worker. These internal links do not register a public URI handler.

Android debug builds additionally wire a long press on the task settings' "Erinnern" label
to a coordinator preview of the stored active reminder. It uses the same platform delivery
and actions, bypassing only due-date timing and delivery deduplication without changing
history or scheduled alarms. Account, permission and enabled-preference checks still apply.
The Android release source set supplies no callback, so release has no long-press gesture
or debug action implementation. Unsaved settings are not used for preview delivery.

Desktop and browser targets perform the shared reconciliation and local-midnight checks while the application is running, with catch-up after reopening. They do not gain Android-style background execution when the desktop process or browser tab is closed. Browser persistence currently assumes one active app instance per origin. No first-install wizard is included in this change.

## Platform and persistence boundaries

Every library convention defines Android, JVM, JS and WasmJS targets. Platform implementations remain in their corresponding source sets. Database schema/package, storage names and desktop storage locations are preserved. Note cache orchestration stays in `features:note`; database schema and drivers stay in `core:database`.

Database queries use SQLDelight's asynchronous API so the same cache works with Web Workers. `DatabaseProvider` waits for initialization once before publishing the database. Android and Desktop adapt the same schema to their synchronous drivers. Web bundles include the SQL.js worker and its Wasm binary; both browser targets have a real worker round-trip test. This follows the [SQLDelight worker setup](https://sqldelight.github.io/sqldelight/2.3.2/js_sqlite/sqljs_worker/) and [multiplatform asynchronous driver setup](https://sqldelight.github.io/sqldelight/2.3.2/js_sqlite/multiplatform/).

The Android notification receivers deliberately retain their original fully qualified names, `net.onefivefour.echolist.data.notification.TaskDoneReceiver` and `TaskReminderReceiver`, to preserve existing PendingIntent targets. Their source code belongs to `core:notifications`.

## Build and verification

Use `./gradlew` on Unix or `.\gradlew.bat` on Windows:

```text
verifyArchitecture
jvmTest
:features:browser:assemble
:features:tasklist:assemble
:features:note:assemble
:features:login:assemble
:features:tasksettings:assemble
:features:tasknotifications:assemble
:features:note:jvmTest
:core:database:jsBrowserTest
:core:database:wasmJsBrowserTest
:apps:android:assembleDebug
:apps:desktop:run
:apps:desktop:createDistributable
:apps:web:jsBrowserDevelopmentRun
:apps:web:wasmJsBrowserDevelopmentRun
:apps:web:jsBrowserDevelopmentWebpack
:apps:web:wasmJsBrowserDevelopmentWebpack
:apps:web:jsBrowserDistribution
:apps:web:wasmJsBrowserDistribution
```

A feature's `assemble` builds that library and required core libraries, without building the app or sibling features. `jvmTest` from the root executes tests across modules. Feature tests and resources live with their owners. Test doubles are local to each module, keeping test-only code out of production APIs. Calendar and persistence regressions test the shared settings contract on either side of the feature boundary.

Browser database tests require Chrome; set `CHROME_BIN` to its executable if it is not discovered automatically. Both production web distributions include `index.html` pointing to `web.js` and can be served from their respective `apps/web/build/dist/<target>/productionExecutable` directories.

`release-build.ps1` and `release-build.sh` target the platform projects and run `verifyArchitecture` plus all JVM tests unless explicitly skipped. Existing Detekt and ktlint tasks remain available.
