package com.outsidesource.oskitcompose.markdown

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Immutable
data class MarkdownContext internal constructor(
    internal val localPainterCache: Map<String, Painter> = emptyMap(),
    internal val remotePainterCache: MutableMap<String, Painter> = mutableMapOf(),
    internal val inlineImageMap: MutableMap<String, MarkdownBlock.Image> = mutableMapOf(),
    internal val styles: MarkdownStyles = MarkdownStyles(),
    internal val onLinkClick: (it: String, uriHandler: UriHandler) -> Unit = ::defaultOnLickClickHandler,
) {
    constructor(
        localImageMap: Map<String, Painter> = emptyMap(),
        styles: MarkdownStyles = MarkdownStyles(),
        onLinkClick: (it: String, uriHandler: UriHandler) -> Unit = ::defaultOnLickClickHandler,
    ) : this(
        localPainterCache = localImageMap,
        remotePainterCache = mutableMapOf(),
        inlineImageMap = mutableMapOf(),
        styles = styles,
        onLinkClick = onLinkClick,
    )
}

internal fun defaultOnLickClickHandler(it: String, uriHandler: UriHandler) {
    try {
        uriHandler.openUri(it)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Immutable
data class MarkdownStyles(
    // Misc
    val allowCodeBlockHorizontalScrolling: Boolean = true,
    val loaderBackgroundColor: Color = Color(0x30000000),
    val blockGap: Dp = 16.dp,

    // Text Styles
    val blockQuoteTextStyle: TextStyle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 1.4.em,
        letterSpacing = .5.sp,
    ),
    val codeTextStyle: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        lineHeight = 1.4.em,
        letterSpacing = .5.sp,
    ),
    val h1TextStyle: TextStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp),
    val h2TextStyle: TextStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp),
    val h3TextStyle: TextStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp),
    val h4TextStyle: TextStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp),
    val h5TextStyle: TextStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp),
    val h6TextStyle: TextStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp),
    val italicTextStyle: TextStyle = TextStyle(fontStyle = FontStyle.Italic),
    val linkTextStyle: TextStyle = TextStyle(
        color = Color.Blue,
        textDecoration = TextDecoration.None,
        letterSpacing = .5.sp,
    ),
    val paragraphTextStyle: TextStyle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 1.4.em,
        letterSpacing = .5.sp,
    ),
    val strongTextStyle: TextStyle = TextStyle(fontWeight = FontWeight.Bold),

    // Layout/Decoration Styles
    val horizontalRuleComposable: @Composable () -> Unit = { DefaultMarkdownHR() },
    val codeSpanDecoration: DrawScope.(Path) -> Unit = defaultCodeSpanDecoration,
    val blockQuoteModifier: Modifier = defaultMarkdownBlockQuoteModifier,
    val codeModifier: Modifier = defaultMarkdownCodeModifier,
    val headerModifier: (MarkdownHeadingSize) -> Modifier = { Modifier },
    val listModifier: Modifier = defaultMarkdownListModifier,
    val listItemPrefixSpacing: Dp = 12.dp,
    val listItemPrefixComposable: @Composable RowScope.(isOrdered: Boolean, prefixContent: String?) -> Unit =
        { isOrdered, prefixContent -> DefaultMarkdownListItemPrefix(isOrdered, prefixContent) },
    val imageModifier: Modifier = Modifier,
    val paragraphModifier: Modifier = Modifier,
    val setextModifier: (MarkdownSetextSize) -> Modifier = { Modifier },
) {

    fun withDefaultTextStyle(defaultTextStyle: TextStyle): MarkdownStyles = copy(
        paragraphTextStyle = paragraphTextStyle.merge(defaultTextStyle),
        blockQuoteTextStyle = blockQuoteTextStyle.merge(defaultTextStyle),
        codeTextStyle = codeTextStyle.merge(defaultTextStyle),
        linkTextStyle = linkTextStyle.merge(defaultTextStyle),
        h1TextStyle = h1TextStyle.merge(defaultTextStyle),
        h2TextStyle = h2TextStyle.merge(defaultTextStyle),
        h3TextStyle = h3TextStyle.merge(defaultTextStyle),
        h4TextStyle = h4TextStyle.merge(defaultTextStyle),
        h5TextStyle = h5TextStyle.merge(defaultTextStyle),
        h6TextStyle = h6TextStyle.merge(defaultTextStyle),
        italicTextStyle = italicTextStyle.merge(defaultTextStyle),
        strongTextStyle = strongTextStyle.merge(defaultTextStyle),
    )
}