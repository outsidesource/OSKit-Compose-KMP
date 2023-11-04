package com.outsidesource.oskitcompose.markdown

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.canvas.kmpUrlImagePainter
import com.outsidesource.oskitcompose.modifier.borderStart
import com.outsidesource.oskitcompose.scrollbars.KMPHorizontalScrollbar
import com.outsidesource.oskitcompose.scrollbars.KMPScrollbarStyle
import com.outsidesource.oskitcompose.scrollbars.rememberKmpScrollbarAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import kotlin.math.max

private const val TAG_URL = "URL"
private const val TAG_CODE_SPAN = "CODE_SPAN"
private const val TAG_INLINE_IMAGE = "TAG_INLINE_IMAGE"

@Immutable
private data class MarkdownInfo(
    val localImageMap: Map<String, Painter> = emptyMap(),
    val styles: MarkdownStyles = MarkdownStyles(),
    val inlineImageMap: MutableMap<String, MarkdownBlock.Image> = mutableMapOf(),
    val onLinkClick: (it: String) -> Unit = {},
)

private val LocalMarkdownInfo = staticCompositionLocalOf { MarkdownInfo() }

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

/**
 * Markdown
 *
 * A Simple markdown renderer for basic rich text.
 * This composable does not support HTML or links navigating to internal references.
 *
 * Images in Markdown can either be URLs or local resources. Local resources are designated by a user-provided id string
 * and a Painter passed via [localImageMap].
 *
 * Images also have the option of adding sizing, alignment, and scaling info:
 *   width (in dp)
 *   height (in dp)
 *   hAlign (start|center|end) (only affects block images)
 *   vAlign (top|center|bottom) (only affects inline images)
 *   scale (none|crop|fillBounds|fit|fillWidth|fillHeight|inside)
 *
 *   example: ![attrs(width=20, height=20, hAlign=start, vAlign=center) image description](local:my-image-id)
 *
 * Note: Android and iOS do not support svg images
 *
 * [loadAsync] If true Markdown will parse and load URL images on the IO thread. If there aren't any URL images it is
 * recommended that [loadAsync] is false. If you have URL images, it is recommended that [loadAsync] is true.
 *
 * TODO: Wrap with SelectionContainer when SelectionContainer does not block clicking of links
 */
@Composable
fun Markdown(
    text: String,
    modifier: Modifier = Modifier,
    styles: MarkdownStyles = MarkdownStyles(),
    localImageMap: Map<String, Painter> = emptyMap(),
    loadAsync: Boolean = false,
    onLoaded: () -> Unit = {},
    onLinkClick: ((it: String) -> Unit)? = null,
) = InternalMarkdown(
    text = text,
    modifier = modifier,
    styles = styles,
    localImageMap = localImageMap,
    loadAsync = loadAsync,
    onLoaded = onLoaded,
    onLinkClick = onLinkClick,
    isLazy = false,
)

@Composable
fun LazyMarkdown(
    text: String,
    modifier: Modifier = Modifier,
    styles: MarkdownStyles = MarkdownStyles(),
    localImageMap: Map<String, Painter> = emptyMap(),
    lazyListState: LazyListState = rememberLazyListState(),
    loadAsync: Boolean = false,
    onLoaded: () -> Unit = {},
    onLinkClick: ((String) -> Unit)? = null,
) = InternalMarkdown(
    text = text,
    modifier = modifier,
    styles = styles,
    localImageMap = localImageMap,
    loadAsync = loadAsync,
    onLoaded = onLoaded,
    onLinkClick = onLinkClick,
    lazyListState = lazyListState,
    isLazy = true,
)

