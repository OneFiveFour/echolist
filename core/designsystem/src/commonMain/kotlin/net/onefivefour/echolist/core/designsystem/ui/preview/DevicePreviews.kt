package net.onefivefour.echolist.core.designsystem.ui.preview

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview

/** Phone window in both themes, with dimensions supplied by the preview renderer. */
@Preview(
    name = "Phone Light",
    device = "spec:width=400dp,height=800dp,dpi=160",
    uiMode = AndroidUiModes.UI_MODE_NIGHT_NO or AndroidUiModes.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Phone Dark",
    device = "spec:width=400dp,height=800dp,dpi=160",
    uiMode = AndroidUiModes.UI_MODE_NIGHT_YES or AndroidUiModes.UI_MODE_TYPE_NORMAL
)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class PreviewPhoneLightDark

/** Desktop window in both themes, with dimensions supplied by the preview renderer. */
@Preview(
    name = "Desktop Light",
    device = "spec:width=1440dp,height=800dp,dpi=160",
    uiMode = AndroidUiModes.UI_MODE_NIGHT_NO or AndroidUiModes.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Desktop Dark",
    device = "spec:width=1440dp,height=800dp,dpi=160",
    uiMode = AndroidUiModes.UI_MODE_NIGHT_YES or AndroidUiModes.UI_MODE_TYPE_NORMAL
)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class PreviewDesktopLightDark

/** Bounded text/card width with content-sized height, without a simulated phone screen. */
@Preview(
    name = "Component Light",
    widthDp = 400,
    uiMode = AndroidUiModes.UI_MODE_NIGHT_NO or AndroidUiModes.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Component Dark",
    widthDp = 400,
    uiMode = AndroidUiModes.UI_MODE_NIGHT_YES or AndroidUiModes.UI_MODE_TYPE_NORMAL
)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class PreviewComponentLightDark
