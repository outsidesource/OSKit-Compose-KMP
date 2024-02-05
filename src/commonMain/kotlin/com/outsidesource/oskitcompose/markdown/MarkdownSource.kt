package com.outsidesource.oskitcompose.markdown

import androidx.compose.runtime.Immutable
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

@Immutable
sealed class MarkdownSource {
    data class Blocks(val items: List<MarkdownBlock>): MarkdownSource() {
        companion object {
            fun fromString(content: kotlin.String, context: MarkdownContext): Blocks {
                val items = MarkdownParser(CommonMarkFlavourDescriptor())
                    .buildMarkdownTreeFromString(content)
                    .buildBlockItems(content, context)
                return Blocks(items)
            }
        }
    }
    data class String(val content: kotlin.String): MarkdownSource()
    data class Source(val source: okio.Source): MarkdownSource()
}