@Composable
private fun InternalMarkdown(
    text: String,
    modifier: Modifier = Modifier,
    styles: MarkdownStyles = MarkdownStyles(),
    localImageMap: Map<String, Painter> = emptyMap(),
    lazyListState: LazyListState? = null,
    loadAsync: Boolean = false,
    onLoaded: () -> Unit = {},
    onLinkClick: ((String) -> Unit)? = null,
    isLazy: Boolean = false,
) {
    val uriHandler = LocalUriHandler.current
    val localOnLinkClick = onLinkClick ?: {
        try {
            uriHandler.openUri(it)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val localMarkdownInfo = remember(styles, localImageMap, onLinkClick) { MarkdownInfo(localImageMap, styles, onLinkClick = localOnLinkClick) }
    val density = LocalDensity.current

    val tree by if (loadAsync) {
        produceState(initialValue = emptyList(), text, localMarkdownInfo, density, loadAsync) {
            value = withContext(Dispatchers.IO) {
                MarkdownParser(CommonMarkFlavourDescriptor())
                    .buildMarkdownTreeFromString(text)
                    .buildBlockItems(text, localMarkdownInfo, density)
            }
            onLoaded()
        }
    } else {
        remember(text, localMarkdownInfo, density) {
            mutableStateOf(
                MarkdownParser(CommonMarkFlavourDescriptor())
                    .buildMarkdownTreeFromString(text)
                    .buildBlockItems(text, localMarkdownInfo, density)
            )
        }
    }

    CompositionLocalProvider(LocalMarkdownInfo provides localMarkdownInfo) {
        Column(
            modifier = modifier,
            verticalArrangement = if (tree.isEmpty()) {
                Arrangement.Center
            } else {
                Arrangement.spacedBy(localMarkdownInfo.styles.blockGap)
            }
        ) {
            if (tree.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = styles.loaderBackgroundColor)
                }
            }

            if (!isLazy) {
                tree.forEach { MarkdownBlock(it) }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(localMarkdownInfo.styles.blockGap),
                    state = lazyListState ?: rememberLazyListState(),
                ) {
                    items(tree) {
                        MarkdownBlock(it)
                    }
                }
            }
        }
    }
}

val defaultMarkdownBlockQuoteModifier: Modifier = Modifier
    .borderStart(color = Color(0x20000000), width = 4.dp)
    .padding(8.dp)

val defaultMarkdownCodeModifier: Modifier = Modifier
    .clip(RoundedCornerShape(4.dp))
    .background(Color(0x10000000))
    .border(width = Dp.Hairline, color = Color(0x10000000), shape = RoundedCornerShape(4.dp))
    .padding(8.dp)

val defaultMarkdownListModifier: Modifier = Modifier.padding(start = 8.dp)

val defaultCodeSpanDecoration: DrawScope.(Path) -> Unit = { path ->
    val fillPaint = Paint().apply {
        style = PaintingStyle.Fill
        pathEffect = PathEffect.cornerPathEffect(4.dp.toPx())
        color = Color(0x10000000)
    }

    val strokePaint = Paint().apply {
        style = PaintingStyle.Stroke
        strokeWidth = Dp.Hairline.toPx()
        pathEffect = PathEffect.cornerPathEffect(4.dp.toPx())
        color = Color(0x10000000)
        isAntiAlias = false
    }

    drawIntoCanvas { canvas ->
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, strokePaint)
    }
}

@Composable
fun DefaultMarkdownHR() {
    Divider(
        modifier = Modifier.fillMaxWidth().clip(CircleShape),
        thickness = 2.dp,
        color = Color(0x20000000),
    )
}

@Composable
fun DefaultMarkdownListItemPrefix(isOrdered: Boolean, prefixContent: String?) {
    if (isOrdered) {
        Text(
            text = "${prefixContent}.",
            textAlign = TextAlign.End,
            style = LocalMarkdownInfo.current.styles.paragraphTextStyle,
        )
    } else {
        val color = LocalMarkdownInfo.current.styles.paragraphTextStyle.color

        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(if (color != Color.Unspecified) color else Color.Black)
        )
    }
}

@Composable
private fun MarkdownBlock(block: MarkdownBlock) {
    when (block) {
        is MarkdownBlock.Heading -> MarkdownHeading(block)
        is MarkdownBlock.Paragraph -> MarkdownParagraph(block)
        is MarkdownBlock.Code -> MarkdownCodeBlock(block)
        is MarkdownBlock.List -> MarkdownList(block)
        is MarkdownBlock.ListItem -> MarkdownListItem(block)
        is MarkdownBlock.BlockQuote -> MarkdownBlockQuote(block)
        is MarkdownBlock.Setext -> MarkdownSetext(block)
        is MarkdownBlock.Image -> MarkdownImage(block)
        is MarkdownBlock.HR -> LocalMarkdownInfo.current.styles.horizontalRuleComposable()
    }
}

