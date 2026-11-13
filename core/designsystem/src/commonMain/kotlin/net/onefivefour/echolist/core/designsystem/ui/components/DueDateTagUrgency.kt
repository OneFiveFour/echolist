package net.onefivefour.echolist.core.designsystem.ui.components

/**
 * Urgency level used by the shared due-date pill to pick its colors.
 *
 * This mirrors the task-domain urgency concept but lives in the design system so the
 * urgency -> theme-color mapping can be shared without the design system depending on
 * `core:tasks`. Callers that already have a domain urgency value map it onto this enum.
 */
enum class DueDateTagUrgency { Normal, Warning, Overdue }