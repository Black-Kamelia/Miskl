@file:JvmName("Interpolations")

package com.kamelia.sprinkler.util

import org.intellij.lang.annotations.Language
import java.util.*

/**
 * Interpolates variables in this string using the given [resolver]. This function replaces all sequence of characters
 * placed between two specific sequences of characters defined as start and end delimiters. The value used to replace
 * these sequences is resolved using the [resolver] parameter which maps variable names to their values thanks to the
 * [context] parameter.
 *
 * Any sequence that matches the start or end delimiter (encountered after the start delimiter) are considered as such.
 * To escape a delimiter, it must be preceded by a backslash (`\`).
 *
 * This function can be used as follows:
 *
 * ```kt
 * val resolver: VariableResolver<MyContext> = ...
 * val context: MyContext = ...
 * val result = "Hello {{name}}, you are {{age}} years old".interpolate(context, resolver = resolver)
 * ```
 *
 * Or you can create a custom [VariableResolver] on the fly:
 *
 * ```kt
 * val context: MyContext = ...
 * val result = "Hello {{name}}, you are {{age}} years old".interpolate(context) { name, context ->
 *    // custom logic to resolve the variable
 * }
 * ```
 *
 * There are several overloads of this function that can be used to interpolate variables using different types of
 * [context] like a map, a list, a vararg, etc.
 *
 * **NOTE**: Depending on the implementation of [VariableResolver] used, this function may throw an
 * [IllegalArgumentException] if a variable name is invalid for the given [VariableResolver].
 *
 * @receiver the string to interpolate
 * @param context the context to use for resolving the variable
 * @param delimiter the delimitation of the variable (defaults to [VariableDelimiter.default])
 * @param resolver the [VariableResolver] to use for resolving variable names
 * @return the interpolated string
 * @throws IllegalArgumentException if a variable name is invalid for the given [VariableResolver]
 * @see VariableResolver
 * @see VariableDelimiter
 */
@JvmOverloads
fun <T> String.interpolate(
    context: T,
    delimiter: VariableDelimiter = VariableDelimiter.default,
    resolver: VariableResolver<T>,
): String {
    val builder = StringBuilder()
    interpolateTo(builder, context, delimiter, resolver)
    return builder.toString()
}

/**
 * Interpolates variables in this string to the given [builder].
 *
 * For more documentation about the implementation, see [String.interpolate].
 *
 * @receiver the string to interpolate
 * @param builder the builder to append the interpolated string to
 * @param context the context to use for resolving the variable
 * @param delimiter the delimitation of the variable (defaults to [VariableDelimiter.default])
 * @param resolver the [VariableResolver] to use for resolving variable names
 * @see String.interpolate
 */
@JvmOverloads
fun <T> String.interpolateTo(
    builder: StringBuilder,
    context: T,
    delimiter: VariableDelimiter = VariableDelimiter.default,
    resolver: VariableResolver<T>,
) {
    // this function is a copy of the kotlin.text.Regex#replace(CharSequence,(MatchResult) -> CharSequence) function
    // the code is pasted here to avoid variable capture in a lambda which would be created for each call to this
    var match: MatchResult? = delimiter.regex.find(this)
    if (match == null) {
        builder.append(this)
        return
    }

    var lastStart = 0
    val length = length
    do {
        val foundMatch = match!!
        builder.append(this, lastStart, foundMatch.range.first)
        // lambda instantiation avoided here
        resolver.resolveTo(builder, foundMatch.groupValues[1], context)
        lastStart = foundMatch.range.last + 1
        match = foundMatch.next()
    } while (lastStart < length && match != null)

    if (lastStart < length) {
        builder.append(this, lastStart, length)
    }
}

/**
 * Interpolates variables in this string using the given vararg [args].
 *
 * Variables are resolved by their index in the given [args]. The variable passed to is parsed as an integer, and the
 * value at the corresponding index in the [array][args] is returned.
 *
 * The following rules apply to variable names:
 * - Name must be a **valid integer** ;
 * - The **index** specified in the name must be in between **0** and the **number of arguments in [args]** - 1.
 *
 * Any string that does not conform to these rules is considered invalid, a call to this function with an invalid string
 * will result in an [IllegalArgumentException] being thrown.
 *
 * This function can be used as follows:
 *
 * ```kt
 * val result = "Hello {{0}}, you are {{1}} years old".interpolateIdx("John", 42)
 * ```
 *
 * **NOTE**: The [interpolateIdx] function is an overload of this function that uses the
 * [default][VariableDelimiter.default] [VariableDelimiter].
 *
 * @receiver the string to interpolate
 * @param delimiter the delimitation of the variable
 * @param args the vararg of values
 * @return the interpolated string
 * @throws IllegalArgumentException if a variable name does not conform to the rules defined above
 * @see VariableResolver.fromArray
 */
