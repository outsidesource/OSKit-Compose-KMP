package com.outsidesource.oskitcompose.markdown

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode

internal const val TAG_URL = "URL"
internal const val TAG_CODE_SPAN = "CODE_SPAN"
internal const val TAG_INLINE_IMAGE = "TAG_INLINE_IMAGE"

@Immutable
data class MarkdownContext(
    internal val localPainterCache: Map<String, Painter> = emptyMap(),
    internal val remotePainterCache: MutableMap<String, Painter> = mutableMapOf(),
    internal val inlineImageMap: MutableMap<String, MarkdownBlock.Image> = mutableMapOf(),
    internal val styles: MarkdownStyles = MarkdownStyles(),
    internal val onLinkClick: (it: String) -> Unit = {},
)

internal fun ASTNode.buildBlockItems(source: String, markdownContext: MarkdownContext, density: Density) =
    children.buildBlockItems(source, markdownContext, density)

private fun List<ASTNode>.buildBlockItems(source: String, markdownContext: MarkdownContext, density: Density): List<MarkdownBlock> {
    val items = mutableListOf<MarkdownBlock>()
    val text = AnnotatedString.Builder()

    forEachIndexed { i, child ->
        try {
            val previousChild = getOrNull(i - 1)

            when (child.type) {
                // Block Level Content
                MarkdownElementTypes.ATX_1 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H1,
                        child.buildHeaderContent(source, markdownContext, density)
                    )
                )

                MarkdownElementTypes.ATX_2 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H2,
                        child.buildHeaderContent(source, markdownContext, density)
                    )
                )

                MarkdownElementTypes.ATX_3 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H3,
                        child.buildHeaderContent(source, markdownContext, density)
                    )
                )

                MarkdownElementTypes.ATX_4 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H4,
                        child.buildHeaderContent(source, markdownContext, density)
                    )
                )

                MarkdownElementTypes.ATX_5 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H5,
                        child.buildHeaderContent(source, markdownContext, density)
                    )
                )

                MarkdownElementTypes.ATX_6 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H6,
                        child.buildHeaderContent(source, markdownContext, density)
                    )
                )

                MarkdownElementTypes.SETEXT_1 -> items.add(
                    MarkdownBlock.Setext(
                        size = MarkdownSetextSize.Setext1,
                        child.buildSetextContent(source, markdownContext, density)
                    )
                )

                MarkdownElementTypes.SETEXT_2 -> items.add(
                    MarkdownBlock.Setext(
                        size = MarkdownSetextSize.Setext2,
                        child.buildSetextContent(source, markdownContext, density)
                    )
                )

                MarkdownElementTypes.CODE_BLOCK -> items.add(MarkdownBlock.Code(child.buildCodeBlockContent(source)))
                MarkdownElementTypes.CODE_FENCE -> items.add(MarkdownBlock.Code(child.buildCodeFenceContent(source)))
                MarkdownElementTypes.BLOCK_QUOTE -> items.add(
                    MarkdownBlock.BlockQuote(
                        child.buildBlockItems(
                            source,
                            markdownContext,
                            density
                        )
                    )
                )

                MarkdownElementTypes.UNORDERED_LIST -> items.add(
                    MarkdownBlock.List(
                        items = child.buildBlockItems(
                            source,
                            markdownContext,
                            density
                        ), isOrdered = false
                    )
                )

                MarkdownElementTypes.ORDERED_LIST -> items.add(
                    MarkdownBlock.List(
                        items = child.buildBlockItems(
                            source,
                            markdownContext,
                            density
                        ), isOrdered = true
                    )
                )

                MarkdownElementTypes.LIST_ITEM -> {
                    val prefixNode = child.children.firstOrNull()
                    val prefix = if (prefixNode?.type == MarkdownTokenTypes.LIST_NUMBER) {
                        prefixNode.getTextInNode(source).toString().removeSuffix(". ")
                    } else {
                        null
                    }
                    items.add(
                        MarkdownBlock.ListItem(
                            prefix = prefix,
                            content = child.buildBlockItems(source, markdownContext, density)
                        )
                    )
                }

                MarkdownElementTypes.PARAGRAPH -> items.addAll(child.buildBlockItems(source, markdownContext, density))
                MarkdownElementTypes.HTML_BLOCK -> {} // Ignore HTML because <br/> cause a lot of extra line breaks and there isn't a great way to render it
                MarkdownTokenTypes.HORIZONTAL_RULE -> items.add(MarkdownBlock.HR)

                // Inline Content
                MarkdownElementTypes.STRONG -> text.append(child.buildBoldContent(source, markdownContext, density))
                MarkdownElementTypes.EMPH -> text.append(child.buildItalicContent(source, markdownContext, density))
                MarkdownElementTypes.CODE_SPAN -> text.append(child.buildCodeSpanContent(source, markdownContext.styles))
                MarkdownElementTypes.INLINE_LINK,
                MarkdownElementTypes.AUTOLINK -> text.append(child.buildLinkContent(source, markdownContext.styles))

                MarkdownElementTypes.IMAGE -> {
                    if (size == 1) { // Handle Block images (paragraphs with only an image)
                        items.add(child.buildImage(source))
                    } else { // Handle inline images
                        val id = (markdownContext.inlineImageMap.size + 1).toString()
                        val imageInfo = child.buildImage(source)
                        text.pushStringAnnotation(TAG_INLINE_IMAGE, id)
                        text.appendInlineContent(id, "inlineImage")
                        text.pop()
                        markdownContext.inlineImageMap[id] = imageInfo
                    }
                }

                // Tokens
                MarkdownTokenTypes.BLOCK_QUOTE -> {}
                MarkdownTokenTypes.LIST_BULLET -> {}
                MarkdownTokenTypes.LIST_NUMBER -> {}
                MarkdownTokenTypes.WHITE_SPACE -> {
                    if (previousChild == null) return@forEachIndexed
                    when (previousChild.type) {
                        MarkdownTokenTypes.EOL,
                        MarkdownTokenTypes.ATX_HEADER,
                        MarkdownTokenTypes.BLOCK_QUOTE -> return@forEachIndexed
                    }
                    text.append(' ')
                }

                else -> text.append(child.getTextInNode(source).toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val annotatedString = text.toAnnotatedString()
    if (annotatedString.text.trim('\n').isNotBlank()) items.add(MarkdownBlock.Paragraph(annotatedString))

    return items
}

