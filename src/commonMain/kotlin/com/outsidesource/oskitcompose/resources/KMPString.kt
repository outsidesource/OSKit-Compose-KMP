package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import kotlinx.atomicfu.atomic

/**
 * Kotlin Multiplatform localized strings
 *
 * Runtime replacements are supported with the `replacementPattern` parameter. By default, it is set to '%s'.
 * Replacements are not formatted in any way.
 *
 * Example:
 * ```
 * object Strings: KMPStrings() {
 *     val hello = kmpStringKey()
 *     val myName = kmpStringKey()
 *
 *     override val strings = mapOf(
 *         "en" to mapOf(
 *             hello to "Hello world!",
 *             myName to "My name is %s",
 *         ),
 *         "es" to mapOf(
 *             hello to "Hola Mundo!",
 *             myName to "Mi nombre es %s",
 *         )
 *     )
 * }
 *
 * @Composable
 * fun Test() {
 *     Text(kmpString(Strings.hello))
 *     Text(kmpString(Strings.myName, "Tom"))
 * }
 * ```
 */
abstract class KMPStrings(private val replacementPattern: Regex = Regex("%s")) {
    private val keyId = atomic(0)
    protected abstract val strings: Map<KMPLocale, Map<KMPStringKey, String>>

    protected fun kmpStringKey() = KMPStringKey(keyId.incrementAndGet(), ::stringsInternal, replacementPattern)
    private fun stringsInternal() = strings
}

typealias KMPLocale = String

data class KMPStringKey(
    private val id: Int,
    internal val strings: () -> Map<KMPLocale, Map<KMPStringKey, String>>,
    internal val replacementPattern: Regex,
)

@Composable
fun kmpString(key: KMPStringKey, vararg args: String): String {
    val locale = Locale.current.language
    val string = key.strings()[locale]?.get(key) ?: ""

    return if (args.isNotEmpty()) {
        var index = 0
        return string.replace(key.replacementPattern) { args.getOrNull(index++) ?: "" }
    } else {
        string
    }
}

fun kmpString(key: KMPStringKey, locale: Locale, vararg args: String): String {
    val string = key.strings()[locale.language]?.get(key) ?: ""
    Locale.current.language

    return if (args.isNotEmpty()) {
        var index = 0
        return string.replace(key.replacementPattern) { args.getOrNull(index++) ?: "" }
    } else {
        string
    }
}