package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.i18n.TranslatorConfiguration.Companion.builder
import com.kamelia.sprinkler.util.VariableDelimiter
import com.zwendo.restrikt2.annotation.PackagePrivate
import java.util.*

/**
 * Configuration of a [Translator]. This class defines rules applied to a [Translator] and all [Translator]s created
 * from it.
 *
 * @see builder
 * @see TranslatorConfiguration.Builder
 * @see TranslatorBuilder
 * @see Translator
 */
class TranslatorConfiguration @PackagePrivate internal constructor(
    internal val interpolationDelimiter: VariableDelimiter,
    internal val pluralMapperFactory: (Locale) -> Plural.Mapper,
    internal val formatters: Map<String, VariableFormatter<out Any>>,
    internal val missingKeyPolicy: MissingKeyPolicy,
) {

    /**
     * Builder for [TranslatorConfiguration].
     */
    class Builder @PackagePrivate internal constructor() {

        /**
         * The delimiter to use for interpolation in translations.
         *
         * default: '{{' and '}}'
         */
        private var interpolationDelimiter: InterpolationDelimiter = InterpolationDelimiter(VariableDelimiter.default)

        /**
         * The function that will be used when building the [Translator] to create all the required [Plural.Mapper]s.
         *
         * default: [Plural.builtinMappers]
         */
        private var pluralMapperFactory: (Locale) -> Plural.Mapper = BuiltinPluralMappers.factory()

        /**
         * Map used to find formatters using their name during variable interpolation.
         *
         * default: [VariableFormatter.builtins]
         *
         * @see VariableFormatter
         */
        private var formatters: MutableMap<String, VariableFormatter<out Any>> = VariableFormatter.mutableBuiltins()

        /**
         * The policy to use when a key is not found.
         *
         * default: [MissingKeyPolicy.THROW_EXCEPTION]
         *
         * @see MissingKeyPolicy
         */
        private var missingKeyPolicy: MissingKeyPolicy = MissingKeyPolicy.THROW_EXCEPTION

        /**
         * Sets the delimiter to use for interpolation in translations.
         *
         * default: '{{' and '}}'
         *
         * @param value the delimiter to use
         * @return this [Builder]
         */
        fun withInterpolationDelimiter(value: InterpolationDelimiter): Builder = apply { interpolationDelimiter = value }

        /**
         * Sets the plural mapper factory to use when building the [Translator] to create all the required
         * [Plural.Mapper]s.
         *
         * default: [Plural.builtinMappers]
         *
         * @param factory the mapper function to use
         * @return this [Builder]
         *
         */
        fun withPluralMapperFactory(factory: (Locale) -> Plural.Mapper): Builder = apply { this.pluralMapperFactory = factory }

        /**
         * Sets the map used to find formatters using their name during variable interpolation.
         *
         * default: [VariableFormatter.builtins]
         *
         * @param formatters the map to use
         * @return this [Builder]
         */
        fun withFormatters(formatters: Map<String, VariableFormatter<out Any>>): Builder = apply { this.formatters = HashMap(formatters) }

        /**
         * Adds a formatter to the map used to find formatters using their name during variable interpolation. In case
         * a formatter with the same name already exists, it will be replaced.
         *
         * @param name the name of the formatter
         * @param formatter the formatter to add
         * @return this [Builder]
         */
        fun addFormatter(name: String, formatter: VariableFormatter<out Any>): Builder = apply {
            formatters[name] = formatter
        }

        /**
         * Clears the map used to find formatters using their name during variable interpolation.
         *
         * @return this [Builder]
         */
        fun clearFormatters(): Builder = apply { formatters = HashMap() }

        /**
         * Sets the policy to use when a key is not found.
         *
         * default: [MissingKeyPolicy.THROW_EXCEPTION]
         *
         * @param missingKeyPolicy the policy to use
         * @return this [Builder]
         */
        fun withMissingKeyPolicy(missingKeyPolicy: MissingKeyPolicy): Builder = apply {
            this.missingKeyPolicy = missingKeyPolicy
        }

        /**
         * Builds the [TranslatorConfiguration].
         *
         * @return the built [TranslatorConfiguration]
         */
        fun build(): TranslatorConfiguration = TranslatorConfiguration(
            interpolationDelimiter.inner,
            pluralMapperFactory,
            formatters,
            missingKeyPolicy,
        )

    }


    /**
     * Policy to use when a key is not found.
     */
    enum class MissingKeyPolicy {

        /**
         * Throw an exception when a key is not found.
         */
        THROW_EXCEPTION,

        /**
         * Return the given key itself when a key is not found.
         */
        RETURN_KEY,

        ;

    }

    /**
     * Delimiter to use for interpolation in translations.
     */
    class InterpolationDelimiter @PackagePrivate internal constructor(
        internal val inner: VariableDelimiter,
    ) {

        companion object {

            /**
             * Creates an [InterpolationDelimiter] using the given [start] and [end] delimiters.
             *
             * The delimiters cannot contain the following characters: `\`, `(`, `)`, `:`. Trying to create a delimiter
             * containing one of these characters will throw an [IllegalStateException].
             *
             * @param start the start delimiter
             * @param end the end delimiter
             * @return the created [InterpolationDelimiter]
             * @throws IllegalStateException if the delimiters contain forbidden characters
             */
            @JvmStatic
            fun create(start: String, end: String): InterpolationDelimiter {
                val inner = VariableDelimiter.create(start, end)
                val ch = forbiddenChars()
                val forbiddenChars = ch.joinToString("", "[^", "]*") { it.toString() }.toRegex()
                check(forbiddenChars.matches(inner.startDelimiter)) {
                    "Start delimiter cannot contain the following characters: ${ch.contentToString()}, but was '${inner.startDelimiter}'"
                }
                check(forbiddenChars.matches(inner.endDelimiter)) {
                    "End delimiter cannot contain the following characters: ${ch.contentToString()}, but was '${inner.endDelimiter}'"
                }
                return InterpolationDelimiter(inner)
            }

            private fun forbiddenChars(): CharArray = charArrayOf('\\', '(', ')', ':')

        }

    }

    companion object {

        /**
         * Creates a [TranslatorConfiguration.Builder].
         *
         * @return the created [TranslatorConfiguration.Builder]
         */
        @JvmStatic
        fun builder(): Builder = Builder()

    }

    override fun toString(): String =
        "TranslatorConfiguration(interpolationDelimiter=$interpolationDelimiter, pluralMapperFactory=$pluralMapperFactory, formatters=$formatters, missingKeyPolicy=$missingKeyPolicy)"

}
