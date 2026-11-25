package net.onefivefour.echolist.core.deeplinks.di

import net.onefivefour.echolist.core.deeplinks.domain.DeepLinkHandler
import org.koin.dsl.module

/** Modules contribute with `single { MyTarget() } bind DeepLinkTarget::class`. */
val deepLinkModule = module {
    single { DeepLinkHandler(targets = getAll()) }
}
