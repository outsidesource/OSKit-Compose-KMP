package com.outsidesource.oskitcompose.markdown

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp

@Immutable
sealed class MarkdownBlock {
    data class Paragraph(val content: AnnotatedString): MarkdownBlock()
    data class Code(val content: AnnotatedString): MarkdownBlock()
    data class BlockQuote(val content: kotlin.collections.List<MarkdownBlock>): MarkdownBlock()
    data class List(val items: kotlin.collections.List<MarkdownBlock>, val isOrdered: Boolean): MarkdownBlock()
    data class ListItem(val prefix: String? = null, val content: kotlin.collections.List<MarkdownBlock>): MarkdownBlock()
    data class Heading(val size: MarkdownHeadingSize, val content: AnnotatedString): MarkdownBlock()
    data class Setext(val size: MarkdownSetextSize, val content: AnnotatedString): MarkdownBlock()
    data object HR: MarkdownBlock()
    data class Image(
        val type: MarkdownImageType = MarkdownImageType.Local(""),
        val description: String = "",
        val width: Dp = Dp.Unspecified,
        val height: Dp = Dp.Unspecified,
        val hAlignment: Alignment.Horizontal = Alignment.Start,
        val vAlignment: Alignment.Vertical = Alignment.CenterVertically,
        val scale: ContentScale = ContentScale.Fit,
    ): MarkdownBlock()
}

@Immutable
sealed class MarkdownImageType {
    data class Remote(val url: String): MarkdownImageType()
    data class Local(val key: String): MarkdownImageType()
}

enum class MarkdownHeadingSize {
    H1, H2, H3, H4, H5, H6
}

enum class MarkdownSetextSize {
    Setext1, Setext2
}