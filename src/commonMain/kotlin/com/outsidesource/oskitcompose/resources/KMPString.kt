package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.intl.Locale
import com.outsidesource.oskitcompose.lib.rememberInject
import com.outsidesource.oskitkmp.annotation.ExperimentalOSKitAPI
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
 *     override val locales = mapOf(
 *          "en" to StringsEnglish,
 *          "es" to StringsSpanish,
 *     )
 * }
 *
 * private object StringsEnglish: KMPStringSet() {
 *      override val strings: Map<KMPStringKey, String> = mapOf(
 *          Strings.hello to "Hello world!",
 *          Strings.myName to "My name is %s",
 *      }
 * }
 *
 * private object StringsSpanish: KMPStringSet() {
 *      override val strings: Map<KMPStringKey, String> = mapOf(
 *          Strings.hello to "Hola Mundo!",
 *          Strings.myName to "Mi nombre es %s",
 *      }
 * }
 *
 * @Composable
 * fun Test() {
 *     Text(kmpString(Strings.hello))
 *     Text(kmpString(Strings.myName, "Tom"))
 * }
 * ```
 */
@ExperimentalOSKitAPI
abstract class KMPStrings(private val replacementPattern: Regex = Regex("%s")) {
    private val keyId = atomic(0)
    protected abstract val locales: Map<String, KMPStringSet>

    protected fun kmpStringKey() = KMPStringKey(keyId.incrementAndGet(), ::localesInternal, replacementPattern)
    private fun localesInternal() = locales
}

@ExperimentalOSKitAPI
abstract class KMPStringSet {
    abstract val strings: Map<KMPStringKey, String>
}

@ExperimentalOSKitAPI
data class KMPStringKey(
    private val id: Int,
    internal val locales: () -> Map<String, KMPStringSet>,
    internal val replacementPattern: Regex,
)

@ExperimentalOSKitAPI
@Composable
fun kmpString(key: KMPStringKey, vararg args: String): String {
    val locale = Locale.current.language
    val string = key.locales()[locale]?.strings?.get(key) ?: ""

    return if (args.isNotEmpty()) {
        var index = 0
        return string.replace(key.replacementPattern) { args.getOrNull(index++) ?: "" }
    } else {
        string
    }
}

@ExperimentalOSKitAPI
fun kmpString(key: KMPStringKey, locale: Locale, vararg args: String): String {
    val string = key.locales()[locale.language]?.strings?.get(key) ?: ""
    Locale.current.language

    return if (args.isNotEmpty()) {
        var index = 0
        return string.replace(key.replacementPattern) { args.getOrNull(index++) ?: "" }
    } else {
        string
    }
}

@ExperimentalOSKitAPI
@Composable
fun rememberKmpString(key: KMPStringKey, vararg args: String): String {
    val locale = Locale.current.language

    return remember(locale, key, *args) {
        val string = key.locales()[locale]?.strings?.get(key) ?: ""

        if (args.isNotEmpty()) {
            var index = 0
            string.replace(key.replacementPattern) { args.getOrNull(index++) ?: "" }
        } else {
            string
        }
    }
}