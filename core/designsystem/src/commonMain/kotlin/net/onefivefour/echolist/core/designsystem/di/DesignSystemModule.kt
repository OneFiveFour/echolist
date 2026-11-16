package net.onefivefour.echolist.core.designsystem.di

import net.onefivefour.echolist.core.designsystem.ui.theme.ThemeManager
import net.onefivefour.echolist.core.designsystem.ui.theme.colorscheme.EchoListDefaultTheme
import org.koin.core.module.Module
import org.koin.dsl.module

val designSystemModule: Module = module {
    single {
        ThemeManager(
            availableThemes = listOf(
                EchoListDefaultTheme
            ),
            initialTheme = EchoListDefaultTheme
        )
    }
}