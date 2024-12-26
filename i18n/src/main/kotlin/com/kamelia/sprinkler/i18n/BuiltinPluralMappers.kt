package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.util.unsafeCast
import com.zwendo.restrikt2.annotation.PackagePrivate
import java.util.*
import java.util.stream.Collectors
import kotlin.math.log10

/**
 * Default implementation of the [Plural.Mapper] that uses the builtin plural rules defined by
 * [unicode.org](https://www.unicode.org/cldr/charts/45/supplemental/language_plural_rules.html). This implementation
 * relies on a csv file located in the resources that contains the rules from Unicode in a simplified format. Each line
 * represents a locale and is parsed and loaded into a [Plural.Mapper].
 *
 * This object contains the logic to parse the tiny grammar representing the rules in the csv file.
 */
@PackagePrivate
internal object BuiltinPluralMappers {

    /**
     * The builtin [Plural.Mapper] factory.
     */
    fun factory(): (Locale) -> Plural.Mapper {
        val map = builtinMappers()
        return { loadMapper(map, it) }
    }

    private fun builtinMappers(): Map<String, String> = Plural::class.java
        .getResourceAsStream("plural_rules.csv")!!
        .bufferedReader()
        .lines()
        .map {
            val (code, rest) = it.split(";", limit = 2)
            code to rest
        }
        .collect(Collectors.toUnmodifiableMap({ it.first }, { it.second }))

    private fun loadMapper(content: Map<String, String>, locale: Locale): Plural.Mapper {
        // we first look for the whole tag, then for the language
        val rules = content[locale.toLanguageTag()]
            ?: content[locale.language]
            ?: throw LocaleNotFoundException(locale)
        return loadedMapper(rules)
    }

    class LocaleNotFoundException(val locale: Locale) : RuntimeException(null, null, false, false)

    private class BuiltinMapper(
        private val cardinal: IV2Plural,
        private val ordinal: IV2Plural,
    ) : Plural.Mapper {

        override fun mapCardinal(count: Double): Plural = cardinal(InputValue.IDouble(count))

        override fun mapCardinal(count: Long): Plural = cardinal(InputValue.ILong(count))

        override fun mapOrdinal(count: Long): Plural = ordinal(InputValue.ILong(count))

        override fun mapOrdinal(count: Double): Plural = ordinal(InputValue.IDouble(count))

        override fun toString(): String = "BuiltinPluralMapper(cardinal='$cardinal',ordinal='$ordinal')"

    }

    /**
     * Simple wrapper around a floating point or integer value. It is used to pass the value to the plural mapper.
     */
    private sealed interface InputValue {

        fun toLong(): ILong

        fun toFloatingPart(): ILong

        fun toFloatingPartDigitsCount(): ILong

        operator fun rem(divisor: Long): ILong

        class ILong(val long: Long) : InputValue {
            override fun toLong(): ILong = this
            override fun toFloatingPart(): ILong = ILong(0)
            override fun toFloatingPartDigitsCount(): ILong = ILong(0)
            override fun rem(divisor: Long): ILong = ILong(long % divisor)
            override fun toString(): String = long.toString()
        }

        class IDouble(val double: Double) : InputValue {
            override fun toLong(): ILong = ILong(double.toLong())
            override fun toFloatingPart(): ILong = ILong((double - double.toLong()).toLong())
            override fun toFloatingPartDigitsCount(): ILong = ILong(log10(double - double.toLong()).toLong())
            override fun rem(divisor: Long): ILong =
                throw UnsupportedOperationException("Cannot apply modulo to a floating point number")
            override fun toString(): String = double.toString()
        }

    }

    private fun loadedMapper(rules: String): Plural.Mapper {
        val (cardinal, ordinal) = rules.split(';')
        val cardinalMapper = parseRule(true, cardinal)
        val ordinalMapper = parseRule(false, ordinal)
        return BuiltinMapper(cardinalMapper, ordinalMapper)
    }

    private fun parseRule(isCardinal: Boolean, rule: String): IV2Plural =
        when (rule) {
            "" -> object : IV2Plural {
                override fun invoke(value: InputValue): Plural {
                    val kind = if (isCardinal) "Cardinal" else "Ordinal"
                    throw UnsupportedOperationException("$kind not supported by this mapper")
                }

                override fun toString(): String {
                    val kind = if (isCardinal) "Cardinal" else "Ordinal"
                    return "Unsupported $kind"
                }
            }

            "*" -> object : IV2Plural {
                override fun invoke(value: InputValue): Plural = Plural.OTHER
                override fun toString(): String = "*"
            }

            else -> parseRuleContent(rule)
        }

