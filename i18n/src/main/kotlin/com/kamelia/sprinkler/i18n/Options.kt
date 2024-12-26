@file:HideFromJava

package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.i18n.Options.context
import com.kamelia.sprinkler.i18n.Options.count
import com.kamelia.sprinkler.util.entryOf
import com.zwendo.restrikt2.annotation.HideFromJava
import com.zwendo.restrikt2.annotation.HideFromKotlin

/**
 * Class defining the options that can be passed as extra arguments during translations.
 *
 * **NOTE**: Options are a set of keys starting with an underscore, meaning that they can be used without passing by
 * this class and by directly creating pairs with the key and the value. However, options only work with a certain set
 * of types, which are the types passed as parameters to the methods of this class (e.g. [count] option only accepts
 * [Long] and [Double]). Passing values of an invalid type will result in an [IllegalArgumentException] being thrown
 * when passed to a [Translator] created from a [TranslatorBuilder].
 *
 * @see Translator
 */
@HideFromKotlin
object Options {

    /**
     * The count value of the translation, used to disambiguate plural forms.
     *
     * The most common use case is to disambiguate the plural form of a word, like in the following example:
     *
     * ```java
     * // content:
     * // {
     * //   "item_zero": "I have no items",
     * //   "item_one": "I have one item",
     * //   "item_other": "I have several items"
     * // }
     * class Main {
     *     Translator translator = ...
     *
     *     void items(long number) {
     *        var value = translator.t(
     *            "item",
     *            Map.ofEntries(count(number))
     *        )
     *        System.out.println(value)
     *     }
     *
     *     void run() {
     *         items(5) // prints "I have several items"
     *     }
     * }
     * ```
     *
     * As shown in the example above, the plural value actually appends the value to the key, separated by an
     * underscore.
     *
     * **NOTE**: The plural value is appended to the key after the [context] (e.g. `key_male_one`).
     *
     * @param count the count value to be used
     * @return a [Map.Entry] representing the count value
     *
     * @see Plural
     */
    @JvmStatic
    fun count(count: Long): Map.Entry<String, Any> = entryOf(COUNT, count)

    /**
     * The count value of the translation, used to disambiguate plural forms.
     *
     * The most common use case is to disambiguate the plural form of a word, like in the following example:
     *
     * ```java
     * // content:
     * // {
     * //   "item_zero": "I have no items",
     * //   "item_one": "I have one item",
     * //   "item_other": "I have several items"
     * // }
     * class Main {
     *     Translator translator = ...
     *
     *     void items(double number) {
     *        var value = translator.t(
     *            "item",
     *            Map.ofEntries(count(number))
     *        )
     *        System.out.println(value)
     *     }
     *
     *     void run() {
     *         items(2.5) // prints "I have several items"
     *     }
     * }
     * ```
     *
     * As shown in the example above, the plural value actually appends the value to the key, separated by an
     * underscore.
     *
     * **NOTE**: The plural value is appended to the key after the [context] (e.g. `key_male_one`).
     *
     * @param count the count value to be used
     * @return a [Map.Entry] representing the count value
     *
     * @see Plural
     */
    @JvmStatic
    fun count(count: Double): Map.Entry<String, Any> = entryOf(COUNT, count)

    /**
     * The count value of the translation, used to disambiguate plural forms.
     *
     * The most common use case is to disambiguate the plural form of a word, like in the following example:
     *
     * ```java
     * // content:
     * // {
     * //   "item_zero": "I have no items",
     * //   "item_one": "I have {{count, number}} item",
     * //   "item_other": "I have {{count, number}} items"
     * // }
     * class Main {
     *    Translator translator = ...
     *
     *    void items(Number number) {
     *        var value = translator.t(
     *            "item",
     *            Map.ofEntries(count(formatted(number, "minFracDigits" to 2))
     *        )
     *        System.out.println(value)
     *    }
     *
     *    void run() {
     *        items(2) // prints "I have 2.00 items"
     *    }
     * }
     */
    @JvmStatic
    fun count(count: FormattedValue): Map.Entry<String, Any> {
        val actualValue = count.value
        require(actualValue is Number) { "Count must be a number but was ${actualValue::class.java}" }
        return entryOf(COUNT, count)
    }

