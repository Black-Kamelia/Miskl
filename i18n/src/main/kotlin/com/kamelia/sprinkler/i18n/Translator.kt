package com.kamelia.sprinkler.i18n

import java.util.*

/**
 * Interface representing an object that can be used to translate strings. Through [t], a key can be translated using a
 * [Locale].
 *
 * Keys must follow the rules defined in the [TranslationKey] typealias. Any key that does not respect these rules will
 * result in an exception being thrown.
 *
 * This interface also provides a set of utility functions (such as [section] or [withNewCurrentLocale]) to reduce
 * boilerplate code when translating strings.
 *
 * @see TranslatorBuilder
 */
interface Translator {

    /**
     * The prefix prepended to all keys used to translate values. If null, the [Translator] is a root [Translator].
     */
    val prefix: String?

    /**
     * Whether this [Translator] is a root [Translator], meaning that no prefix is prepended to the keys used to
     * translate values.
     */
    val isRoot: Boolean
        get() = prefix == null

    /**
     * The default locale that is used as a fallback when a translation is not found for the current locale.
     */
    val defaultLocale: Locale

    /**
     * The current locale used to translate keys.
     */
    val currentLocale: Locale

    /**
     * Returns the translation using the provided information. If the translation is not found, it will return null.
     *
     * The order of resolution is the following:
     * - First, the translation is searched for the given [key] and [locale]
     * - Then, it will try to find a valid translation for the keys provided in [fallbacks] in order.
     * - The next step is, if the [locale] is different from the [fallbackLocale], to repeat the previous steps using
     * the [fallbackLocale] instead of the [locale].
     * - Finally, if no translation is found, null is returned.
     *
     * **NOTE**: During all the steps above, the [options] parameter remains the same for each translation attempt.
     *
     * @param key the key to translate
     * @param options the options to use
     * @param locale the locale to use for the translation
     * @param fallbackLocale the fallback locale to use if the translation is not found for the given [locale]
     * @param fallbacks the fallback keys to use if the translation is not found for the given [locale] and
     * [fallbackLocale]
     * @return the translation using the provided information, or null if the translation is not found
     * @throws IllegalArgumentException if the key is not [valid][TranslationKey] or if the [options] are not valid
     */
    fun tn(
        key: TranslationKey,
        options: Map<TranslationOption, Any>,
        locale: Locale,
        fallbackLocale: Locale?,
        vararg fallbacks: String,
    ): String?

    /**
     * Returns the translation using the provided information. If the translation is not found, it will return null.
     *
     * This method is an overload, see the documentation of the base method [tn] for more details.
     *
     * @param key the key to translate
     * @param options the options to use
     * @param locale the locale to use for the translation
     * @param fallbacks the fallback keys to use if the translation is not found for the given [locale]
     * @return the translation using the provided information, or null if the translation is not found
     * @throws IllegalArgumentException if the key is not [valid][TranslationKey] or if the [options] are not valid
     */
    fun tn(
        key: TranslationKey,
        options: Map<TranslationOption, Any>,
        locale: Locale,
        vararg fallbacks: String,
    ): String? =
        tn(key, options, locale, defaultLocale, *fallbacks)

    /**
     * Returns the translation using the provided information. If the translation is not found, it will return null.
     *
     * This method is an overload, see the documentation of the base method [tn] for more details.
     *
     * @param key the key to translate
     * @param locale the locale to use for the translation
     * @param fallbacks the fallback keys to use if the translation is not found for the given [locale]
     * @return the translation using the provided information, or null if the translation is not found
     * @throws IllegalArgumentException if the key is not [valid][TranslationKey] or if the [options] are not valid
     */
    fun tn(key: TranslationKey, locale: Locale, vararg fallbacks: String): String? =
        tn(key, emptyMap(), locale, defaultLocale, *fallbacks)