private fun ASTNode.buildCodeBlockContent(source: String) = buildAnnotatedString {
    append(getTextInNode(source).toString().trimIndent())
}

private fun ASTNode.buildCodeFenceContent(source: String) = buildAnnotatedString {
    val startChildIndex = children.indexOfFirst {
        it.type !== MarkdownTokenTypes.CODE_FENCE_START &&
                it.type !== MarkdownTokenTypes.FENCE_LANG &&
                it.type !== MarkdownTokenTypes.EOL
    }
    val endChildIndex = children.indexOfLast {
        it.type !== MarkdownTokenTypes.CODE_FENCE_END &&
                it.type !== MarkdownTokenTypes.EOL
    }
    val start = children[startChildIndex].startOffset
    val end = children[endChildIndex].endOffset
    append(source.subSequence(start, end).toString())
}

private fun ASTNode.buildHeaderContent(source: String, markdownContext: MarkdownContext, density: Density) = buildAnnotatedString {
    val text = findChildOfType(MarkdownTokenTypes.ATX_CONTENT) ?: return@buildAnnotatedString
    val items = text.buildBlockItems(source, markdownContext, density)

    items.forEach {
        if (it is MarkdownBlock.Paragraph) {
            append(it.content)
        }
    }
}

private fun ASTNode.buildSetextContent(source: String, markdownContext: MarkdownContext, density: Density) = buildAnnotatedString {
    val text = findChildOfType(MarkdownTokenTypes.SETEXT_CONTENT) ?: return@buildAnnotatedString
    val items = text.buildBlockItems(source, markdownContext, density)

    items.forEach {
        if (it is MarkdownBlock.Paragraph) {
            append(it.content)
        }
    }
}

private fun ASTNode.buildBoldContent(source: String, markdownContext: MarkdownContext, density: Density): AnnotatedString {
    return buildAnnotatedString {
        val content = children
            .filter { it.type != MarkdownTokenTypes.EMPH }
            .buildBlockItems(source, markdownContext, density)
            .firstOrNull() ?: return@buildAnnotatedString

        if (content !is MarkdownBlock.Paragraph) return@buildAnnotatedString

        withStyle(markdownContext.styles.strongTextStyle.toSpanStyle()) {
            append(content.content)
        }
    }
}

private fun ASTNode.buildItalicContent(source: String, markdownContext: MarkdownContext, density: Density): AnnotatedString {
    return buildAnnotatedString {
        val content = children
            .filter { it.type != MarkdownTokenTypes.EMPH }
            .buildBlockItems(source, markdownContext, density)
            .firstOrNull() ?: return@buildAnnotatedString

        if (content !is MarkdownBlock.Paragraph) return@buildAnnotatedString

        withStyle(markdownContext.styles.italicTextStyle.toSpanStyle()) {
            append(content.content)
        }
    }
}