@Composable
private fun MarkdownHeading(header: MarkdownBlock.Heading) {
    val styles = LocalMarkdownInfo.current.styles

    MarkdownInlineContent(
        modifier = styles.headerModifier(header.size),
        content = header.content,
        textStyle = when (header.size) {
            MarkdownHeadingSize.H1 -> styles.h1TextStyle
            MarkdownHeadingSize.H2 -> styles.h2TextStyle
            MarkdownHeadingSize.H3 -> styles.h3TextStyle
            MarkdownHeadingSize.H4 -> styles.h4TextStyle
            MarkdownHeadingSize.H5 -> styles.h5TextStyle
            MarkdownHeadingSize.H6 -> styles.h6TextStyle
        },
    )
}

@Composable
private fun MarkdownParagraph(paragraph: MarkdownBlock.Paragraph) {
    MarkdownInlineContent(
        modifier = LocalMarkdownInfo.current.styles.paragraphModifier,
        content = paragraph.content
    )
}

@Composable
private fun MarkdownBlockQuote(blockQuote: MarkdownBlock.BlockQuote) {
    val styles = LocalMarkdownInfo.current.styles

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(styles.blockQuoteModifier),
        verticalArrangement = Arrangement.spacedBy(styles.blockGap)
    ) {
        for (block in blockQuote.content) {
            MarkdownBlock(block)
        }
    }
}

@Composable
private fun MarkdownInlineContent(
    content: AnnotatedString,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalMarkdownInfo.current.styles.paragraphTextStyle,
) {
    val styles = LocalMarkdownInfo.current.styles
    val onLinkClick = LocalMarkdownInfo.current.onLinkClick
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    var codeSpans by remember(content) { mutableStateOf(emptyList<Path>()) }
    val inlineImageMap = LocalMarkdownInfo.current.inlineImageMap
    val density = LocalDensity.current
    var maxImageHeight = 0f

    val inlineContent = remember(content) {
        buildMap {
            content.getStringAnnotations(TAG_INLINE_IMAGE, 0, content.length).forEach {
                val id = it.item
                val image = inlineImageMap[id] ?: return@forEach
                val size = resolveDimensionsForImage(density, image)
                val (width, height) = with(density) { Pair(size.width.toSp(), size.height.toSp()) }
                maxImageHeight = max(height.value, maxImageHeight)

                val alignment = when (image.vAlignment) {
                    Alignment.Top -> PlaceholderVerticalAlign.Top
                    Alignment.CenterVertically -> PlaceholderVerticalAlign.Center
                    Alignment.Bottom -> PlaceholderVerticalAlign.Bottom
                    else -> PlaceholderVerticalAlign.Center
                }

                put(id,
                    InlineTextContent(
                        placeholder = Placeholder(
                            width = width,
                            height = height,
                            placeholderVerticalAlign = alignment
                        ),
                        children = {
                            MarkdownImage(image)
                        }
                    ),
                )
            }
        }
    }

    Text(
        modifier = Modifier
            .pointerInput(content) {
                detectTapGestures { pos ->
                    layoutResult.value?.let { layoutResult ->
                        val offset = layoutResult.getOffsetForPosition(pos)
                        content.getStringAnnotations(TAG_URL, offset, offset).forEach { annotation -> onLinkClick(annotation.item) }
                    }
                }
            }
            .drawBehind {
                codeSpans.forEach {
                    styles.codeSpanDecoration(this, it)
                }
            }.then(modifier),
        style = textStyle.copy(
            lineHeight = if (maxImageHeight == 0f) {
                textStyle.lineHeight
            } else {
                maxImageHeight.sp
            }
        ),
        text = content,
        onTextLayout = { lr ->
            layoutResult.value = lr
            codeSpans = content.getStringAnnotations(TAG_CODE_SPAN, 0, content.length).map { annotation ->
                val first = lr.getBoundingBox(annotation.start)
                val last = lr.getBoundingBox(annotation.end - 1)

                if (first.bottomLeft.y == last.bottomLeft.y) {
                    Path().apply {
                        moveTo(first.left + .5f, first.top + .5f)
                        lineTo(last.right + .5f, last.top + .5f)
                        lineTo(last.right + .5f, last.bottom + .5f)
                        lineTo(first.left + .5f, first.bottom + .5f)
                        close()
                    }
                } else {
                    lr.getPathForRange(annotation.start, annotation.end)
                }
            }
        },
        inlineContent = inlineContent
    )
}