fun String.interpolateIdxD(delimiter: VariableDelimiter, vararg args: Any): String =
    interpolate(args, delimiter, VariableResolver.fromArray())

/**
 * Overload of [String.interpolateIdxD] that uses the [default][VariableDelimiter.default] [VariableDelimiter].
 *
 * @receiver the string to interpolate
 * @param args the vararg of values
 * @return the interpolated string
 * @throws IllegalArgumentException if a variable name does not conform to the rules defined in [String.interpolateIdx]
 * @see VariableResolver.fromArray
 * @see interpolateIdxD
 */
fun String.interpolateIdx(vararg args: Any): String = interpolate(args, resolver = VariableResolver.fromArray())

/**
 * Interpolates variables in this string using the given vararg [args], converted to an [Iterator].
 *
 * Variables are resolved in the order they appear in the string, using the [Iterator.next] method to get the next
 * value. If the iterator has no more elements, an [IllegalArgumentException] is thrown.
 *
 * This function can be used as follows:
 * ```kt
 * val result = "Hello {{}}, {{}}, {{}}".interpolateIdxIt("John", 42, "foo")
 * ```
 *
 * @receiver the string to interpolate
 * @param delimiter the delimitation of the variable
 * @param args the vararg of values
 * @return the interpolated string
 * @throws IllegalArgumentException if the iterator has no more elements and a variable is found
 * @see VariableResolver.fromIterator
 */
fun String.interpolateItD(delimiter: VariableDelimiter, vararg args: Any): String =
    interpolate(args.iterator(), delimiter, VariableResolver.fromIterator())

/**
 * Overload of [String.interpolateItD] that uses the [default][VariableDelimiter.default] [VariableDelimiter].
 *
 * @receiver the string to interpolate
 * @param args the vararg of values
 * @return the interpolated string
 * @throws IllegalArgumentException if the iterator has no more elements and a variable is found
 * @see VariableResolver.fromIterator
 * @see interpolateItD
 */
fun String.interpolateIt(vararg args: Any): String =
    interpolate(args.iterator(), resolver = VariableResolver.fromIterator())

/**
 * Interpolates variables in this string using the given map of [args].
 *
 * Variables are resolved by their name in the given [map][args]. The name of the variable passed to is used as a key in
 * the [map][args], and the value associated with that key is returned. If a variable name is unknown, an
 * [IllegalArgumentException] is thrown.
 *
 * It can be used as follows:
 *
 * ```kt
 * val result = "Hello {{name}}, you are {{age}} years old".interpolate(mapOf("name" to "John", "age" to 42))
 * ```
 *
 * @param args the map of values
 * @param delimiter the delimitation of the variable (defaults to [VariableDelimiter.default])
 * @return the interpolated string
 * @throws IllegalArgumentException if a variable name is unknown
 * @see VariableResolver.fromMap
 */
@JvmOverloads
fun String.interpolate(
    args: Map<String, Any>,
    delimiter: VariableDelimiter = VariableDelimiter.default,
): String =
    interpolate(args, delimiter, VariableResolver.fromMap())

/**
 * Interpolates variables in this string using the given [Pair] array [args]. The array of pairs is converted to a
 * [map][Map].
 *
 * Variables are resolved by their name represented by the [first][Pair.first] value of each pair. The name of the
 * variable passed to is used as a key in the map created from the [array][args] and the value associated with that key
 * is returned. If a variable name is unknown, an [IllegalArgumentException] is thrown.
 *
 * Strings must follow the same rules as defined in [String.interpolate].
 *
 * It can be used as follows:
 *
 * ```kt
 * val result = "Hello {{name}}, you are {{age}} years old".interpolate("name" to "John", "age" to 42)
 * ```
 *
 * @param args the array of pairs
 * @param delimiter the delimitation of the variable (defaults to [VariableDelimiter.default])
 * @return the interpolated string
 * @throws IllegalArgumentException if a variable name is unknown
 * @see VariableResolver.fromMap
 */
@JvmOverloads
fun String.interpolate(
    vararg args: Pair<String, Any>,
    delimiter: VariableDelimiter = VariableDelimiter.default,
): String =
    interpolate(args.toMap(), delimiter)

