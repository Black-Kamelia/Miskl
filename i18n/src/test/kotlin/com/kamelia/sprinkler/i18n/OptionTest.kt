package com.kamelia.sprinkler.i18n

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OptionTest {

    @Test
    fun `coverage of const val`() {
//        val fields = Options.javaClass.declaredFields
//        fields.forEach {
//            it.get(Options)
//        }
    }

    @Test
    fun `count throws if the formatted value is not a number`() {
        assertThrows<IllegalArgumentException> {
            count(formatted("foo", emptyMap()))
        }
    }



}
