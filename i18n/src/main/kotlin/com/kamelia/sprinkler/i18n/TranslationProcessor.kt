package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.util.*
import com.zwendo.restrikt2.annotation.PackagePrivate
import org.intellij.lang.annotations.Language
import java.util.*

@PackagePrivate
internal object TranslationProcessor {

    fun translate(data: TranslatorData, key: String, extraArgs: Map<String, Any>, locale: Locale): String? {
        // first, get the translations for the given locale, or return null if they don't exist
        val translations = data.translations[locale] ?: return null

        // build the actual key with the options
        val actualKey = buildKey(key, extraArgs, data.pluralMapper(locale))

        // get the value for the actual key or return null if it doesn't exist
        val value = translations[actualKey] ?: return null

        val context = ProcessingContext(data.formatters.unsafeCast(), locale, extraArgs)
        return value.interpolate(context, data.interpolationDelimiter, customResolver)
    }

    fun buildKey(key: String, optionMap: Map<String, Any>, pluralMapper: Plural.Mapper): String {
        if (optionMap.isEmpty()) return key

        val builder = StringBuilder(key)

        val interpolationContext = optionMap.context()
        if (interpolationContext != null) {
            builder.append("_")
                .append(interpolationContext)
        }


        val ordinal = optionMap.ordinal()
        val pluralValue = optionMap.count(ordinal, pluralMapper)
        if (pluralValue != null) {
            if (ordinal) {
                builder.append("_ordinal")
            }
            builder.append("_")
                .append(pluralValue.representation)
        }

        return builder.toString()
    }

    fun Map<String, Any>.context(): String? {
        val context = get(Options.CONTEXT) ?: return null
        val actualContext = if (context is FormattedValueImpl) context.value else context
        require(actualContext is String) { "Context must be a string but was ${actualContext::class.java}" }
        return actualContext
    }

    fun Map<String, Any>.ordinal(): Boolean {
        val ordinal = get(Options.ORDINAL) ?: return false
        val actualOrdinal = if (ordinal is FormattedValueImpl) ordinal.value else ordinal
        require(actualOrdinal is Boolean) { "Ordinal must be a boolean but was ${actualOrdinal::class.java}" }
        return actualOrdinal
    }

    fun Map<String, Any>.count(ordinal: Boolean, pluralMapper: Plural.Mapper): Plural? {
        val count = get(Options.COUNT) ?: return null
        val actualCount = if (count is FormattedValueImpl) count.value else count
        require(actualCount is Number) { "Count must be a number but was ${actualCount::class.java}" }
        return if (ordinal) {
            pluralMapper.mapOrdinal(actualCount)
        } else {
            pluralMapper.mapCardinal(actualCount)
        }
    }

    /**
     * This regex is globally the same as the translation value format check regex except that this regex actually
     * captures the information in groups, whereas the other one only checks if the format is valid.
     */
    private val generalSplit: Regex

    private val paramsSplit = """(?<!\\),""".toRegex()

    private val keyValueSplit = """(?<!\\):""".toRegex()

    internal class ProcessingContext(
        val formatters: (String) -> VariableFormatter<Any>,
        val locale: Locale,
        val extraArgs: Map<String, Any>,
    )

    init {
        // capture all the params in a single group
        // any char and ending with a non-escaped ')', no need further validation as the value has already been validated
        // on translator creation
        @Language("RegExp")
        val formatParams = """\((.+(?<!\\))\)"""

        // capture the format name
        @Language("RegExp")
        val format = """\s*,\s*($IDENTIFIER)\s*(?:$formatParams)?"""

        // capture the variable name
        generalSplit = """\s*(${Regex.escape(NESTED_KEY_CHAR.toString())}?$IDENTIFIER)(?:$format)?\s*""".toRegex()
    }


    internal val customResolver = object : VariableResolver<ProcessingContext> {

        override fun resolveTo(builder: Appendable, name: String, context: ProcessingContext) {
            // '!!' is ok, because values are validated on translator creation
            val (_, variableName, formatName, formatParams) = generalSplit.matchEntire(name)!!.groupValues

            val variableValue = context.extraArgs[variableName]
                ?: context.extraArgs["_$variableName"]
                ?: illegalArgument("variable '$variableName' not found")

            var formatPassedParams = emptyMap<String, Any>()
            var actualValue: Any = variableValue
            if (variableValue is FormattedValueImpl) {
                actualValue = variableValue.value
                formatPassedParams = variableValue.formatParams
            }

            if (formatName.isEmpty()) { // if there is no format, just append the value
                builder.append(actualValue.toString())
                return
            }

            // otherwise, there is a format
            val paramMap = if (formatParams.isEmpty() && formatPassedParams.isEmpty()) {
                emptyMap()
            } else {
                HashMap<String, Any>(formatParams.length).apply {
                    if (formatParams.isNotEmpty()) { // there are format parameters
                        formatParams.split(paramsSplit).forEach {
                            val (k, v) = keyValueSplit.split(it, 2)
                            put(k, v)
                        }
                    }
                    putAll(formatPassedParams) // add the formats passed after to override the defaults
                }
            }
            // same as above, '!!' is ok due to pre-validation
            context.formatters(formatName).format(builder, actualValue, context.locale, paramMap)
        }

        override fun resolve(name: String, context: ProcessingContext): String =
            assertionFailed("This method should never be called")

    }

}
