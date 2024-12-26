package com.kamelia.sprinkler.i18n

import java.io.File
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.io.path.Path


class TranslatorBuilderFileTest {

    @Test
    fun `addFile throws an IAE if the extension is invalid`() {
        assertThrows<IllegalArgumentException> {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
                .addFile(File("invalid-extension.txt"))
        }
    }

    @Test
    fun `addPath throws an IAE if the extension is invalid`() {
        assertThrows<IllegalArgumentException> {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
                .addPath(Path("invalid-extension.txt"))
        }
    }

    @Test
    fun `addURL throws an IAE if the extension is invalid`() {
        assertThrows<IllegalArgumentException> {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
                .addURL(absoluteResource(ROOT, "invalid-extension.txt").toUri().toURL())
        }
    }

    @Test
    fun `addURI throws an IAE if the extension is invalid`() {
        assertThrows<IllegalArgumentException> {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
                .addURI(absoluteResource(ROOT, "invalid-extension.txt").toUri())
        }
    }

    @Test
    fun `addFile does not throw with json extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addFile(absoluteResource(ROOT, "fr.json").toFile())
        }
    }

    @Test
    fun `addPath does not throw with json extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addPath(absoluteResource(ROOT, "fr.json"))
        }
    }

    @Test
    fun `addURL does not throw with json extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addURL(absoluteResource(ROOT, "fr.json").toUri().toURL())
        }
    }

    @Test
    fun `addURI does not throw with json extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addURI(absoluteResource(ROOT, "fr.json").toUri())
        }
    }

    @Test
    fun `addFile does not throw with yaml extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addFile(absoluteResource(ROOT, "en.yaml").toFile())
        }
    }

    @Test
    fun `addPath does not throw with yaml extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addPath(absoluteResource(ROOT, "en.yaml"))
        }
    }

    @Test
    fun `addURL does not throw with yaml extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addURL(absoluteResource(ROOT, "en.yaml").toUri().toURL())
        }
    }

    @Test
    fun `addURI does not throw with yaml extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addURI(absoluteResource(ROOT, "en.yaml").toUri())
        }
    }

    @Test
    fun `addFile does not throw with yml extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addFile(absoluteResource(ROOT, "en.yml").toFile())
        }
    }

    @Test
    fun `addPath does not throw with yml extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addPath(absoluteResource(ROOT, "en.yml"))
        }
    }

    @Test
    fun `addURL does not throw with yml extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addURL(absoluteResource(ROOT, "en.yml").toUri().toURL())
        }
    }

    @Test
    fun `addURI does not throw with yml extension`() {
        assertDoesNotThrow {
            TranslatorBuilder.create(defaultLocale = Locale.ENGLISH).addURI(absoluteResource(ROOT, "en.yml").toUri())
        }
    }

    @Test
    fun `addPath throws an ISE on build call if the file name is not a valid locale`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addPath(absoluteResource(ROOT, "invalid-locale&.json"))
        }
    }

    @Test
    fun `addFile throws an ISE on build call if the file name is not a valid locale`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addFile(absoluteResource(ROOT, "invalid-locale&.json").toFile())
        }
    }

    @Test
    fun `addPath throws an ISE on build if the parser returns a map containing invalid key`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addPath(absoluteResource(ROOT, INVALID_CONTENT))
        }
    }

    @Test
    fun `addFile throws an ISE on build if the parser returns a map containing invalid key`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addFile(absoluteResource(ROOT, INVALID_CONTENT).toFile())
        }
    }

    @Test
    fun `addPath throws an ISE on build if a value is null`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addPath(absoluteResource(ROOT, NULL_VALUE))
        }
    }

    @Test
    fun `addFile throws an ISE on build if a value is null`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addFile(absoluteResource(ROOT, NULL_VALUE).toFile())
        }
    }

    @Test
    fun `addPath throws an ISE on build if the map contains a map containing an invalid key`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addPath(absoluteResource(ROOT, INVALID_NESTED_KEY))
        }
    }

    @Test
    fun `addFile throws an ISE on build if the map contains a map containing an invalid key`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addFile(absoluteResource(ROOT, "fr_FR.json").toFile())
        }
    }

    @Test
    fun `addPath throws an ISE on build if the map contains a map containing a null value`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addPath(absoluteResource(ROOT, NESTED_NULL_VALUE))
        }
    }

    @Test
    fun `addFile throws an ISE on build if the map contains a map containing a null value`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalStateException> {
            builder.addFile(absoluteResource(ROOT, NESTED_NULL_VALUE).toFile())
        }
    }

    @Test
    fun `addPath works with list`() {
        val translator = TranslatorBuilder.create(defaultLocale = Locale.JAPANESE)
            .addPath(absoluteResource(ROOT, LIST))
            .build()
        assertEquals("tesuto", translator.t("test.0"))
    }

    @Test
    fun `addFile works with list`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.JAPANESE)
            .addFile(absoluteResource(ROOT, LIST).toFile())
        val translator = builder.build()
        assertEquals("tesuto", translator.t("test.0"))
    }

    @Test
    fun `addPath works with nested value`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addPath(absoluteResource(ROOT, NESTED_VALUE))
        val translator = builder.build()
        assertEquals("test", translator.t("test.test"))
    }

    @Test
    fun `addFile works with map`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addFile(absoluteResource(ROOT, NESTED_VALUE).toFile())
        val translator = builder.build()
        assertEquals("test", translator.t("test.test"))
    }

    @Test
    fun `addFile works with folder`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addFile(absoluteResource(ROOT, GROUP_FOLDER).toFile())
        val translator = builder.build()
        assertEquals("this is a test", translator.t("test.test"))
    }

    @Test
    fun `addPath works with folder`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addPath(absoluteResource(ROOT, GROUP_FOLDER))
        val translator = builder.build()
        assertEquals("this is a test", translator.t("test.test"))
    }

    @Test
    fun `addFile throws an ISE if the file does not exist`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalArgumentException> {
            builder.addFile(File("does-not-exist.json"))
        }
    }

    @Test
    fun `addPath throws an ISE if the file does not exist`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalArgumentException> {
            builder.addPath(Path("does-not-exist.json"))
        }
    }

    @Test
    fun `throws an exception if the content of the file of a json file is not a valid json`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalArgumentException> {
            builder.addPath(absoluteResource(ROOT, INVALID_JSON))
        }
    }

    @Test
    fun `throws an exception if the content of the file of a yaml file is not a valid yaml`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
        assertThrows<IllegalArgumentException> {
            builder.addPath(absoluteResource(ROOT, INVALID_YAML))
        }
    }

    @Test
    fun `addFile with the same file more than once throws an ISE`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addFile(absoluteResource(ROOT, LIST).toFile())
        assertThrows<IllegalStateException> {
            builder.addFile(absoluteResource(ROOT, LIST).toFile())
        }
    }

    @Test
    fun `addURL with the same file more than once throws an ISE`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addPath(absoluteResource(ROOT, LIST))
        assertThrows<IllegalStateException> {
            builder.addURL(absoluteResource(ROOT, LIST).toUri().toURL())
        }
    }

    @Test
    fun `addURI with the same file more than once throws an ISE`() {
        val builder = TranslatorBuilder.create(defaultLocale = Locale.ENGLISH)
            .addPath(absoluteResource(ROOT, LIST))
        assertThrows<IllegalStateException> {
            builder.addURI(absoluteResource(ROOT, LIST).toUri())
        }
    }

    private companion object {

        const val ROOT = "builder_test"

        const val GROUP_FOLDER = "group"

        const val INVALID_CONTENT = "fr_FR.json"

        const val NULL_VALUE = "fr_FR.yml"

        const val INVALID_NESTED_KEY = "fr_FR.json"

        const val NESTED_NULL_VALUE = "nlv.json"

        const val INVALID_JSON = "invalid.json"

        const val INVALID_YAML = "invalid.yaml"

        const val LIST = "ja.yml"

        const val NESTED_VALUE = "en.yaml"

    }

}