/**
 * Interpolates variables in this string using the given list [args].
 *
 * Variables are resolved by their index in the given [args]. The variable passed to is parsed as an integer, and the
 * value at the corresponding index in the [list][args] is returned.
 *
 * The following rules apply to variable names:
 * - Name must be a **valid integer** ;
 * - The **index** specified in the name must be in between **0** and the **number of arguments in [args]** - 1.
 *
 * Any string that does not conform to these rules is considered invalid, a call to this function with an invalid string
 * will result in an [IllegalArgumentException] being thrown.
 *
 * This function can be used as follows:
 *
 * ```kt
 * val args = listOf("John", 42)
 * val result = "Hello {{0}}, you are {{1}} years old".interpolate(args)
 * ```
 *
 * @param args the list of values
 * @param delimiter the delimitation of the variable (defaults to [VariableDelimiter.default])
 * @return the interpolated string
 * @throws IllegalArgumentException if a variable name does not conform to the rules defined above
 * @see VariableResolver.fromList
 */
@JvmOverloads
fun String.interpolate(
    args: List<Any>,
    delimiter: VariableDelimiter = VariableDelimiter.default,
): String =
    interpolate(args, delimiter, VariableResolver.fromList())

/**
 * Interpolates variables in this string using the given iterable [args].
 *
 * Variables are resolved in the order they appear in the string, using the [Iterator.next] method to get the next
 * value. If the iterator has no more elements, an [IllegalArgumentException] is thrown.
 *
 * This function can be used as follows:
 * ```kt
 * val args = listOf("John", 42).iterator()
 * val result = "Hello {{}}, you are {{}} years old".interpolate(args)
 * ```
 *
 * **NOTE**: To use this method with a list, you need to cast it to an iterable, otherwise the [String.interpolate]
 * method expecting indices will be used (this is due to compiler method call resolution).
 *
 * @param args an iterable of values
 * @param delimiter the delimitation of the variable (defaults to [VariableDelimiter.default])
 * @return the interpolated string
 * @throws IllegalArgumentException if the iterator has no more elements and a variable is found
 * @see VariableResolver.fromIterator
 */
@JvmOverloads
fun String.interpolate(args: Iterable<Any>, delimiter: VariableDelimiter = VariableDelimiter.default): String =
    interpolate(args.iterator(), delimiter, VariableResolver.fromIterator())

/**
 * Interface for resolving variables during string interpolation. This interface maps variable names to their values.
 *
 * @see interpolate
 * @param T the type of the context used for resolving the variable
 */
fun interface VariableResolver<T> {

    /**
     * Returns the value of the variable with the given [name].
     *
     * Implementations may throw an [IllegalArgumentException] if the variable is unknown, or return a default value.
     *
     * @param name the name of the variable
     * @param context the context to use for resolving the variable
     * @return the value of the variable
     * @throws IllegalArgumentException if the variable is unknown
     */
    fun resolve(name: String, context: T): String

    fun resolveTo(builder: Appendable, name: String, context: T) {
        builder.append(resolve(name, context))
    }

    companion object {

        /**
         * Creates a [VariableResolver] that resolves variables by their index in a list.
         *
         * The variable passed to [VariableResolver.resolve] is parsed as an integer, and the value at the corresponding
         * index in the is returned. If name does not represent a valid integer, or if the index is out of bounds, an
         * [IllegalArgumentException] is thrown.
         *
         * Example:
         * ```kt
         * val args = listOf("foo", "bar", "baz")
         * val resolver = VariableResolver.fromList()
         * val result = "Hello {{0}}, {{2}}, {{1}}".interpolate(resolver, args)
         * println(result) // prints "Hello foo, baz, bar"
         * ```
         *
         * @return a [VariableResolver] that resolves variables by their index in a list
         */
        @JvmStatic
        fun fromList(): VariableResolver<List<Any>> =
            VariableResolver { name, context ->
                val index = requireNotNull(name.toIntOrNull()) { "Expected an integer for variable '$name'" }
                require(index >= 0 && index < context.size) {
                    "Index out of bounds for variable '$name': expected an integer between 0 and ${context.size - 1} (inclusive)"
                }
                context[index].toString()
            }

        /**
         * Creates a [VariableResolver] that resolves variables by their index in an array.
         *
         * The variable passed to [VariableResolver.resolve] is parsed as an integer, and the value at the corresponding
         * index in the array is returned. If name does not represent a valid integer, or if the index is out of bounds,
         * an [IllegalArgumentException] is thrown.
         *
         * Example:
         * ```kt
         * val args = arrayOf("foo", "bar", "baz")
         * val resolver = VariableResolver.fromArray()
         * val result = "Hello {{0}}, {{2}}, {{1}}".interpolate(resolver, args)
         * println(result) // prints "Hello foo, baz, bar"
         * ```
         *
         * @return a [VariableResolver] that resolves variables by their index in an array
         */
        @JvmStatic
        fun fromArray(): VariableResolver<Array<out Any>> =
            VariableResolver { name, context ->
                val index = requireNotNull(name.toIntOrNull()) { "Expected an integer for variable '$name'" }
                require(index >= 0 && index < context.size) {
                    "Index out of bounds for variable '$name': expected an integer between 0 and ${context.size - 1} (inclusive)"
                }
                context[index].toString()
            }

        /**
         * Creates a [VariableResolver] that resolves variables by their name in a map.
         *
         * The name of the variable passed to [VariableResolver.resolve] is used as a key in the map, and the value
         * associated with that key is returned. If a variable
         *
         * Example:
         * ```kt
         * val args = mapOf("name" to "John", "age" to 42)
         * val resolver = VariableResolver.fromMap()
         * val result = "Hello {{name}}, you are {{age}} years old.".interpolate(resolver, args)
         * println(result) // prints "Hello John, you are 42 years old."
         * ```
         *
         * @return a [VariableResolver] that resolves variables by their name in a map
         */
        @JvmStatic
        fun fromMap(): VariableResolver<Map<String, Any>> =
            VariableResolver { name, context ->
                context[name]?.toString() ?: throw IllegalArgumentException("No value found for variable '$name'")
            }

        /**
         * Creates a [VariableResolver] that resolves variables using in an iterator. Each call to
         * [VariableResolver.resolve] will use the [Iterator.next] method to get the next value.
         *
         * If the iterator has no more elements, an [IllegalArgumentException] is thrown.
         *
         * Example:
         * ```kt
         * val args = listOf("foo", "bar", "baz").iterator()
         * val resolver = VariableResolver.fromIterator()
         * val result = "Hello {{}}, {{}}, {{}}".interpolate(resolver, args)
         * println(result) // prints "Hello foo, bar, baz"
         * ```
         *
         * @return a [VariableResolver] that resolves variables using in an iterator
         */
        @JvmStatic
        fun fromIterator(): VariableResolver<Iterator<Any>> =
            VariableResolver { _, context ->
                require(context.hasNext()) { "No available variable" }
                context.next().toString()
            }

    }

}