@Composable
private fun MarkdownImage(image: MarkdownBlock.Image) {
    val styles = LocalMarkdownInfo.current.styles
    val density = LocalDensity.current
    val painter = image.painter ?: return Text(image.description)
    val alignment = image.hAlignment

    Column(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.align(alignment)) {
            val size = resolveDimensionsForImage(density, image)

            Image(
                modifier = Modifier
                    .size(width = size.width, height = size.height)
                    .then(styles.imageModifier),
                painter = painter,
                contentScale = image.scale,
                contentDescription = image.description,
            )
        }
    }
}

private fun resolveDimensionsForImage(
    density: Density,
    image: MarkdownBlock.Image,
): DpSize {
    if (image.painter == null) return DpSize.Zero

    val ratio = with(density) { image.painter.intrinsicSize.width.toDp() / image.painter.intrinsicSize.height.toDp() }
    val intrinsicSize = with(density) { image.painter.intrinsicSize.toDpSize() }

    val width = when {
        image.width != Dp.Unspecified -> image.width
        image.height != Dp.Unspecified -> image.height * ratio
        else -> intrinsicSize.width
    }

    val height = when {
        image.height != Dp.Unspecified -> image.height
        image.width != Dp.Unspecified -> image.width / ratio
        else -> intrinsicSize.height
    }

    return DpSize(width, height)
}

@Composable
private fun MarkdownCodeBlock(codeBlock: MarkdownBlock.Code) {
    val styles = LocalMarkdownInfo.current.styles
    val allowHScroll = LocalMarkdownInfo.current.styles.allowCodeBlockHorizontalScrolling
    val scrollState = rememberScrollState()
    val adapter = rememberKmpScrollbarAdapter(scrollState)

    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .then(styles.codeModifier)
                .then(if (allowHScroll) Modifier.horizontalScroll(state = scrollState) else Modifier),
            text = codeBlock.content,
            style = styles.codeTextStyle
        )
        if (allowHScroll) {
            KMPHorizontalScrollbar(
                modifier = Modifier
                    .padding(2.dp)
                    .align(Alignment.BottomStart),
                adapter = adapter,
                style = KMPScrollbarStyle(thickness = 4.dp),
            )
        }
    }
}

