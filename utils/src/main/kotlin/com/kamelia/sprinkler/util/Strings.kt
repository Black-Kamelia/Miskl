@file:JvmName("Strings")

package com.kamelia.sprinkler.util

/**
 * Interpolates variables in this string using the given [resolver]. This function replaces all occurrences of
 * `{variable}` with the value of the variable returned by [VariableResolver.value].
 *
 * Names must be alphanumeric, and may contain underscores and dashes.
 *
 * &nbsp;
 *
 * This function can be used as follows:
 *
 * ```kt
 * val resolver: NameResolver = ...
 * val result = "Hello {name}, you are {age} years old".interpolate(resolver)
 * ```
 *
 * @param resolver the [VariableResolver] to use for resolving variable names
 * @return the interpolated string
 * @throws IllegalArgumentException if a variable has an invalid name, or if an error occurs while resolving a variable
 * @see [VariableResolver]
 */
fun String.interpolate(resolver: VariableResolver): String {
    val builder = StringBuilder()
    var state = State.DEFAULT
    var keyBuilder = StringBuilder()

    forEachIndexed { index, char ->
        when (state) {
            State.DEFAULT -> {
                when (char) {
                    '{' -> state = State.IN_CURLY
                    '\\' -> state = State.BACKSLASH
                    else -> builder.append(char)
                }
            }
            State.BACKSLASH -> {
                state = State.DEFAULT
                builder.append(char)
            }
            State.IN_CURLY -> {
                if ('}' == char) {
                    val key = keyBuilder.toString()
                    val value = try {
                        resolver.value(key)
                    } catch (e: VariableResolver.NameResolutionException) {
                        illegalArgument("Error while resolving variable '$key': ${e.message!!}")
                    }
                    builder.append(value)
                    keyBuilder = StringBuilder()
                    state = State.DEFAULT
                } else {
                    if ('_' != char && '-' != char && !char.isLetterOrDigit()) {
                        illegalArgument("Unexpected character '$char' in interpolated value near character ${index + 1}")
                    } else {
                        keyBuilder.append(char)
                    }
                }
            }
        }
    }

    return builder.toString()
}

/**
 * Interpolates variables in this string using the given vararg [args].
 *
 * Variable are resolved by their index in the given [args]. The variable passed to is parsed as an integer, and the
 * value at the corresponding index in the [array][args] is returned. If name does not represent a valid integer, or if
 * the index is not in between 0 and the number of arguments, an [IllegalArgumentException] is thrown.
 *
 * &nbsp;
 *
 * It can be used as follows:
 *
 * ```kt
 * val result = "Hello {0}, you are {1} years old".interpolate("John", 42)
 * ```
 *
 * @param args the array of values
 * @return the interpolated string
 * @throws IllegalArgumentException if a variable has an invalid name, or if a variable name is not a valid integer or
 * is if the index is out of bounds
 * @see [VariableResolver]
 */
fun String.interpolate(vararg args: Any?): String = interpolate(VariableResolver.create(*args))

/**
 * Interpolates variables in this string using the given map of [args].
 *
 * Variable are resolved by their name in the given [map][args]. The name of the variable passed to is used as a key in
 * the [map][args], and the value associated with that key is returned. If a variable is unknown, the [fallback] value
 * is returned. If the [fallback] value is `null`, an [IllegalArgumentException] is thrown.
 *
 * &nbsp;
 *
 * It can be used as follows:
 *
 * ```kt
 * val result = "Hello {name}, you are {age} years old".interpolate(mapOf("name" to "John", "age" to 42))
 * ```
 *
 * @param args the map of values
 * @param fallback the fallback value (defaults to `null`)
 * @return the interpolated string
 * @throws IllegalArgumentException if a variable has an invalid name, or if a variable name is unknown and the
 * [fallback] value is `null`
 * @see [VariableResolver]
 */
fun String.interpolate(args: Map<String, Any>, fallback: String? = null): String =
    interpolate(VariableResolver.create(args, fallback))

/**
 * Interpolates variables in this string using the given [Pair] array [args]. The array of pairs is converted to a
 * [map][Map].
 *
 * Variable are resolved by their name in the [Pair] array [args]. The name of the variable passed to is used as a key
 * in the map created from the [array][args] and the value associated with that key is returned. If a variable is
 * unknown, the [fallback] value is returned. If the [fallback] value is `null`, an [IllegalArgumentException] is
 * thrown.
 *
 * &nbsp;
 *
 * It can be used as follows:
 *
 * ```kt
 * val result = "Hello {name}, you are {age} years old".interpolate("name" to "John", "age" to 42)
 * ```
 *
 * @param args the array of pairs
 * @param fallback the fallback value (defaults to `null`)
 * @return the interpolated string
 * @throws IllegalArgumentException if a variable has an invalid name, or if a variable name is unknown and the
 * [fallback] value is `null`
 * @see [VariableResolver]
 */
