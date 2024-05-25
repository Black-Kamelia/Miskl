package com.kamelia.sprinkler.i18n

import java.util.Locale
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TranslatorBuilderTest {

    @Test
    fun `defaultLocale is the locale specifier in the constructor`() {
        val locale = Locale.FRANCE
        val translator = TranslatorBuilder.create(defaultLocale = locale).build()
        Assertions.assertEquals(locale, translator.defaultLocale)
    }

    @Test
    fun `currentLocale is set to the default locale by default`() {
        val locale = Locale.FRANCE
        val translator = TranslatorBuilder.create(defaultLocale = locale).build()
        Assertions.assertEquals(locale, translator.currentLocale)
    }

    @Test
    fun `addMap adds the map to the translator`() {
        val key = "test"
        val value = "this is a test"
        val locale = Locale.FRANCE

        val translator = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMap(locale, mapOf(key to value))
            .build()
        Assertions.assertEquals(value, translator.t(key, locale))
    }

    @Test
    fun `addMaps adds the maps to the translator`() {
        val key = "test"
        val value = "this is a test"
        val locale = Locale.FRANCE

        val translator = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMaps(mapOf(locale to mapOf(key to value)))
            .build()
        Assertions.assertEquals(value, translator.t(key, locale))
    }

    @Test
    fun `FAIL duplicate policy throws if a key is duplicated`() {
        val builder = TranslatorBuilder.create(
            duplicatedKeyResolution = TranslatorBuilder.DuplicatedKeyResolution.FAIL,
            defaultLocale = Locale.ENGLISH
        ).addMap(Locale.ENGLISH, mapOf("test" to "test"))

        assertThrows<IllegalStateException> {
            builder.addMap(Locale.ENGLISH, mapOf("test" to "test"))
        }
    }

    @Test
    fun `KEEP_FIRST duplicate policy keeps the first value`() {
        val builder =
            TranslatorBuilder.create(
                duplicatedKeyResolution = TranslatorBuilder.DuplicatedKeyResolution.KEEP_FIRST,
                defaultLocale = Locale.ENGLISH
            )
                .addMap(Locale.ENGLISH, mapOf("test" to "test"))
                .addMap(Locale.ENGLISH, mapOf("test" to "test2"))
        val translator = builder.build()
        Assertions.assertEquals("test", translator.t("test"))
    }

    @Test
    fun `KEEP_LAST duplicate policy keeps the last value`() {
        val builder =
            TranslatorBuilder.create(
                duplicatedKeyResolution = TranslatorBuilder.DuplicatedKeyResolution.KEEP_LAST,
                defaultLocale = Locale.ENGLISH
            )
                .addMap(Locale.ENGLISH, mapOf("test" to "test"))
                .addMap(Locale.ENGLISH, mapOf("test" to "test2"))
        val translator = builder.build()
        Assertions.assertEquals("test2", translator.t("test"))
    }

    @Test
    fun `build throws an IAE if a value contains a variable named 'options'`() {
        assertThrows<IllegalArgumentException> {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
                .addMap(Locale.ENGLISH, mapOf("test" to "test {{options}}"))
                .build()
        }
    }

    @Test
    fun `build throws an IAE if a value contains a format that is not present in the configuration`() {
        assertThrows<IllegalArgumentException> {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
                .addMap(Locale.ENGLISH, mapOf("test" to "test {{name, unknown}}"))
                .build()
        }
    }

    @Test
    fun `build does not throw if a value contains a format that is present in the configuration`() {
        TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMap(Locale.ENGLISH, mapOf("test" to "test {{name, date}}"))
            .build()
    }

    @Test
    fun `build throws an IAE if a value contains a variable that is does not respect the format (illegal name)`() {
        assertThrows<IllegalArgumentException> {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
                .addMap(Locale.ENGLISH, mapOf("test" to "test {{name#}}"))
                .build()
        }
    }

    @Test
    fun `build throws an IAE if a value contains a variable that is does not respect the format (illegal format)`() {
        val configuration = TranslatorConfiguration.builder()
            .addFormatter("12#", VariableFormatter.date())
            .build()
        assertThrows<IllegalArgumentException> {
            TranslatorBuilder.create(configuration, defaultLocale = Locale.ENGLISH)
                .addMap(Locale.ENGLISH, mapOf("test" to "test {{name, 12#}}"))
                .build()
        }
    }

    @Test
    fun `build throws an IAE if a value contains a variable that is does not respect the format (format with parenthesis and no param)`() {
        assertThrows<IllegalArgumentException> {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
                .addMap(Locale.ENGLISH, mapOf("test" to "test {{name, date()}}"))
                .build()
        }
    }

}
