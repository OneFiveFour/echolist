package net.onefivefour.echolist.core.tasks.domain.model

import net.onefivefour.echolist.core.deeplinks.domain.DeepLinkDestination

/** Deep-link outcome asking the app to show an existing task list; the app decides how to navigate. */
data class OpenTaskListDestination(
    override val accountId: String,
    val taskListId: String
) : DeepLinkDestination