fun String.interpolate(vararg args: Pair<String, Any>, fallback: String? = null): String =
    interpolate(VariableResolver.create(args.toMap(), fallback))

/**
 * Interface for resolving variables during string interpolation. This interface maps variable names to their
 * values.
 *
 * @see [String.interpolate]
 * @see [VariableResolver.create]
 */
fun interface VariableResolver {

    /**
     * Returns the value of the variable with the given [name].
     *
     * Implementations may throw an [NameResolutionException] if the variable is unknown, or return a default value.
     *
     * @param name the name of the variable
     * @return the value of the variable
     * @throws NameResolutionException if the variable is unknown
     */
    fun value(name: String): String

    /**
     * Exception thrown by [VariableResolver] implementations when a variable name cannot be resolved.
     *
     * @param message the exception message
     * @see VariableResolver.value
     */
    class NameResolutionException(message: String) : IllegalArgumentException(message) {

        override fun fillInStackTrace(): Throwable = this // no need to fill in the stack trace

    }

    companion object {

        /**
         * Creates a [VariableResolver] that resolves variables by their index in the given [array][args].
         *
         * The variable passed to [VariableResolver.value] is parsed as an integer, and the value at the corresponding index
         * in the [array][args] is returned. If name does not represent a valid integer, or if the index is out of
         * bounds, an [NameResolutionException] is thrown.
         *
         * Example:
         * ```kt
         * val resolver = NameResolver.create("foo", "bar", "baz")
         * val result = "Hello {0}, {2}, {1}".interpolate(resolver)
         * println(result) // prints "Hello foo, baz, bar"
         * ```
         *
         * @param args the array of values
         * @return a [VariableResolver] that resolves variables by their index in the given [array][args]
         */
        fun create(vararg args: Any?): VariableResolver =
            VariableResolver { name ->
                val index = name.toIntOrNull()
                    ?: throw NameResolutionException("index must be a parsable integer, but was'$name'")
                if (index !in args.indices) {
                    throw NameResolutionException("index must be in between 0 and ${args.size}, but was $index")
                }
                args[index].toString()
            }

        /**
         * Creates a [VariableResolver] that resolves variables by their name in the given [map][args].
         *
         * The name of the variable passed to [VariableResolver.value] is used as a key in the [map][args], and the value
         * associated with that key is returned. If a variable is unknown, the [fallback] value is returned. If the
         * [fallback] value is `null`, an [NameResolutionException] is thrown.
         *
         * Example:
         * ```kt
         * val resolver = NameResolver.create(mapOf("name" to "John", "age" to 42), fallback = "unknown")
         * val result = "Hello {name}, you are {age} years old, and you live in {city}".interpolate(resolver)
         * println(result) // prints "Hello John, you are 42 years old, and you live in unknown"
         * ```
         *
         * @param args the map of values
         * @param fallback the fallback value (defaults to `null`)
         * @return a [VariableResolver] that resolves variables by their name in the given [map][args]
         */
        fun create(args: Map<String, Any>, fallback: String? = null): VariableResolver =
            VariableResolver { name ->
                args[name]?.toString() ?: fallback ?: throw NameResolutionException("unknown variable name '$name'")
            }

        /**
         * Creates a [VariableResolver] that resolves variables by their name in the [Pair] array [args]. The array of pairs
         * is converted to a [map][Map].
         *
         * The name of the variable passed to [VariableResolver.value] is used as a key in the map created from the
         * [array][args] and the value associated with that key is returned. If a variable is unknown, the [fallback]
         * value is returned. If the [fallback] value is `null`, an [NameResolutionException] is thrown.
         *
         * Example:
         * ```kt
         * val resolver = NameResolver.create("name" to "John", "age" to 42, fallback = "unknown")
         * val result = "Hello {name}, you are {age} years old, and you live in {city}".interpolate(resolver)
         * println(result) // prints "Hello John, you are 42 years old, and you live in unknown"
         * ```
         *
         * @param args the array of pairs
         * @param fallback the fallback value (defaults to `null`)
         * @return a [VariableResolver] that resolves variables by their name in the [Pair] array [args]
         */
        fun create(vararg args: Pair<String, Any>, fallback: String? = null): VariableResolver =
            create(args.toMap(), fallback)

    }

}


private enum class State {
    DEFAULT,
    BACKSLASH,
    IN_CURLY,
}