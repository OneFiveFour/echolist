package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey

/** Reuse a visited directory, or open it even when its ancestors are absent from the stack. */
internal fun MutableList<NavKey>.navigateToBrowserFolder(parentDir: String) {
    val index = indexOfLast { it is BrowserRoute && it.parentDir == parentDir }
    if (index >= 0) {
        while (size > index + 1) removeAt(lastIndex)
    } else {
        add(BrowserRoute(parentDir))
    }
}
