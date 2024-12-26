package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.util.assertionFailed
import com.kamelia.sprinkler.util.entryOf
import java.util.*
import java.util.stream.Collectors

/**
 * The plural value of the translation. It can be used to disambiguate translations depending on the number of
 * items.
 *
 * @see Options
 * @see TranslatorConfiguration.Builder.pluralMapperFactory
 */
enum class Plural {

    /**
     * Plural value usually used in case the count is 0.
     */
    ZERO,

    /**
     * Plural value usually used in case the count is 1.
     */
    ONE,

    /**
     * Plural value usually used in case the count is 2.
     */
    TWO,

    /**
     * Plural value usually used in case the count represents a few items.
     */
    FEW,

    /**
     * Plural value usually used in case the count represents many items.
     */
    MANY,

    /**
     * Plural value used as default when no other value matches.
     */
    OTHER,

    ;

    /**
     * A mapper that associates a [Locale] and a [count][Int] to a [Plural] value.
     *
     * Because several languages treat differently integers and floating point numbers, for these languages, calls like
     * `mapCardinal(1)` and `mapCardinal(1.0)` should return different values.
     */
    interface Mapper {

        /**
         * Maps the given [count] to a [Plural] value. This method associates the parameters in the context of a count
         * of items.
         *
         * @param count the count to use
         * @return the [Plural] value
         * @throws IllegalArgumentException if the given [count] is negative
         */
        fun mapCardinal(count: Double): Plural

        /**
         * Maps the given [count] to a [Plural] value. This method associates the parameters in the context of a count
         * of items.
         *
         * @param count the count to use
         * @return the [Plural] value
         * @throws IllegalArgumentException if the given [count] is negative
         */
        fun mapCardinal(count: Long): Plural = mapCardinal(count.toDouble())

        /**
         * Maps the given [count] to a [Plural] value. This method associates the parameters in the context of a count
         * of items.
         *
         * @param count the count to use
         * @return the [Plural] value
         * @throws IllegalArgumentException if the given [count] is negative
         */
        fun mapCardinal(count: Number): Plural =
            when (count) {
                is Byte,
                is Short,
                is Int,
                    -> mapCardinal(count.toLong())

                is Long -> mapCardinal(count)
                is Float -> mapCardinal(count.toDouble())
                is Double -> mapCardinal(count)
                else -> assertionFailed("Unsupported number type: ${count.javaClass}")
            }

        /**
         * Maps the given [count] to a [Plural] value. This method associates the parameters in the context of an
         * ordinal number.
         *
         * @param count the count to use
         * @return the [Plural] value
         * @throws IllegalArgumentException if the given [count] is negative or zero
         * @throws UnsupportedOperationException if the implementation does not support floating point numbers for
         * ordinal
         */
        fun mapOrdinal(count: Double): Plural =
            throw UnsupportedOperationException("Floating point numbers are not supported for ordinal numbers")

        /**
         * Maps the given [count] to a [Plural] value. This method associates the parameters in the context of an
         * ordinal number.
         *
         * @param count the count to use
         * @return the [Plural] value
         * @throws IllegalArgumentException if the given [count] is negative or zero
         */
        fun mapOrdinal(count: Long): Plural

        /**
         * Maps the given [count] to a [Plural] value. This method associates the parameters in the context of an
         * ordinal number.
         *
         * @param count the count to use
         * @return the [Plural] value
         * @throws IllegalArgumentException if the given [count] is negative or zero
         */
        fun mapOrdinal(count: Number): Plural =
            when (count) {
                is Byte,
                is Short,
                is Int,
                    -> mapOrdinal(count.toLong())

                is Long -> mapOrdinal(count)
                is Float -> mapOrdinal(count.toDouble())
                is Double -> mapOrdinal(count)
                else -> assertionFailed("Unsupported number type: ${count.javaClass}")
            }

    }

    companion object {

        /**
         * The english [Mapper] implementation. This implementation always returns plural values as if the locale was
         * [English][Locale.ENGLISH].
         *
         * @return the default implementation of the [Mapper] interface
         */
        @JvmStatic
        fun englishMapper(): Mapper = object : Mapper {

            override fun mapCardinal(count: Double): Plural {
                require(count >= 0.0) { "count must be >= 0, but was $count" }
                return OTHER
            }

            override fun mapCardinal(count: Long): Plural {
                require(count >= 0L) { "count must be >= 0, but was $count" }
                return when (count) {
                    1L -> ONE
                    else -> OTHER
                }
            }

            override fun mapOrdinal(count: Long): Plural {
                require(count >= 1L) { "count must be >= 1, but was $count" }
                return when (count % 10) {
                    1L -> ONE
                    2L -> TWO
                    3L -> FEW
                    else -> OTHER
                }
            }

            override fun toString(): String = "Plural.englishMapper()"

        }

        /**
         * A [Mapper] factory. This factory uses the plural rules defined by the
         * [Unicode CLDR](https://www.unicode.org/cldr/charts/45/supplemental/language_plural_rules.html) to create
         * the corresponding [Mapper] implementation.
         *
         * @param locales the set of locales to create the mappers for
         * @return a factory that always returns the [englishMapper] implementation
         * @throws IllegalArgumentException if a locale in the set is not supported
         */
        @JvmStatic
        fun builtinMappers(locales: Set<Locale>): Map<Locale, Mapper> {
            val factory = BuiltinPluralMappers.factory()
            return try {
                locales
                    .stream()
                    .map { entryOf(it, factory(it)) }
                    .collect(
                        Collectors.toUnmodifiableMap(
                            { it.key },
                            { it.value },
                            { _, _ -> throw AssertionError("A set should not allow duplicates") },
                        )
                    )
            } catch (e: BuiltinPluralMappers.LocaleNotFoundException) {
                throw IllegalArgumentException("Unsupported locale: ${e.locale}")
            }
        }

        /**
         * The default [Mapper] implementation. This implementation always returns [Plural.OTHER].
         *
         * @return the default implementation of the [Mapper] interface
         */
        @JvmStatic
        fun nullMapper(): Mapper = object : Mapper {
            override fun mapCardinal(count: Double): Plural {
                require(count >= 0.0) { "count must be >= 0, but was $count" }
                return OTHER
            }

            override fun mapOrdinal(count: Long): Plural {
                require(count >= 1L) { "count must be >= 1, but was $count" }
                return OTHER
            }

            override fun toString(): String = "Plural.nullMapper()"
        }

        /**
         * A [Mapper] factory. This factory always returns the [nullMapper] implementation for any locale.
         *
         * @return a factory that always returns the [nullMapper] implementation
         */
        @JvmStatic
        fun nullFactory(): (Locale) -> Mapper = { nullMapper() }

    }

    /**
     * Used to create a translation key with the plural value.
     */
    internal val representation: String
        get() = name.lowercase()

}
