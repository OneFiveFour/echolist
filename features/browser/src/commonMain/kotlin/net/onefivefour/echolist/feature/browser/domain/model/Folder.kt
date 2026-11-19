package net.onefivefour.echolist.feature.browser.domain.model

/** [path] is relative and has no leading or trailing directory separators. */
internal data class Folder(
    val path: String,
    val name: String
)