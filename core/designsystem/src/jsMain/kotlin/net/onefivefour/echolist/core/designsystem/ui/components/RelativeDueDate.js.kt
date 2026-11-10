package net.onefivefour.echolist.core.designsystem.ui.components

internal actual fun formatRelativeDueDate(value: RelativeDueDate, localeTag: String): String =
    formatRelativeTime(value.signedAmount, value.unit.name.lowercase(), localeTag, value.useNamedDay)

private fun formatRelativeTime(amount: Int, unit: String, localeTag: String, useNamedDay: Boolean): String =
    js("new Intl.RelativeTimeFormat(localeTag, {numeric: useNamedDay ? 'auto' : 'always'}).format(amount, unit)")
