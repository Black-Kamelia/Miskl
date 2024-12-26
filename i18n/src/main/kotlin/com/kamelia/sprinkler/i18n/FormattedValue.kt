@file:JvmName("FormattedValues")

package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.util.toUnmodifiableMap
import com.zwendo.restrikt2.annotation.PackagePrivate

/**
 * Represents a value and the parameters of the format to be applied to it. It is used to pass parameters to the
 * formatter on translation call instead of directly putting them in the translation string.
 *
 * It can be used in the following way:
 *
 * ```
 * // the value of the key "my.key" is "Hello, {name, myFormatter}!"
 * val translator: Translator = ...
 * val value = translator.t(
 *     "my.key",
 *     mapOf(
 *         "name" to formatted("World", "myFormatterParam" to "foo")
 *     )
 * )
 * ```
 *
 * In the above example, after retrieving the value of the key "my.key" from the translator, the formatter `myFormatter`
 * will be called on the value "World" with the parameter "myFormatterParam" set to "foo".
 *
 * @see formatted
 */
sealed interface FormattedValue

/**
 * Creates a [FormattedValue] instance with the given value and format parameters.
 *
 * It can be used in the following way:
 *
 * ```
 * // the value of the key "my.key" is "Hello, {name, myFormatter}!"
 * val translator: Translator = ...
 * val formattingParams = mapOf("myFormatterParam" to "foo")
 * val value = translator.t(
 *     "my.key",
 *     mapOf("name" to formatted("World", formattingParams))
 * )
 * ```
 *
 * @param value the value to be formatted
 * @param formatParams the parameters to be used in the format
 * @return a [FormattedValue] instance with the given value and format parameters
 */
fun formatted(value: Any, formatParams: Map<String, Any>): FormattedValue = FormattedValueImpl(value, formatParams.toUnmodifiableMap())

/**
 * Creates a [FormattedValue] instance with the given value and format parameters.
 *
 * It can be used in the following way:
 *
 * ```
 * // the value of the key "my.key" is "Hello, {name, myFormatter}!"
 * val translator: Translator = ...
 * val value = translator.t(
 *     "my.key",
 *     mapOf("name" to formatted("World", "myFormatterParam" to "foo"))
 * )
 * ```
 *
 * @param value the value to be formatted
 * @param formatParams the parameters to be used in the format
 * @return a [FormattedValue] instance with the given value and format parameters
 */
fun formatted(value: Any, vararg formatParams: Pair<String, Any>): FormattedValue = FormattedValueImpl(value, formatParams.toUnmodifiableMap())

/**
 * Creates a [FormattedValue] instance with the given value and format parameters.
 *
 * It can be used in the following way:
 *
 * ```
 * // the value of the key "my.key" is "Hello, {name, myFormatter}!"
 * val translator: Translator = ...
 * val value = translator.t(
 *     "my.key",
 *     mapOf("name" to formatted("World", java.util.Map.entry("myFormatterParam", "foo"))
 * )
 * ```
 *
 * @param value the value to be formatted
 * @param formatParams the parameters to be used in the format
 * @return a [FormattedValue] instance with the given value and format parameters
 */
fun formatted(value: Any, vararg formatParams: Map.Entry<String, Any>): FormattedValue = FormattedValueImpl(value, formatParams.toUnmodifiableMap())


@PackagePrivate
internal class FormattedValueImpl(
    val value: Any,
    val formatParams: Map<String, Any>,
) : FormattedValue

@PackagePrivate
val FormattedValue.value: Any
    get() = (this as FormattedValueImpl).value