    /**
     * The context of the translation. It can be used to disambiguate translations.
     *
     * The most common use case is to disambiguate gender, like in the following example:
     *
     * ```java
     * // content:
     * // {
     * //   "greetings_male": "Hello mister",
     * //   "greetings_female": "Hello miss"
     * // }
     * class Main {
     *     Translator translator = ...
     *
     *     void greetings(boolean isMale) {
     *        var value = translator.t(
     *            "greetings",
     *            Map.ofEntries(context(isMale ? "male" : "female"))
     *        )
     *        System.out.println(value)
     *     }
     *
     *     void run() {
     *         greetings(true) // prints "Hello mister"
     *     }
     * }
     * ```
     *
     * As shown in the example above, the context actually appends the value to the key, separated by an underscore.
     *
     * **NOTE**: The context is appended to the key before the [plural][Plural] value which is
     * defined by the [count] option (e.g. a possible key with context and plural `key_male_one`).
     *
     * @param context the context value to be used
     * @return a [Map.Entry] representing the context value
     *
     * @see Plural
     */
    @JvmStatic
    fun context(context: String): Map.Entry<String, Any> = entryOf(CONTEXT, context)

    /**
     * Whether the count value should be treated as an ordinal number. It can be used to disambiguate ordinal forms. It
     * is only used if the [count] option is also present (otherwise it is ignored).
     *
     * The most common use case is to disambiguate the ordinal form of a word, like in the following example:
     *
     * ```java
     * // content:
     * // {
     * //   "item_ordinal_one": "I arrived {count}st",
     * //   "item_ordinal_two": "I arrived {count}nd",
     * //   "item_ordinal_few": "I arrived {count}rd",
     * //   "item_ordinal_other": "I arrived {rank}th"
     * // }
     * class Main {
     *     Translator translator = ...
     *
     *     void rank(int position) {
     *        var value = translator.t(
     *            "greetings",
     *            Map.ofEntries(count(position), ordinal(true))
     *        )
     *        System.out.println(value)
     *     }
     *
     *     void run() {
     *         rank(3) // prints "I arrived 3rd"
     *     }
     * }
     * ```
     *
     * As shown in the example above, the `ordinal` literal is appended to the key, separated by an underscore.
     *
     * **NOTE**: The value is right before the [plural][Plural] value (e.g. a possible key with
     * `key_male_ordinal_one`).
     *
     * @param value whether the count value should be treated as an ordinal number
     * @return a [Map.Entry] representing the ordinal value
     *
     * @see Plural
     */
    @JvmStatic
    fun ordinal(value: Boolean): Map.Entry<String, Any> = entryOf(ORDINAL, value)

    /**
     * Force the count value to be treated as an ordinal number. It can be used to disambiguate ordinal forms. It is
     * only used if the [count] option is also present (otherwise it is ignored).
     *
     * The most common use case is to disambiguate the ordinal form of a word, like in the following example:
     *
     * ```java
     * // content:
     * // {
     * //   "item_ordinal_one": "I arrived {count}st",
     * //   "item_ordinal_two": "I arrived {count}nd",
     * //   "item_ordinal_few": "I arrived {count}rd",
     * //   "item_ordinal_other": "I arrived {rank}th"
     * // }
     * class Main {
     *     Translator translator = ...
     *
     *     void rank(int position) {
     *        var value = translator.t(
     *            "greetings",
     *            Map.ofEntries(count(position), ordinal())
     *        )
     *        System.out.println(value)
     *     }
     *
     *     void run() {
     *         rank(4) // prints "I arrived 4th"
     *     }
     * }
     * ```
     *
     * As shown in the example above, the `ordinal` literal is appended to the key, separated by an underscore.
     *
     * **NOTE**: The value is right before the [plural][Plural] value (e.g. a possible key with
     * `key_male_ordinal_one`).
     *
     * @return a [Map.Entry] representing the ordinal value set to `true`
     *
     * @see Plural
     */
    @JvmStatic
    fun ordinal(): Map.Entry<String, Any> = ordinal(true)

    internal const val CONTEXT = "_context"

    internal const val COUNT = "_count"

    internal const val ORDINAL = "_ordinal"

}

/**
 * The count value of the translation, used to disambiguate plural forms.
 *
 * The most common use case is to disambiguate the plural form of a word, like in the following example:
 *
 * ```
 * // content:
 * // {
 * //   "item_zero": "I have no items",
 * //   "item_one": "I have one item",
 * //   "item_other": "I have several items"
 * // }
 * val translator: Translator = ...
 *
 * fun items(number: Long) {
 *    val value = translator.t(
 *        "item",
 *        mapOf(count(number))
 *    )
 *    println(value)
 * }
 *
 * fun main() {
 *     items(5) // prints "I have several items"
 * }
 *
 * ```
 *
 * As shown in the example above, the plural value actually appends the value to the key, separated by an
 * underscore.
 *
 * **NOTE**: The plural value is appended to the key after the [context] (e.g. `key_male_one`).
 *
 * @param count the count value to be used
 * @return a [Pair] representing the count value
 *
 * @see Plural
 */
