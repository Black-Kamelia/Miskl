package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.util.unsafeCast
import java.util.Locale
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class TranslatorBuilderMapTest {

    @Test
    fun `addMap throws an ISE on build if the map contains invalid key`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMap(Locale.FRANCE, mapOf("invalid#" to 5))
        }
    }

    @Test
    fun `addMaps throws an ISE on build if one of the maps contains invalid key`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMaps(mapOf(Locale.FRANCE to mapOf("invalid#" to 5)))
        }
    }

    @Test
    fun `addMap throws an ISE on build if a key is null`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMap(Locale.FRANCE, mapOf(null to 5).unsafeCast())
        }
    }

    @Test
    fun `addMaps throws an ISE on build if a key is null`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMaps(mapOf(Locale.FRANCE to mapOf(null to 5).unsafeCast()))
        }
    }

    @Test
    fun `addMap throws an ISE on build if a value is null`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMap(Locale.FRANCE, mapOf("test" to null).unsafeCast())
        }
    }

    @Test
    fun `addMaps throws an ISE on build if a value is null`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMaps(mapOf(Locale.FRANCE to mapOf("test" to null).unsafeCast()))
        }
    }

    @Test
    fun `addMap throws an ISE on build if the map contains a map containing an invalid key`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMap(Locale.FRANCE, mapOf("test" to mapOf("invalid#" to 5)))
        }
    }

    @Test
    fun `addMaps throws an ISE on build if the map contains a map containing an invalid key`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMaps(mapOf(Locale.FRANCE to mapOf("test" to mapOf("invalid#" to 5))))
        }
    }

    @Test
    fun `addMap throws an ISE on build if the map contains a map containing a null key`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMap(Locale.FRANCE, mapOf("test" to mapOf(null to 5)).unsafeCast())
        }
    }

    @Test
    fun `addMaps throws an ISE on build if the map contains a map containing a null key`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMaps(mapOf(Locale.FRANCE to mapOf("test" to mapOf(null to 5)).unsafeCast()))
        }
    }

    @Test
    fun `addMap throws an ISE on build if the map contains a map containing a null value`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMap(Locale.FRANCE, mapOf("test" to mapOf("test" to null)))
        }
    }

    @Test
    fun `addMaps throws an ISE on build if the map contains a map containing a null value`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMaps(mapOf(Locale.FRANCE to mapOf("test" to mapOf("test" to null))))
        }
    }

    @Test
    fun `addMap throws an ISE on build if the map contains a list containing an invalid value`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMap(Locale.FRANCE, mapOf("test" to listOf(Any())))
        }
    }

    @Test
    fun `addMaps throws an ISE on build if the map contains a list containing an invalid value`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMaps(mapOf(Locale.FRANCE to mapOf("test" to listOf(Any()))))
        }
    }

    @Test
    fun `addMap throws an ISE on build if the map contains a map containing a key that is not a string`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMap(Locale.FRANCE, mapOf("test" to mapOf(5 to "test")))
        }
    }

    @Test
    fun `addMaps throws an ISE on build if the map contains a map containing a key that is not a string`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addMaps(mapOf(Locale.FRANCE to mapOf("test" to mapOf(5 to "test"))))
        }
    }

    @Test
    fun `addMap works with list`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMap(Locale.ENGLISH, mapOf("test" to listOf("test")))
        val translator = builder.build()
        Assertions.assertEquals("test", translator.t("test.0"))
    }

    @Test
    fun `addMaps works with list`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMaps(mapOf(Locale.ENGLISH to mapOf("test" to listOf("test"))))
        val translator = builder.build()
        Assertions.assertEquals("test", translator.t("test.0"))
    }

    @Test
    fun `addMap works with map`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMap(Locale.ENGLISH, mapOf("test" to mapOf("test" to "test")))
        val translator = builder.build()
        Assertions.assertEquals("test", translator.t("test.test"))
    }

    @Test
    fun `addMaps works with map`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMaps(mapOf(Locale.ENGLISH to mapOf("test" to mapOf("test" to "test"))))
        val translator = builder.build()
        Assertions.assertEquals("test", translator.t("test.test"))
    }

    @Test
    fun `addMap does not throw for valid types`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMap(
                Locale.ENGLISH, mapOf(
                    "test" to "test",
                    "test2" to 0.toByte(),
                    "test3" to 0.toShort(),
                    "test4" to 0,
                    "test5" to 0L,
                    "test6" to 0f,
                    "test7" to .0,
                    "test8" to true,
                    "test9" to listOf("test"),
                    "test10" to mapOf("test" to "test")
                )
            )
        assertDoesNotThrow {
            builder.build()
        }
    }

    @Test
    fun `addMaps does not throw for valid types`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMaps(
                mapOf(
                    Locale.ENGLISH to mapOf(
                        "test" to "test",
                        "test2" to 0.toByte(),
                        "test3" to 0.toShort(),
                        "test4" to 0,
                        "test5" to 0L,
                        "test6" to 0f,
                        "test7" to .0,
                        "test8" to true,
                        "test9" to listOf("test"),
                        "test10" to mapOf("test" to "test")
                    )
                )
            )
        assertDoesNotThrow {
            builder.build()
        }
    }

    @Test
    fun `addMap with dotted key correctly nest the value in the translator`() {
        val translator = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMap(Locale.FRANCE, mapOf("test.test" to "test"))
            .build()
        Assertions.assertEquals("test", translator.t("test.test", Locale.FRANCE))
    }

    @Test
    fun `addMaps with dotted key correctly nest the value in the translator`() {
        val translator = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addMaps(mapOf(Locale.FRANCE to mapOf("test.test" to "test")))
            .build()
        Assertions.assertEquals("test", translator.t("test.test", Locale.FRANCE))
    }

}