    /**
     * Returns the translation using the provided information. If the translation is not found, it will return null.
     *
     * This method is an overload, see the documentation of the base method [tn] for more details.
     *
     * @param key the key to translate
     * @param options the options to use
     * @param fallbacks the fallback keys to use if the translation is not found for the given [locale]
     * @return the translation using the provided information, or null if the translation is not found
     * @throws IllegalArgumentException if the key is not [valid][TranslationKey] or if the [options] are not valid
     */
    fun tn(key: TranslationKey, options: Map<TranslationOption, Any>, vararg fallbacks: String): String? =
        tn(key, options, currentLocale, defaultLocale, *fallbacks)

    /**
     * Returns the translation using the provided information. If the translation is not found, it will return null.
     *
     * This method is an overload, see the documentation of the base method [tn] for more details.
     *
     * @param key the key to translate
     * @param fallbacks the fallback keys to use if the translation is not found for the given [locale]
     * @return the translation using the provided information, or null if the translation is not found
     */
    fun tn(key: TranslationKey, vararg fallbacks: String): String? =
        tn(key, emptyMap(), currentLocale, defaultLocale, *fallbacks)

    /**
     * Returns the translation using the provided information. If the translation is not found, it will return null.
     *
     * This method is an overload, see the documentation of the base method [tn] for more details.
     *
     * @param key the key to translate
     * @return the translation using the provided information, or null if the translation is not found
     */
    fun tn(key: TranslationKey): String? = tn(key, emptyMap(), currentLocale, defaultLocale)

    fun t(
        key: TranslationKey,
        options: Map<TranslationOption, Any>,
        locale: Locale,
        fallbackLocale: Locale?,
        vararg fallbacks: String,
    ): String

    fun t(key: TranslationKey, options: Map<TranslationOption, Any>, locale: Locale, vararg fallbacks: String): String =
        t(key, options, locale, defaultLocale, *fallbacks)

    fun t(key: TranslationKey, locale: Locale, vararg fallbacks: String): String =
        t(key, emptyMap(), locale, defaultLocale, *fallbacks)

    fun t(key: TranslationKey, options: Map<TranslationOption, Any>, vararg fallbacks: String): String =
        t(key, options, currentLocale, defaultLocale, *fallbacks)

    fun t(key: TranslationKey, vararg fallbacks: String): String =
        t(key, emptyMap(), currentLocale, defaultLocale, *fallbacks)

    fun t(key: TranslationKey): String = t(key, emptyMap(), currentLocale, defaultLocale)

    /**
     * Returns a new [Translator] with the given [key] as root key. The [key] will be prepended to all keys used to
     * translate values.
     *
     * **NOTE**: This method does not check if the key actually exists in the translations.
     *
     * @param key the root key
     * @return a new [Translator] with the given [key] as root key
     * @throws IllegalArgumentException if the key is not [valid][Translator]
     */
    fun section(key: TranslationKey): Translator

    /**
     * Returns a new [Translator] with the given [locale] as current locale. This operation is lightweight (it simply
     * translation map is shared between all instances), meaning that it can be used frequently without any performance
     * impact.
     *
     * **NOTE**: This method does not check if the [locale] is actually supported by this [Translator].
     *
     * @param locale the new current locale
     * @return a new [Translator] with the given [locale] as current locale
     */
    fun withNewCurrentLocale(locale: Locale): Translator

    /**
     * Returns the root [Translator] version of this [Translator] (the same translator with its prefix set to null). If
     * this [Translator] is already a root [Translator], it will return itself.
     *
     * @return the root [Translator] version of this [Translator]
     */
    fun asRoot(): Translator

    /**
     * Returns a map containing all translations for all locales.
     *
     * **NOTE**: If this [Translator] is a section, only the translations for the keys under the section will be
     * returned.
     *
     * @return a map containing all translations for all locales
     */
    fun toMap(): Map<Locale, Map<TranslationKey, String>>

    companion object {

        /**
         * Returns a new [TranslatorBuilder] with the given [defaultLocale]. The [defaultLocale] will be used as the
         * default locale for all [Translator]s built by the returned [TranslatorBuilder].
         *
         * @param defaultLocale the default locale
         * @return a new [TranslatorBuilder] with the given [defaultLocale]
         */
        @JvmStatic
        fun builder(defaultLocale: Locale): TranslatorBuilder = TranslatorBuilder(defaultLocale)

    }

}
