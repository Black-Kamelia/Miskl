package com.kamelia.sprinkler.i18n

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PluralTest {

    @Test
    fun `the englishMapper mapCardinal returns the values of the English language`() {
        val mapper = Plural.englishMapper()
        assertEquals(Plural.ONE, mapper.mapCardinal(1))
        repeat(10_000) { count ->
            if (count == 1) return@repeat
            assertEquals(Plural.OTHER, mapper.mapCardinal(count))
        }
    }


    @Test
    fun `the englishMapper mapCardinal throws an IAE if the count is negative (long)`() {
        val mapper = Plural.englishMapper()
        assertThrows<IllegalArgumentException> { mapper.mapCardinal(-1) }
    }

    @Test
    fun `the englishMapper mapCardinal throws an IAE if the count is negative (double)`() {
        val mapper = Plural.englishMapper()
        assertThrows<IllegalArgumentException> { mapper.mapCardinal(-1.0) }
    }

    @Test
    fun `the englishMapper mapCardinal returns the values of the English language (double)`() {
        val mapper = Plural.englishMapper()
        repeat(10_000) { count ->
            val cnt = count.toDouble()
            if (cnt == .0) return@repeat
            val plural = mapper.mapCardinal(cnt)
            assertEquals(Plural.OTHER, plural)
        }
    }

    @Test
    fun `the englishMapper mapOrdinal returns the values of the English language (long)`() {
        val mapper = Plural.englishMapper()
        repeat(10_000) { count ->
            if (count == 0) return@repeat
            val plural = mapper.mapOrdinal(count)
            when (count % 10) {
                1 -> assertEquals(Plural.ONE, plural)
                2 -> assertEquals(Plural.TWO, plural)
                3 -> assertEquals(Plural.FEW, plural)
                else -> assertEquals(Plural.OTHER, plural)
            }
        }
    }

    @Test
    fun `the englishMapper mapOrdinal throws an IAE if the count is negative or zero (long)`() {
        val mapper = Plural.englishMapper()
        assertThrows<IllegalArgumentException> { mapper.mapOrdinal(-1) }
        assertThrows<IllegalArgumentException> { mapper.mapOrdinal(0) }
    }

    @Test
    fun `the englishMapper mapOrdinal throws an UnsupportedOperationException if the count is a double`() {
        val mapper = Plural.englishMapper()
        assertThrows<UnsupportedOperationException> { mapper.mapOrdinal(1.0) }
    }

    @Test
    fun `the englishMapper toString returns the name of the class`() {
        val mapper = Plural.englishMapper()
        assertEquals("Plural.englishMapper()", mapper.toString())
    }

}