fun count(count: Long): Pair<String, Any> = Options.COUNT to count

/**
 * The count value of the translation, used to disambiguate plural forms.
 *
 * The most common use case is to disambiguate the plural form of a word, like in the following example:
 *
 * ```
 * // content:
 * // {
 * //   "item_zero": "I have no items",
 * //   "item_one": "I have one item",
 * //   "item_other": "I have several items"
 * // }
 * val translator: Translator = ...
 *
 * fun items(number: Double) {
 *    val value = translator.t(
 *        "item",
 *        mapOf(count(number))
 *    )
 *    println(value)
 * }
 *
 * fun main() {
 *     items(2.5) // prints "I have several items"
 * }
 * ```
 *
 * As shown in the example above, the plural value actually appends the value to the key, separated by an
 * underscore.
 *
 * **NOTE**: The plural value is appended to the key after the [context] (e.g. `key_male_one`).
 *
 * @param count the count value to be used
 * @return a [Pair] representing the count value
 *
 * @see Plural
 */
fun count(count: Double): Pair<String, Any> = Options.COUNT to count

/**
 * The count value of the translation, used to disambiguate plural forms.
 *
 * The most common use case is to disambiguate the plural form of a word, like in the following example:
 *
 * ```
 * // content:
 * // {
 * //   "item_zero": "I have no items",
 * //   "item_one": "I have {{count, number}} item",
 * //   "item_other": "I have {{count, number}} items"
 * // }
 * val translator: Translator = ...
 *
 * fun items(number: Number) {
 *    val value = translator.t(
 *        "item",
 *        mapOf(count(formatted(number, "minFracDigits" to 2))
 *    )
 *    println(value)
 * }
 *
 * fun main() {
 *    items(2) // prints "I have 2.00 items"
 * }
 */
fun count(count: FormattedValue): Pair<String, Any> {
    val actualValue = count.value
    require(actualValue is Number) { "Count must be a number but was ${actualValue::class.java}" }
    return Options.COUNT to count
}

/**
 * The context of the translation. It can be used to disambiguate translations.
 *
 * The most common use case is to disambiguate gender, like in the following example:
 *
 * ```
 * // content:
 * // {
 * //   "greetings_male": "Hello mister",
 * //   "greetings_female": "Hello miss"
 * // }
 * val translator: Translator = ...
 *
 * fun greetings(isMale: Boolean) {
 *     val value = translator.t(
 *         "greetings",
 *         mapOf(context(if (isMale) "male" else "female"))
 *     )
 *     println(value)
 * }
 *
 * fun main() {
 *    greetings(true) // prints "Hello mister"
 * }
 * ```
 *
 * As shown in the example above, the context actually appends the value to the key, separated by an underscore.
 *
 * **NOTE**: The context is appended to the key before the [plural][Plural] value which is
 * defined by the [count] option (e.g. a possible key with context and plural `key_male_one`).
 *
 * @param context the context value to be used
 * @return a [Pair] representing the context value
 *
 * @see Plural
 */
fun context(context: String): Pair<String, Any> = Options.CONTEXT to context

/**
 * Whether the count value should be treated as an ordinal number. It can be used to disambiguate ordinal forms.
 * It is only used if the [count] option is also present (otherwise it is ignored).
 *
 * The most common use case is to disambiguate the ordinal form of a word, like in the following example:
 *
 * ```
 * // content:
 * // {
 * //   "item_ordinal_one": "I arrived {count}st",
 * //   "item_ordinal_two": "I arrived {count}nd",
 * //   "item_ordinal_few": "I arrived {count}rd",
 * //   "item_ordinal_other": "I arrived {rank}th"
 * // }
 * val translator: Translator = ...
 *
 * fun rank(count: Int) {
 *    val value = translator.t(
 *        "item",
 *        mapOf(count(count), ordinal(true))
 *    )
 *    println(value)
 * }
 *
 * fun main() {
 *    rank(3) // prints "I arrived 3rd"
 * }
 * ```
 *
 * As shown in the example above, the `ordinal` literal is appended to the key, separated by an underscore.
 *
 * **NOTE**: The value is right before the [plural][Plural] value (e.g. a possible key with
 * `key_male_ordinal_one`).
 *
 * @param value whether the count value should be treated as an ordinal number
 * @return a [Pair] representing the ordinal value
 *
 * @see Plural
 */
fun ordinal(value: Boolean = true): Pair<String, Any> = Options.ORDINAL to value