private fun ASTNode.buildCodeSpanContent(source: String, styles: MarkdownStyles): AnnotatedString {
    return buildAnnotatedString {
        val startChildIndex = children.indexOfFirst { it.type != MarkdownTokenTypes.BACKTICK }
        val endChildIndex = children.indexOfLast { it.type != MarkdownTokenTypes.BACKTICK }
        val start = children[startChildIndex].startOffset
        val end = children[endChildIndex].endOffset

        withStyle(styles.codeTextStyle.toSpanStyle()) {
            pushStringAnnotation(TAG_CODE_SPAN, "")
            append(' ')
            append(source.subSequence(start, end).toString())
            append(' ')
            pop()
        }
    }
}

private fun ASTNode.buildLinkContent(source: String, styles: MarkdownStyles): AnnotatedString {
    return buildAnnotatedString {
        val (text, destination) = if (this@buildLinkContent.type == MarkdownElementTypes.AUTOLINK) {
            val text = findChildOfType(MarkdownTokenTypes.AUTOLINK)?.getTextInNode(source) ?: return@buildAnnotatedString
            Pair(text, text)
        } else {
            val text = findChildOfType(MarkdownElementTypes.LINK_TEXT)?.getTextInNode(source)?.let { it.substring(1, it.length - 1) } ?: return@buildAnnotatedString
            val destination = findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(source) ?: return@buildAnnotatedString
            Pair(text, destination)
        }

        withStyle(styles.linkTextStyle.toSpanStyle()) {
            pushStringAnnotation(TAG_URL, destination.toString())
            append(text.toString())
            pop()
        }
    }
}

private fun ASTNode.buildImage(source: String): MarkdownBlock.Image {
    val link = findChildOfType(MarkdownElementTypes.INLINE_LINK) ?: return MarkdownBlock.Image()
    val description = link.findChildOfType(MarkdownElementTypes.LINK_TEXT)?.getTextInNode(source)?.let { it.substring(1, it.length - 1) } ?: ""
    val destination = link.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(source)?.toString() ?: ""

    var parsedDescription = description
    var width = Dp.Unspecified
    var height = Dp.Unspecified
    var hAlign = Alignment.Start
    var vAlign = Alignment.CenterVertically
    var scale = ContentScale.Fit

    if (description.startsWith("attrs(")) {
        val attrsStart = 6
        val attrsEnd = description.indexOf(')', attrsStart)
        val attributes = description.substring(attrsStart..< attrsEnd)
        parsedDescription = description.substring(attrsEnd + 1 ..< description.length)

        attributes.split(",").forEach { attribute ->
            val pair = attribute.split("=")
            if (pair.size != 2) return@forEach

            when (pair[0].trim()) {
                "width" -> width = pair[1].toFloatOrNull()?.dp ?: Dp.Unspecified
                "height" -> height = pair[1].toFloatOrNull()?.dp ?: Dp.Unspecified
                "hAlign" -> hAlign = when (pair[1]) {
                    "start" -> Alignment.Start
                    "center" -> Alignment.CenterHorizontally
                    "end" -> Alignment.End
                    else -> Alignment.Start
                }
                "vAlign" -> vAlign = when (pair[1]) {
                    "top" -> Alignment.Top
                    "center" -> Alignment.CenterVertically
                    "bottom" -> Alignment.Bottom
                    else -> Alignment.CenterVertically
                }
                "scale" -> scale = when (pair[1]) {
                    "fit" -> ContentScale.Fit
                    "inside" -> ContentScale.Inside
                    "none" -> ContentScale.None
                    "fillBounds" -> ContentScale.FillBounds
                    "fillHeight" -> ContentScale.FillHeight
                    "fillWidth" -> ContentScale.FillWidth
                    "crop" -> ContentScale.Crop
                    else -> ContentScale.Fit
                }
            }
        }
    }

    return MarkdownBlock.Image(
        type = if (destination.startsWith("local:")) {
            MarkdownImageType.Local(destination.split(":").getOrElse(1) { "" })
        } else {
            MarkdownImageType.Remote(destination)
        },
        description = parsedDescription,
        width = width,
        height = height,
        hAlignment = hAlign,
        vAlignment = vAlign,
        scale = scale,
    )
}

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