    private fun parseRuleContent(rule: String): IV2Plural {
        val parts = rule.split("//")
            .stream()
            .map {
                val (partName, partRule) = it.split(":")
                parseOrParts(partRule) to Plural.valueOf(partName.uppercase())
            }
            .toArray { arrayOfNulls<Pair<IV2Boolean, Plural>>(it) }
            .unsafeCast<Array<Pair<IV2Boolean, Plural>>>()
        return object : IV2Plural {
            override fun invoke(value: InputValue): Plural {
                for ((part, plural) in parts) {
                    if (part(value)) return plural
                }
                return Plural.OTHER
            }

            override fun toString(): String = parts.joinToString(" // ") { (part, plural) -> "$plural = $part" }
        }
    }

    private fun parseOrParts(part: String): IV2Boolean {
        val orParts = part.split("||")
        if (orParts.size == 1) return parseAndParts(orParts[0])
        val parts = orParts.map { parseAndParts(it) }.toTypedArray()
        return object : IV2Boolean {
            override fun invoke(value: InputValue): Boolean {
                return parts.any { it(value) }
            }

            override fun toString(): String = parts.joinToString(" || ")
        }
    }

    private fun parseAndParts(part: String): IV2Boolean {
        val andParts = part.split("&&")
        if (andParts.size == 1) return parseComparisonParts(andParts[0])
        val parts = andParts.map { parseComparisonParts(it) }.toTypedArray()
        return object : IV2Boolean {
            override fun invoke(value: InputValue): Boolean = parts.all { it(value) }
            override fun toString(): String = parts.joinToString(" && ")
        }
    }

    private fun parseComparisonParts(part: String): IV2Boolean {
        var isDifferent = true
        var comparisonParts = part.split("!=")
        if (comparisonParts.size == 1) {
            comparisonParts = part.split("=")
            isDifferent = false
        }
        assert(comparisonParts.size == 2) { "Invalid comparison part: $part" }
        val leftPart = parseComparisonLeftPart(comparisonParts[0])
        val rightPart = parseComparisonRightPart(comparisonParts[1])
        return object : IV2Boolean {
            private val isDiff = isDifferent
            override fun invoke(value: InputValue): Boolean {
                val left = leftPart(value)
                val validForRight = rightPart(left)
                return if (isDiff) !validForRight else validForRight
            }

            override fun toString(): String = "$leftPart ${if (isDiff) "!=" else "=="} $rightPart"
        }
    }

    private fun parseComparisonLeftPart(part: String): IV2IV {
        val parts = part.split("%")
        if (parts.size == 1) return parseInputValueAdapter(parts[0])
        val adapter = parseInputValueAdapter(parts[0])
        val modulo = parts[1].toLong()
        return object : IV2IV {
            override fun invoke(value: InputValue): InputValue {
                val result = adapter(value)
                return result % modulo
            }

            override fun toString(): String = "$adapter % $modulo"
        }
    }

    private fun parseInputValueAdapter(part: String): IV2IV {
        return object : IV2IV {
            override fun invoke(value: InputValue): InputValue =
                when (part) {
                    "n" -> value
                    "e" -> InputValue.ILong(0L)
                    "i" -> value.toLong()
                    "f" -> value.toFloatingPart()
                    "v" -> value.toFloatingPartDigitsCount()
                    else -> throw AssertionError("Unknown variable: $part")
                }

            override fun toString(): String = part
        }
    }

    private fun parseComparisonRightPart(part: String): IV2Boolean {
        val enumeration = part.split(",")
        if (enumeration.size == 1) return parseRangePart(enumeration[0])
        val parts = enumeration.map { parseRangePart(it) }.toTypedArray()
        return object : IV2Boolean {
            override fun invoke(value: InputValue): Boolean = parts.any { it(value) }
            override fun toString(): String = enumeration.joinToString(",")
        }
    }

    private fun parseRangePart(part: String): IV2Boolean {
        val range = part.split("..")
        if (range.size == 1) return parseValuePart(range[0])
        val start = range[0].toLong()
        val end = range[1].toLong()
        return object : IV2Boolean {
            override fun invoke(value: InputValue): Boolean =
                when (value) {
                    is InputValue.IDouble -> value.double in start.toDouble()..end.toDouble()
                    is InputValue.ILong -> value.long in start..end
                }

            override fun toString(): String = "$start..$end"
        }
    }

    /**
     * Simple value like `5` in `i = 5`.
     */
    private fun parseValuePart(part: String): IV2Boolean =
        object : IV2Boolean {
            private val value = part.toLong()
            override fun invoke(value: InputValue): Boolean =
                when (value) {
                    is InputValue.IDouble -> false
                    is InputValue.ILong -> value.long == this.value
                }

            override fun toString(): String = value.toString()
        }

    private interface IV2Boolean {
        operator fun invoke(value: InputValue): Boolean
    }


    private interface IV2Plural {
        operator fun invoke(value: InputValue): Plural
    }

    private interface IV2IV {
        operator fun invoke(value: InputValue): InputValue
    }

}
