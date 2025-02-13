package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import kotlinx.atomicfu.atomic

/**
 * Kotlin Multiplatform localized strings
 *
 * Runtime replacements are supported with the `replacementPattern` parameter. By default, it is set to '%s'.
 * Replacements are not formatted in any way.
 *
 * If a key is not found in the current locale, the first provided locale will be used as the fallback unless
 * [useFallbackLocale] is set to false. An empty string will be returned if no string matches the key.
 *
 * Note: The string keys need to be declared before the locales due to the way [KmpStrings] is set up. If you would
 * like to declare the locales you may declare your locales using the [lazy] builder:
 * ```
 * override val locales by lazy {
 *     mapOf(
 *          "en" to StringsEnglish,
 *          "es" to StringsSpanish,*
 *     )
 * }
 * ```
 *
 * Example Usage:
 * ```
 * object Strings: KmpStrings() {
 *     val hello = kmpStringKey()
 *     val myName = kmpStringKey()
 *
 *     override val locales = mapOf(
 *          "en" to StringsEnglish,
 *          "es" to StringsSpanish,
 *     )
 * }
 *
 * private object StringsEnglish: KmpStringSet() {
 *      override val strings: Map<KmpStringKey, String> = mapOf(
 *          Strings.hello to "Hello world!",
 *          Strings.myName to "My name is %s",
 *      }
 * }
 *
 * private object StringsSpanish: KmpStringSet() {
 *      override val strings: Map<KmpStringKey, String> = mapOf(
 *          Strings.hello to "Hola Mundo!",
 *          Strings.myName to "Mi nombre es %s",
 *      }
 * }
 *
 * @Composable
 * fun Test() {
 *     Text(rememberKmpString(Strings.hello))
 *     Text(rememberKmpString(Strings.myName, "Tom"))
 * }
 * ```
 */
abstract class KmpStrings(
    private val replacementPattern: Regex = Regex("%s"),
    private val useFallbackLocale: Boolean = true,
) {
    private val keyId = atomic(0)
    protected abstract val locales: Map<String, KmpStringSet>

    protected fun kmpStringKey() = KmpStringKey(
        keyId.incrementAndGet(),
        ::localesInternal,
        replacementPattern,
        useFallbackLocale,
    )
    private fun localesInternal() = locales
}

abstract class KmpStringSet {
    abstract val strings: Map<KmpStringKey, String>
}

data class KmpStringKey(
    private val id: Int,
    internal val locales: () -> Map<String, KmpStringSet>,
    internal val replacementPattern: Regex,
    internal val useDefaultLocale: Boolean,
)

@Composable
fun kmpString(key: KmpStringKey, vararg args: String): String {
    val locale = LocalLocaleOverride.current?.language ?: Locale.current.language
    return getAndReplacePlaceholders(key, locale, args)
}

fun kmpString(key: KmpStringKey, locale: Locale, vararg args: String): String {
    return getAndReplacePlaceholders(key, locale.language, args)
}

@Composable
fun rememberKmpString(key: KmpStringKey, vararg args: String): String {
    val locale = LocalLocaleOverride.current?.language ?: Locale.current.language
    return remember(locale, key, *args) { getAndReplacePlaceholders(key, locale, args) }
}

private fun getAndReplacePlaceholders(key: KmpStringKey, locale: String, args: Array<out String>): String {
    val locales = key.locales()
    val string = locales[locale]?.strings?.get(key)
        ?: (if (key.useDefaultLocale) locales.values.firstOrNull()?.strings?.get(key) else null)
        ?: ""

    return if (args.isNotEmpty()) {
        var index = 0
        string.replace(key.replacementPattern) { args.getOrNull(index++) ?: "" }
    } else {
        string
    }
}

val LocalLocaleOverride = staticCompositionLocalOf<Locale?> { null }