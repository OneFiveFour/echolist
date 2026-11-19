package net.onefivefour.echolist.feature.note.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun MarkdownPreview(
    document: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(document) { MarkdownSubsetParser.parse(document) }
    val linkColor = EchoListTheme.materialColors.primary

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> MarkdownText(
                    annotatedString = block.content.toAnnotatedString(linkColor),
                    style = when (block.level) {
                        1 -> EchoListTheme.typography.headlineSmall
                        2 -> EchoListTheme.typography.titleLarge
                        else -> EchoListTheme.typography.titleMedium
                    }
                )

                is MarkdownBlock.Paragraph -> MarkdownText(
                    annotatedString = block.content.toAnnotatedString(linkColor),
                    style = EchoListTheme.typography.bodyLarge
                )

                is MarkdownBlock.BulletList -> Column(
                    verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs)
                ) {
                    block.items.forEach { item ->
                        MarkdownListRow(
                            prefix = "-",
                            content = item.toAnnotatedString(linkColor)
                        )
                    }
                }

                is MarkdownBlock.Checklist -> Column(
                    verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs)
                ) {
                    block.items.forEach { item ->
                        MarkdownListRow(
                            prefix = if (item.isChecked) "[x]" else "[ ]",
                            content = item.content.toAnnotatedString(linkColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownListRow(
    prefix: String,
    content: AnnotatedString
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
    ) {
        Text(
            text = prefix,
            style = EchoListTheme.typography.bodyLarge,
            color = EchoListTheme.materialColors.onSurface
        )
        MarkdownText(
            annotatedString = content,
            style = EchoListTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarkdownText(
    annotatedString: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = annotatedString,
        style = style,
        color = EchoListTheme.materialColors.onSurface,
        modifier = modifier
    )
}

private fun List<MarkdownInline>.toAnnotatedString(linkColor: Color): AnnotatedString = buildAnnotatedString {
    appendInlineContent(this@toAnnotatedString, linkColor)
}

private fun AnnotatedString.Builder.appendInlineContent(
    inlines: List<MarkdownInline>,
    linkColor: Color
) {
    inlines.forEach { inline ->
        when (inline) {
            is MarkdownInline.Text -> append(inline.value)
            is MarkdownInline.Bold -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendInlineContent(inline.content, linkColor)
                pop()
            }

            is MarkdownInline.Link -> {
                withLink(
                    LinkAnnotation.Url(
                        url = inline.url,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append(inline.label)
                }
            }
        }
    }
}