/**
 * Represents the delimitation of a variable in an interpolated string.
 *
 * @property startDelimiter The start delimiter of the variable.
 * @property endDelimiter The end delimiter of the variable.
 * @see VariableResolver
 * @see interpolate
 */
class VariableDelimiter private constructor(
    val startDelimiter: String,
    val endDelimiter: String,
    internal val regex: Regex,
) {

    companion object {

        /**
         * The default [VariableDelimiter] used by [String.interpolate]. It uses the strings `"{{"` and `"}}"` as
         * delimiters.
         */
        @JvmStatic
        @get:JvmName("defaultDelimiter")
        val default: VariableDelimiter = create("{{", "}}")

        /**
         * Creates a [VariableDelimiter] using the given [start] and [end] delimiters.
         *
         * The [start] and [end] delimiters must follow these rules:
         * - They must not be blank ;
         * - They must be different ;
         * - They must not contain the escape character (`\`).
         *
         * If the delimiters do not follow these rules, an [IllegalArgumentException] is thrown.
         *
         * @param start the start delimiter
         * @param end the end delimiter
         * @return a [VariableDelimiter] using the given [start] and [end] delimiters
         * @throws IllegalArgumentException if the delimiters does not follow the rules defined above
         */
        @JvmStatic
        fun create(start: String, end: String): VariableDelimiter {
            require(start.isNotBlank()) { "start must not be blank" }
            require(end.isNotBlank()) { "end must not be blank" }
            require(start != end) { "start and end must be different" }
            require('\\' !in start) { "start must not contain the escape character" }
            require('\\' !in end) { "end must not contain the escape character" }

            val s = Regex.escape(start)
            val e = Regex.escape(end)

            val validContent = if (end.length > 1) {
                val last = Regex.escape(end.last().toString())
                val prefix = Regex.escape(end.substring(0, end.length - 1))

                @Language("RegExp") // variable needed to apply the @Language annotation for syntax highlighting
                val r = """(?:[^$last]|(?<!$prefix)$last|(?<=\\$prefix)$last)*?"""
                r
            } else {
                @Language("RegExp") // same as above
                val r = """(?:[^$e]|(?<=\\)$e)*?"""
                r
            }

            val regex = """(?<!\\)$s($validContent)(?<!\\)$e""".toRegex()
            return VariableDelimiter(start, end, regex)
        }

        /**
         * Backward compatibility method
         */
        @JvmName("default")
        internal fun default(): VariableDelimiter = default

    }



    override fun toString(): String =
        "VariableDelimiter(startDelimiter='$startDelimiter', endDelimiter='$endDelimiter')"

    override fun equals(other: Any?): Boolean =
        other is VariableDelimiter && startDelimiter == other.startDelimiter && endDelimiter == other.endDelimiter

    override fun hashCode(): Int = Objects.hash(startDelimiter, endDelimiter)

}
