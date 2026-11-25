package net.onefivefour.echolist.core.deeplinks.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import net.onefivefour.echolist.core.deeplinks.domain.DeepLink

/** Android transport for [DeepLink]s: explicit launcher intents and string round-trips for saved state. */
object DeepLinkIntents {
    private const val SCHEME = "echolist"
    private const val ACTION = "net.onefivefour.echolist.action.OPEN_DEEP_LINK"

    // Notifications posted by earlier versions still carry this action.
    private const val LEGACY_ACTION = "net.onefivefour.echolist.action.OPEN_TASK_LIST"

    fun create(context: Context, link: DeepLink): Intent? =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            action = ACTION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data = toUri(link)
        }

    fun parse(intent: Intent): DeepLink? {
        if (intent.action != ACTION && intent.action != LEGACY_ACTION) return null
        return intent.data?.let(::fromUri)
    }

    fun toUriString(link: DeepLink): String = toUri(link).toString()

    fun parse(uriString: String): DeepLink? = fromUri(Uri.parse(uriString))

    private fun toUri(link: DeepLink): Uri = Uri.Builder()
        .scheme(SCHEME)
        .authority(link.host)
        .apply { link.segments.forEach(::appendPath) }
        .build()

    private fun fromUri(uri: Uri): DeepLink? {
        if (uri.scheme != SCHEME) return null
        val host = uri.authority?.takeIf { it.isNotBlank() } ?: return null
        return DeepLink(host = host, segments = uri.pathSegments)
    }
}