@Composable
private fun MarkdownList(list: MarkdownBlock.List) {
    val styles = LocalMarkdownInfo.current.styles

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(styles.listModifier),
    ) {
        var resolvedPrefix = 0

        list.items.forEachIndexed { i, item ->
            if (item !is MarkdownBlock.ListItem) return@forEachIndexed

            resolvedPrefix = when {
                i == 0 -> item.prefix?.toIntOrNull() ?: 1
                else -> resolvedPrefix + 1
            }

            Row(horizontalArrangement = Arrangement.spacedBy(styles.listItemPrefixSpacing)) {
                styles.listItemPrefixComposable(this, list.isOrdered, "$resolvedPrefix")

                Column {
                    item.content.forEach {
                        MarkdownBlock(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownListItem(block: MarkdownBlock.ListItem) {
    val styles = LocalMarkdownInfo.current.styles

    Column(
        verticalArrangement = Arrangement.spacedBy(styles.blockGap)
    ) {
        block.content.forEach { MarkdownBlock(it) }
    }
}

@Composable
private fun MarkdownSetext(setext: MarkdownBlock.Setext) {
    val styles = LocalMarkdownInfo.current.styles

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            modifier = styles.setextModifier(setext.size),
            text = setext.content,
            style = when (setext.size) {
                MarkdownSetextSize.Setext1 -> styles.h1TextStyle
                MarkdownSetextSize.Setext2 -> styles.h2TextStyle
            },
        )
        styles.horizontalRuleComposable()
    }
}

/**
 * ASTNode to MarkdownBlock converters
 * ----------------------------------------------------------------------------
 */
private fun ASTNode.buildBlockItems(source: String, markdownInfo: MarkdownInfo, density: Density) =
    children.buildBlockItems(source, markdownInfo, density)

private fun List<ASTNode>.buildBlockItems(source: String, markdownInfo: MarkdownInfo, density: Density): List<MarkdownBlock> {
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
                        child.buildHeaderContent(source, markdownInfo, density)
                    )
                )

                MarkdownElementTypes.ATX_2 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H2,
                        child.buildHeaderContent(source, markdownInfo, density)
                    )
                )

                MarkdownElementTypes.ATX_3 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H3,
                        child.buildHeaderContent(source, markdownInfo, density)
                    )
                )

                MarkdownElementTypes.ATX_4 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H4,
                        child.buildHeaderContent(source, markdownInfo, density)
                    )
                )

                MarkdownElementTypes.ATX_5 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H5,
                        child.buildHeaderContent(source, markdownInfo, density)
                    )
                )

                MarkdownElementTypes.ATX_6 -> items.add(
                    MarkdownBlock.Heading(
                        size = MarkdownHeadingSize.H6,
                        child.buildHeaderContent(source, markdownInfo, density)
                    )
                )

                MarkdownElementTypes.SETEXT_1 -> items.add(
                    MarkdownBlock.Setext(
                        size = MarkdownSetextSize.Setext1,
                        child.buildSetextContent(source, markdownInfo, density)
                    )
                )

                MarkdownElementTypes.SETEXT_2 -> items.add(
                    MarkdownBlock.Setext(
                        size = MarkdownSetextSize.Setext2,
                        child.buildSetextContent(source, markdownInfo, density)
                    )
                )

                MarkdownElementTypes.CODE_BLOCK -> items.add(MarkdownBlock.Code(child.buildCodeBlockContent(source)))
                MarkdownElementTypes.CODE_FENCE -> items.add(MarkdownBlock.Code(child.buildCodeFenceContent(source)))
                MarkdownElementTypes.BLOCK_QUOTE -> items.add(
                    MarkdownBlock.BlockQuote(
                        child.buildBlockItems(
                            source,
                            markdownInfo,
                            density
                        )
                    )
                )

                MarkdownElementTypes.UNORDERED_LIST -> items.add(
                    MarkdownBlock.List(
                        items = child.buildBlockItems(
                            source,
                            markdownInfo,
                            density
                        ), isOrdered = false
                    )
                )

                MarkdownElementTypes.ORDERED_LIST -> items.add(
                    MarkdownBlock.List(
                        items = child.buildBlockItems(
                            source,
                            markdownInfo,
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
                            content = child.buildBlockItems(source, markdownInfo, density)
                        )
                    )
                }

                MarkdownElementTypes.PARAGRAPH -> items.addAll(child.buildBlockItems(source, markdownInfo, density))
                MarkdownElementTypes.HTML_BLOCK -> {} // Ignore HTML because <br/> cause a lot of extra line breaks and there isn't a great way to render it
                MarkdownTokenTypes.HORIZONTAL_RULE -> items.add(MarkdownBlock.HR)

                // Inline Content
                MarkdownElementTypes.STRONG -> text.append(child.buildBoldContent(source, markdownInfo, density))
                MarkdownElementTypes.EMPH -> text.append(child.buildItalicContent(source, markdownInfo, density))
                MarkdownElementTypes.CODE_SPAN -> text.append(child.buildCodeSpanContent(source, markdownInfo.styles))
                MarkdownElementTypes.INLINE_LINK,
                MarkdownElementTypes.AUTOLINK -> text.append(child.buildLinkContent(source, markdownInfo.styles))

                MarkdownElementTypes.IMAGE -> {
                    if (size == 1) { // Handle Block images (paragraphs with only an image)
                        items.add(child.buildImage(source, markdownInfo, density))
                    } else { // Handle inline images
                        val id = (markdownInfo.inlineImageMap.size + 1).toString()
                        val imageInfo = child.buildImage(source, markdownInfo, density)
                        text.pushStringAnnotation(TAG_INLINE_IMAGE, id)
                        text.appendInlineContent(id, "inlineImage")
                        text.pop()
                        markdownInfo.inlineImageMap[id] = imageInfo
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

private fun ASTNode.buildHeaderContent(source: String, markdownInfo: MarkdownInfo, density: Density) = buildAnnotatedString {
    val text = findChildOfType(MarkdownTokenTypes.ATX_CONTENT) ?: return@buildAnnotatedString
    val items = text.buildBlockItems(source, markdownInfo, density)

    items.forEach {
        if (it is MarkdownBlock.Paragraph) {
            append(it.content)
        }
    }
}

private fun ASTNode.buildSetextContent(source: String, markdownInfo: MarkdownInfo, density: Density) = buildAnnotatedString {
    val text = findChildOfType(MarkdownTokenTypes.SETEXT_CONTENT) ?: return@buildAnnotatedString
    val items = text.buildBlockItems(source, markdownInfo, density)

    items.forEach {
        if (it is MarkdownBlock.Paragraph) {
            append(it.content)
        }
    }
}

private fun ASTNode.buildBoldContent(source: String, markdownInfo: MarkdownInfo, density: Density): AnnotatedString {
    return buildAnnotatedString {
        val content = children
            .filter { it.type != MarkdownTokenTypes.EMPH }
            .buildBlockItems(source, markdownInfo, density)
            .firstOrNull() ?: return@buildAnnotatedString

        if (content !is MarkdownBlock.Paragraph) return@buildAnnotatedString

        withStyle(markdownInfo.styles.strongTextStyle.toSpanStyle()) {
            append(content.content)
        }
    }
}

private fun ASTNode.buildItalicContent(source: String, markdownInfo: MarkdownInfo, density: Density): AnnotatedString {
    return buildAnnotatedString {
        val content = children
            .filter { it.type != MarkdownTokenTypes.EMPH }
            .buildBlockItems(source, markdownInfo, density)
            .firstOrNull() ?: return@buildAnnotatedString

        if (content !is MarkdownBlock.Paragraph) return@buildAnnotatedString

        withStyle(markdownInfo.styles.italicTextStyle.toSpanStyle()) {
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

private fun ASTNode.buildImage(source: String, markdownInfo: MarkdownInfo, density: Density): MarkdownBlock.Image {
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

    val painter = if (destination.startsWith("local:")) {
        markdownInfo.localImageMap[destination.split(":").getOrElse(1) { "" }]
    } else {
        kmpUrlImagePainter(destination, density)
    }

    return MarkdownBlock.Image(
        painter = painter,
        description = parsedDescription,
        width = width,
        height = height,
        hAlignment = hAlign,
        vAlignment = vAlign,
        scale = scale,
    )
}

@Immutable
private sealed class MarkdownBlock {
    data class Paragraph(val content: AnnotatedString): MarkdownBlock()
    data class Code(val content: AnnotatedString): MarkdownBlock()
    data class BlockQuote(val content: kotlin.collections.List<MarkdownBlock>): MarkdownBlock()
    data class List(val items: kotlin.collections.List<MarkdownBlock>, val isOrdered: Boolean): MarkdownBlock()
    data class ListItem(val prefix: String? = null, val content: kotlin.collections.List<MarkdownBlock>): MarkdownBlock()
    data class Heading(val size: MarkdownHeadingSize, val content: AnnotatedString): MarkdownBlock()
    data class Setext(val size: MarkdownSetextSize, val content: AnnotatedString): MarkdownBlock()
    data object HR: MarkdownBlock()
    data class Image(
        val painter: Painter? = null,
        val description: String = "",
        val width: Dp = Dp.Unspecified,
        val height: Dp = Dp.Unspecified,
        val hAlignment: Alignment.Horizontal = Alignment.Start,
        val vAlignment: Alignment.Vertical = Alignment.CenterVertically,
        val scale: ContentScale = ContentScale.Fit,
    ): MarkdownBlock()
}

enum class MarkdownHeadingSize {
    H1, H2, H3, H4, H5, H6
}

enum class MarkdownSetextSize {
    Setext1, Setext2
}