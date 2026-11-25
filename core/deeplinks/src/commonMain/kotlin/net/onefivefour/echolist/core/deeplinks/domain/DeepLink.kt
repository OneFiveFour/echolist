package net.onefivefour.echolist.core.deeplinks.domain

/** Platform-neutral address of an in-app destination, e.g. `echolist://<host>/<segment>/<segment>`. */
data class DeepLink(val host: String, val segments: List<String>)

/** Outcome of resolving a [DeepLink]; concrete destinations are owned by the module that understands them. */
interface DeepLinkDestination {
    /** Account the link was issued for, or null if the destination is not account specific. */
    val accountId: String?
}

/** Implemented and registered by modules that can handle a family of deep links. */
fun interface DeepLinkTarget {
    /** Returns a destination if this target recognizes and accepts [link], otherwise null. */
    fun resolve(link: DeepLink): DeepLinkDestination?
}
