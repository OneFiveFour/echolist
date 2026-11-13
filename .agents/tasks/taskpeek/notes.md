# TaskPeek implementation notes

Initial implementation (no review.json present).

## What was built
- core:tasks: `MainTaskPeek` domain model, `TaskListRepository.listMainTaskPeek(parentDir)`,
  remote data source method on path `/tasks.v1.TaskListService/ListMainTaskPeek`, and
  `TaskListMapper` proto->domain mapping (empty due_date -> null, invalid non-empty -> SerializationError).
- features/browser: `TaskPeekViewModel` (inline toggle + load, hide-done + group-by-list preserving
  server order via `groupVisibleTasks`), `TaskPeekUiState`, and stateless UI
  (`TaskPeekOverview` with LazyColumn sticky headers, `TaskPeekItemRow` with non-interactive checkbox,
  single-line ellipsized title, right-aligned due date, empty/loading/error states).
- BrowserScreen swaps FileOverview <-> TaskPeekOverview based on `taskPeekUiState.isVisible`.
  Toggle wired internally in BrowserFeature (App.kt no longer passes a no-op onTaskPeekClick).
- Test doubles updated across core:tasks and features/tasklist, features/tasknotifications.

## Verification (all passing)
- ./gradlew verifyArchitecture
- ./gradlew :core:tasks:compileKotlinJvm :core:tasks:jvmTest
- ./gradlew :features:browser:compileKotlinJvm :features:browser:jvmTest
- ./gradlew :app:compileKotlinJvm

## Pre-existing (not caused by this change)
- detekt (MagicNumber in KMP test source sets) and ktlint (final-newline / argument-list-wrapping)
  already fail repo-wide on committed files (e.g. UpcomingMainTasksRepositoryTest, BottomNavigation.kt,
  UpcomingMainTask.kt, NotificationPreview.kt, TaskReminderIntegrationTest.kt).
  New TaskPeek files were written to match the repo convention (no final newline per .editorconfig).
