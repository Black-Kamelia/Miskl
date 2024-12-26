package com.kamelia.sprinkler.i18n

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TranslatorBuilderResourceTest {

    @Test
    fun `addResource adds the resource to the translator`() {
        val t = TranslatorBuilder.create().addResource("/builder_test/en.yml").build()
        assertEquals("foo", t.t("test"))
    }

    @Test
    fun `addResource throws an IAE if the resource file does not exist`() {
        val builder = TranslatorBuilder.create()
        assertThrows<IllegalArgumentException> {
            builder.addResource("builder_test/does_not_exist.yml")
        }
    }

    @Test
    fun `addResource throws an IAE if the resource directory does not exist`() {
        val builder = TranslatorBuilder.create()
        assertThrows<IllegalArgumentException> {
            builder.addResource("/builder_test/does_not_exist/")
        }
    }

    @Test
    fun `addResource throws an IAE if the resource directory is a file and the content does not exist as a resource`() {
        val builder = TranslatorBuilder.create()
        assertThrows<IllegalArgumentException> {
            builder.addResource("/builder_test/resource_dir.txt", Translator::class.java)
        }
    }

}
