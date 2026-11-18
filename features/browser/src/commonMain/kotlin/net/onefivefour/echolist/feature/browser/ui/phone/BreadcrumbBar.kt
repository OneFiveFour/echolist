package net.onefivefour.echolist.feature.browser.ui.phone

import net.onefivefour.echolist.feature.browser.ui.BreadcrumbItem

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.home_title
import net.onefivefour.echolist.feature.browser.resources.ic_arrow_right
import net.onefivefour.echolist.feature.browser.resources.ic_home
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.flow.first

@Composable
internal fun BreadcrumbBar(
    breadcrumbs: List<BreadcrumbItem>
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(breadcrumbs) {
        if (breadcrumbs.isNotEmpty()) {
            snapshotFlow { scrollState.maxValue }
                .first { it > 0 }

            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = EchoListTheme.materialColors.surfaceVariant,
                shape = RoundedCornerShape(50)
            ),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = EchoListTheme.dimensions.m,
                    vertical = EchoListTheme.dimensions.m
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
        ) {
            Icon(
                modifier = Modifier
                    .offset(x = EchoListTheme.dimensions.xs),
                painter = painterResource(Res.drawable.ic_home),
                contentDescription = stringResource(Res.string.home_title),
                tint = when (breadcrumbs.size) {
                    1 -> EchoListTheme.materialColors.primary
                    else -> EchoListTheme.materialColors.onSurface
                }
            )

            if (breadcrumbs.isEmpty()) {
                return@Row
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
            ) {
                breadcrumbs
                    .dropLast(1)
                    .forEach { bc ->
                        Text(
                            text = bc.label,
                            style = EchoListTheme.typography.labelMedium
                        )
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_right),
                            contentDescription = null
                        )
                    }

                Text(
                    text = breadcrumbs.last().label,
                    style = EchoListTheme.typography.labelMedium.copy(
                        color = EchoListTheme.materialColors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
@Preview
private fun BreadcrumbBarPreview() {
    EchoListTheme {
        BreadcrumbBar(
            breadcrumbs = listOf(
                BreadcrumbItem(
                    label = "Home",
                    parentDir = ""
                ),
                BreadcrumbItem(
                    label = "Folder 1",
                    parentDir = "folder1"
                ),
                BreadcrumbItem(
                    label = "Folder 2",
                    parentDir = "folder1/folder2"
                )
            )
        )
    }
}