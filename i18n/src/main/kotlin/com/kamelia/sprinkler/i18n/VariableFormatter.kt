package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.util.illegalArgument
import com.kamelia.sprinkler.util.toUnmodifiableMap
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor
import java.util.*

/**
 * Represents an object that can format specific values depending on the locale. Formatting is performed through the
 * [format] method, which writes the formatted value to an [Appendable] using the given locale and extra arguments.
 *
 * Formatter implementations should ignore any extra arguments that they do not recognize.
 *
 * The following formatters are built-in:
 * - [VariableFormatter.currency]
 * - [VariableFormatter.date]
 * - [VariableFormatter.time]
 * - [VariableFormatter.datetime]
 * - [VariableFormatter.number]
 */
fun interface VariableFormatter<T> {

    /**
     * Formats the given [value] to an [Appendable] using the given [locale] and [extraArgs].
     *
     * @param appendable the appendable to write the formatted value to
     * @param value the value to format
     * @param locale the locale to use
     * @param extraArgs the extra arguments to use
     * @throws RuntimeException if an error occurs during formatting
     */
    fun format(appendable: Appendable, value: T, locale: Locale, extraArgs: Map<String, Any>)

    companion object {

        /**
         * Returns a [VariableFormatter] that formats the variable as a currency using the locale, following the
         * rules of the [NumberFormat.getCurrencyInstance][java.text.NumberFormat.getCurrencyInstance] method, including
         * the currency symbol, its position, and the thousands and floating point symbols.
         *
         * For example, if the locale is `en_US`, the value `1234.56` will be formatted as `$1,234.56`.
         *
         * The extra arguments are the same as that of the [number] formatter.
         *
         * @return A [VariableFormatter] that formats the variable as a currency using the locale.
         */
        @JvmStatic
        fun currency(): VariableFormatter<Number> = object : VariableFormatter<Number> {

            override fun format(appendable: Appendable, value: Number, locale: Locale, extraArgs: Map<String, Any>) {
                val inner = NumberFormat.getCurrencyInstance(locale)
                parseNumberFormatParams(inner, extraArgs)
                appendable.append(inner.format(value))
            }

            override fun toString(): String = "VariableFormatter.currency()"

        }

        /**
         * Returns a [VariableFormatter] that formats the variable as a readable date using the locale, following the
         * rules of the [DateTimeFormatter.ofLocalizedDate][java.time.format.DateTimeFormatter.ofLocalizedDate] method.
         *
         * For example, if the locale is `en_US`, the value `2023-01-01` will be formatted as `Jan 1, 2023` by default.
         *
         * The only recognized extra argument is `dateStyle`, which specify the style to use to format the date. The
         * value must be a string that can be parsed by the [FormatStyle.valueOf][java.time.format.FormatStyle.valueOf]
         * method, and the default is `MEDIUM`.
         *
         * @return A [VariableFormatter] that formats the variable as a readable date using the locale.
         */
        @JvmStatic
        fun date(): VariableFormatter<TemporalAccessor> = object : VariableFormatter<TemporalAccessor> {

            override fun format(
                appendable: Appendable,
                value: TemporalAccessor,
                locale: Locale,
                extraArgs: Map<String, Any>,
            ) {
                val inner = createDateTimeFormatParams(DateTimeFormatterKind.DATE, extraArgs).localizedBy(locale)
                inner.formatTo(value, appendable)
            }

            override fun toString(): String = "VariableFormatter.date()"

        }

        /**
         * Returns a [VariableFormatter] that formats the variable as a readable date using the locale, following the
         * rules of the [DateTimeFormatter.ofLocalizedTime][java.time.format.DateTimeFormatter.ofLocalizedTime] method.
         *
         * For example, if the locale is `en_US`, the value `12:34:56` will be formatted as `12:34:56 PM` by default.
         *
         * The only recognized extra argument is `timeStyle`, which specify the style to use to format the time. The
         * value must be a string that can be parsed by the [FormatStyle.valueOf][java.time.format.FormatStyle.valueOf]
         * method, and the default is `MEDIUM`.
         *
         * @return A [VariableFormatter] that formats the variable as a readable date using the locale.
         */
        @JvmStatic
        fun time(): VariableFormatter<TemporalAccessor> = object : VariableFormatter<TemporalAccessor> {

            override fun format(
                appendable: Appendable,
                value: TemporalAccessor,
                locale: Locale,
                extraArgs: Map<String, Any>,
            ) {
                val inner = createDateTimeFormatParams(DateTimeFormatterKind.TIME, extraArgs).localizedBy(locale)
                inner.formatTo(value, appendable)
            }

            override fun toString(): String = "VariableFormatter.time()"

        }

        /**
         * Returns a [VariableFormatter] that formats the variable as a readable date using the locale, following the
         * rules of the [DateTimeFormatter.ofLocalizedDateTime][java.time.format.DateTimeFormatter.ofLocalizedDateTime]
         * method.
         *
         * For example, if the locale is `en_US`, the value `2023-01-01T12:34:56` will be formatted as
         * `Jan 1, 2023, 12:34:56 PM` by default.
         *
         * The extra arguments are the same as that of the [date] and [time] formatters.
         *
         * @return A [VariableFormatter] that formats the variable as a readable date using the locale.
         */
        @JvmStatic
        fun datetime(): VariableFormatter<TemporalAccessor> = object : VariableFormatter<TemporalAccessor> {

            override fun format(appendable: Appendable, value: TemporalAccessor, locale: Locale, extraArgs: Map<String, Any>) {
                val inner = createDateTimeFormatParams(DateTimeFormatterKind.DATE_TIME, extraArgs).localizedBy(locale)
                inner.formatTo(value, appendable)
            }

            override fun toString(): String = "VariableFormatter.datetime()"

        }

        /**
         * Returns a [VariableFormatter] that formats the variable as a readable number using the locale, following the
         * rules of the [NumberFormat.getInstance][java.text.NumberFormat.getInstance] method, including the thousands
         * and floating point symbols.
         *
         * For example, if the locale is `en_US`, the value `1234.56` will be formatted as `1,234.56`.
         *
         * The extra arguments are restrictions to apply to the value:
         * - `minIntDigits`: the minimum number of digits to use for the integer part of the number (will be padded with
         *   non-significant zeros if needed). For example, if the value is `1,234.56` and the parameter is `6`, the
         *   result will be `001,234.56`.
         * - `maxIntDigits`: the maximum number of digits to use for the integer part of the number (will be truncated
         *   from the left if needed). For example, if the value is `1234.56` and the parameter is `2`, the result will
         *   be `34.56`.
         * - `minFracDigits`: the minimum number of digits to use for the fractional part of the number (will be padded
         *   with non-significant zeros if needed). For example, if the value is `1234.56` and the parameter is `4`, the
         *   result will be `1,234.5600`.
         * - `maxFracDigits`: the maximum number of digits to use for the fractional part of the number (will be
         *   truncated from the right if needed). For example, if the value is `1234.5678` and the parameter is `2`, the
         *   result will be `1,234.56`.
         * - `groupingUsed`: whether to use the grouping separator (e.g. `,` in `1,234.56`) or not. The value must be a
         *   boolean, and the default is `true`.
         * - `roundingMode`: the rounding mode to use when truncating the number. The value must be a string that can be
         *   parsed by the [RoundingMode.valueOf][java.math.RoundingMode.valueOf] method, and the default is
         *   `HALF_EVEN`.
         *
         * @return A [VariableFormatter] that formats the variable as a readable number using the locale.
         */
        @JvmStatic
        fun number(): VariableFormatter<Number> = object : VariableFormatter<Number> {

            override fun format(appendable: Appendable, value: Number, locale: Locale, extraArgs: Map<String, Any>) {
                val inner = NumberFormat.getInstance(locale)
                parseNumberFormatParams(inner, extraArgs)
                appendable.append(inner.format(value))
            }

            override fun toString(): String = "VariableFormatter.number()"

        }

        /**
         * Returns a map of all the built-in variable formatters, with their name as the key.
         *
         * The names of the formatters are assigned according to the name of their factory method, e.g. the name of the
         * [currency] formatter is `currency`.
         *
         * @return the map of all the built-in variable formatters
         */
        @JvmStatic
        fun builtins(): Map<String, VariableFormatter<out Any>> = mutableBuiltins().toUnmodifiableMap()

        internal fun mutableBuiltins(): MutableMap<String, VariableFormatter<out Any>> = hashMapOf(
            ::currency.name to currency(),
            ::date.name to date(),
            ::time.name to time(),
            ::datetime.name to datetime(),
            ::number.name to number(),
        )

        private fun parseNumberFormatParams(formatter: NumberFormat, params: Map<String, Any>) {
            params.forEach { (key, value) ->
                when (key) {
                    "minIntDigits" -> formatter.minimumIntegerDigits = value.toInt()
                    "maxIntDigits" -> formatter.maximumIntegerDigits = value.toInt()
                    "minFracDigits" -> formatter.minimumFractionDigits = value.toInt()
                    "maxFracDigits" -> formatter.maximumFractionDigits = value.toInt()
                    "groupingUsed" -> formatter.isGroupingUsed = value.toBooleanStrict()
                    "roundingMode" -> formatter.roundingMode = value.toEnum(RoundingMode::class.java)
                }
            }
        }

        private enum class DateTimeFormatterKind {
            DATE,
            TIME,
            DATE_TIME,
        }

        private fun createDateTimeFormatParams(
            kind: DateTimeFormatterKind,
            params: Map<String, Any>,
        ): DateTimeFormatter {
            var firstFormat: FormatStyle = DEFAULT_FORMAT_STYLE
            var secondFormat: FormatStyle = DEFAULT_FORMAT_STYLE
            params.forEach { (key, value) ->
                if ("dateStyle" == key && (DateTimeFormatterKind.DATE == kind || DateTimeFormatterKind.DATE_TIME == kind)) {
                    firstFormat = value.toEnum(FormatStyle::class.java)
                } else if ("timeStyle" == key && (kind == DateTimeFormatterKind.TIME || kind == DateTimeFormatterKind.DATE_TIME)) {
                    if (DateTimeFormatterKind.DATE_TIME == kind) {
                        secondFormat = value.toEnum(FormatStyle::class.java)
                    } else {
                        firstFormat = value.toEnum(FormatStyle::class.java)
                    }
                }
            }

            return when (kind) {
                DateTimeFormatterKind.DATE -> DateTimeFormatter.ofLocalizedDate(firstFormat)
                DateTimeFormatterKind.TIME -> DateTimeFormatter.ofLocalizedTime(firstFormat)
                DateTimeFormatterKind.DATE_TIME -> DateTimeFormatter.ofLocalizedDateTime(firstFormat, secondFormat)
            }
        }

        private fun Any.toInt(): Int = when (this) {
            is Int -> this
            is String -> Integer.valueOf(this)
            else -> illegalArgument("Invalid integer value: $this, must be a number or a parsable string")
        }

        private fun Any.toBooleanStrict(): Boolean = when (this) {
            is Boolean -> this
            is String -> when (this.lowercase()) {
                "true" -> true
                "false" -> false
                else -> illegalArgument("Invalid boolean value: $this, must be 'true' or 'false'")
            }
            else -> illegalArgument("Invalid boolean value: $this, must be a boolean or a parsable string")
        }

        private fun <T : Enum<T>> Any.toEnum(cl: Class<T>): T = when {
            cl.isInstance(this) -> @Suppress("UNCHECKED_CAST") (this as T)
            this is String -> try {
                java.lang.Enum.valueOf(cl, this.uppercase(Locale.ENGLISH))
            } catch (e: IllegalArgumentException) {
                illegalArgument("Invalid enum value: $this")
            }
            else -> illegalArgument("Invalid enum value: $this, must be a ${cl.simpleName} label or a parsable string")
        }

        private val DEFAULT_FORMAT_STYLE = FormatStyle.MEDIUM

    }

}
