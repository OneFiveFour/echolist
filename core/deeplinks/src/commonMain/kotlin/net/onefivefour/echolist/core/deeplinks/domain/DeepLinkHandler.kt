package net.onefivefour.echolist.core.deeplinks.domain

/** Asks every registered [DeepLinkTarget]; the first one that accepts the link wins. */
class DeepLinkHandler(private val targets: List<DeepLinkTarget>) {
    fun resolve(link: DeepLink): DeepLinkDestination? = targets.firstNotNullOfOrNull { it.resolve(link) }
}
