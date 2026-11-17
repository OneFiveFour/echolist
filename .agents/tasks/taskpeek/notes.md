# TasksSummary implementation notes

Initial implementation (no review.json present).

## What was built
- core:tasks: `MainTasksSummary` domain model, `TaskListRepository.listMainTasksSummary(parentDir)`,
  remote data source method on path `/tasks.v1.TaskListService/ListMainTasksSummary`, and
  `TaskListMapper` proto->domain mapping (empty due_date -> null, invalid non-empty -> SerializationError).
- features/browser: `TasksSummaryViewModel` (inline toggle + load, hide-done + group-by-list preserving
  server order via `groupVisibleTasks`), `TasksSummaryUiState`, and stateless UI
  (`TasksSummaryOverview` with LazyColumn sticky headers, `TasksSummaryItemRow` with non-interactive checkbox,
  single-line ellipsized title, right-aligned due date, empty/loading/error states).
- BrowserScreen swaps FileOverview <-> TasksSummaryOverview based on `tasksSummaryUiState.isVisible`.
  Toggle wired internally in BrowserFeature (App.kt no longer passes a no-op onTasksSummaryClick).
- Test doubles updated across core:tasks and features/tasklist, features/reminderssummary.

## Verification (all passing)
- ./gradlew verifyArchitecture
- ./gradlew :core:tasks:compileKotlinJvm :core:tasks:jvmTest
- ./gradlew :features:browser:compileKotlinJvm :features:browser:jvmTest
- ./gradlew :app:compileKotlinJvm

## Pre-existing (not caused by this change)
- detekt (MagicNumber in KMP test source sets) and ktlint (final-newline / argument-list-wrapping)
  already fail repo-wide on committed files (e.g. UpcomingMainTasksRepositoryTest, BottomNavigation.kt,
  UpcomingMainTask.kt, NotificationPreview.kt, TaskReminderIntegrationTest.kt).
  New TasksSummary files were written to match the repo convention (no final newline per .editorconfig).
