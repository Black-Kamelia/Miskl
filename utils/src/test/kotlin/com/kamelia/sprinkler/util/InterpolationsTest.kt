package com.kamelia.sprinkler.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class InterpolationsTest {

    @Test
    fun `interpolate(List) replaces the found variables`() {
        val str = "Hello {{0}}, you are {{1}}"
        assertEquals("Hello John, you are 25", str.interpolate(listOf("John", 25, "foo")))
    }

    @Test
    fun `interpolate(List) throws an exception if the index is not an integer`() {
        val str = "Hello {{0}}, you are {{a}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolateIdx(arrayOf("John", 25, "foo", "bar"))
        }
    }

    @Test
    fun `interpolate(List) throws an exception if the index is negative`() {
        val str = "Hello {{0}}, you are {{-1}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolateIdx(arrayOf("John", 25, "foo", "bar"))
        }
    }

    @Test
    fun `interpolate(List) throws an exception if the index is greater than the array size`() {
        val str = "Hello {{0}}, you are {{3}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolateIdx(arrayOf("John", 25))
        }
    }

    @Test
    fun `interpolate(List) throws if the variable identifier is invalid`() {
        val str = "Hello {{0}}, you are {{§}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolateIdx(arrayOf("John", 25, "foo", "bar"))
        }
    }

    @Test
    fun `interpolate(List) does not replace ignored variables`() {
        val str = "Hello {{0}}, you are \\{{1}} years old"
        assertEquals("Hello John, you are \\{{1}} years old", str.interpolate(listOf("John", "25")))
    }

    @Test
    fun `interpolate(vararg) replaces the found variables`() {
        val str = "Hello {{0}}, you are {{1}} years old"
        assertEquals("Hello John, you are 25 years old", str.interpolateIdx("John", 25, "foo"))
    }

    @Test
    fun `interpolate(vararg) throws an exception if the index is not an integer`() {
        val str = "Hello {{0}}, you are {{a}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolateIdx("John", 25, "foo", "bar")
        }
    }

    @Test
    fun `interpolate(vararg) throws an exception if the index is negative`() {
        val str = "Hello {{0}}, you are {{-1}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolateIdx("John", 25, "foo", "bar")
        }
    }

    @Test
    fun `interpolate(vararg) throws an exception if the index is greater than the array size`() {
        val str = "Hello {{0}}, you are {{3}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolateIdx("John", 25)
        }
    }

    @Test
    fun `interpolate(vararg) throws if the variable identifier is invalid`() {
        val str = "Hello {{0}}, you are {{§}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolateIdx("John", 25, "foo", "bar")
        }
    }

    @Test
    fun `interpolate(vararg) does not replace ignored variables`() {
        val str = "Hello \\{{0}}"
        assertEquals("Hello \\{{0}}", str.interpolateIdx("John"))
    }

    @Test
    fun `interpolate(Map) replaces the found variables`() {
        val str = "Hello {{name}}, you are {{age}} years old"
        assertEquals(
            "Hello John, you are 25 years old",
            str.interpolate(mapOf("name" to "John", "age" to 25, "foo" to "bar"))
        )
    }

    @Test
    fun `interpolate(Map) throws an exception if the key is not found`() {
        val str = "Hello {{name}}, you are {{age}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolate(mapOf("name" to "John"))
        }
    }

    @Test
    fun `interpolate(Map) throws if the variable identifier is invalid`() {
        val str = "Hello {{name}}, you are {{§}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolate(mapOf("name" to "John", "age" to 25, "foo" to "bar"))
        }
    }

    @Test
    fun `interpolate(Map) does not replace ignored variables`() {
        val str = "Hello {{name}}, you are \\{{age}} years old"
        assertEquals(
            "Hello John, you are \\{{age}} years old",
            str.interpolate(mapOf("name" to "John", "age" to 25))
        )
    }

    @Test
    fun `interpolate(Map) accepts '-'`() {
        val str = "Hello {{name-}}, you are {{age}} years old"
        assertEquals(
            "Hello John, you are 25 years old",
            str.interpolate(mapOf("name-" to "John", "age" to 25, "foo" to "bar"))
        )
    }

    @Test
    fun `interpolate(Map) accepts '_'`() {
        val str = "Hello {{name_}}, you are {{age}} years old"
        assertEquals(
            "Hello John, you are 25 years old",
            str.interpolate(mapOf("name_" to "John", "age" to 25, "foo" to "bar"))
        )
    }

    @Test
    fun `interpolate(Pairs) replaces the found variables`() {
        val str = "Hello {{name}}, you are {{age}} years old"
        assertEquals(
            "Hello John, you are 25 years old",
            str.interpolate("name" to "John", "age" to 25, "foo" to "bar")
        )
    }

    @Test
    fun `interpolate(Pairs) throws an exception if the key is not found`() {
        val str = "Hello {{name}}, you are {{age}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolate("name" to "John")
        }
    }

    @Test
    fun `interpolate(Pairs) throws if the variable identifier is invalid`() {
        val str = "Hello {{name}}, you are {{§}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolate("name" to "John", "age" to 25, "foo" to "bar")
        }
    }

    @Test
    fun `interpolate(Pairs) does not replace ignored variables`() {
        val str = "Hello {{name}}, you are \\{{age}}"
        assertEquals("Hello John, you are \\{{age}}", str.interpolate("name" to "John", "age" to 25))
    }

    @Test
    fun `interpolate(Pairs) accepts '-'`() {
        val str = "Hello {{name-}}, you are {{age}} years old"
        assertEquals(
            "Hello John, you are 25 years old",
            str.interpolate("name-" to "John", "age" to 25, "foo" to "bar")
        )
    }

    @Test
    fun `interpolate(Pairs) accepts '_'`() {
        val str = "Hello {{name_}}, you are {{age}} years old"
        assertEquals(
            "Hello John, you are 25 years old",
            str.interpolate("name_" to "John", "age" to 25, "foo" to "bar")
        )
    }

    @Test
    fun `interpolateIdxD uses the provided delimiter`() {
        val str = "Hello [0], you are [1] years old"
        val delimiter = VariableDelimiter.create("[", "]")
        assertEquals("Hello John, you are 25 years old", str.interpolateIdxD(delimiter, "John", 25, "foo"))
    }

    @Test
    fun `interpolateIdxD throws an exception if the index is not an integer`() {
        val str = "Hello [0], you are [a] years old"
        val delimiter = VariableDelimiter.create("[", "]")
        assertThrows<IllegalArgumentException> {
            str.interpolateIdxD(delimiter, "John", 25, "foo", "bar")
        }
    }

    @Test
    fun `interpolateIdxD throws an exception if the index is negative`() {
        val str = "Hello [0], you are [-1] years old"
        val delimiter = VariableDelimiter.create("[", "]")
        assertThrows<IllegalArgumentException> {
            str.interpolateIdxD(delimiter, "John", 25, "foo", "bar")
        }
    }

    @Test
    fun `interpolateIdxD throws an exception if the index is greater than the array size`() {
        val str = "Hello [0], you are [3] years old"
        val delimiter = VariableDelimiter.create("[", "]")
        assertThrows<IllegalArgumentException> {
            str.interpolateIdxD(delimiter, "John", 25)
        }
    }

    @Test
    fun `interpolateIdxD throws if the variable identifier is not an integer`() {
        val str = "Hello [0], you are [§] years old"
        val delimiter = VariableDelimiter.create("[", "]")
        assertThrows<IllegalArgumentException> {
            str.interpolateIdxD(delimiter, "John", 25, "foo", "bar")
        }
    }

    @Test
    fun `interpolateIdxD does not replace ignored variables`() {
        val str = "Hello [0], you are \\[1] years old"
        val delimiter = VariableDelimiter.create("[", "]")
        assertEquals("Hello John, you are \\[1] years old", str.interpolateIdxD(delimiter, "John", "25"))
    }

    @Test
    fun `interpolateItD uses the provided delimiter`() {
        val str = "Hello [], you are [] years old"
        val delimiter = VariableDelimiter.create("[", "]")
        assertEquals("Hello John, you are 25 years old", str.interpolateItD(delimiter, "John", 25))
    }

    @Test
    fun `interpolateItD throws an exception if there is not any value left`() {
        val str = "Hello [], you are [] years old"
        val delimiter = VariableDelimiter.create("[", "]")
        assertThrows<IllegalArgumentException> {
            str.interpolateItD(delimiter)
        }
    }

    @Test
    fun `interpolateIt uses the default delimiter`() {
        val str = "Hello {{}}, you are {{}} years old"
        assertEquals("Hello John, you are 25 years old", str.interpolateIt("John", 25))
    }

    @Test
    fun `interpolateIt throws an exception if there is not any value left`() {
        val str = "Hello {{}}, you are {{}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolateIt()
        }
    }

    @Test
    fun `interpolate(Iterator) uses the default delimiter`() {
        val str = "Hello {{}}, you are {{}} years old"
        assertEquals("Hello John, you are 25 years old", str.interpolate(listOf("John", 25) as Iterable<Any>))
    }

    @Test
    fun `interpolate(Iterator) throws an exception if there is not any value left`() {
        val str = "Hello {{0}}, you are {{1}} years old"
        assertThrows<IllegalArgumentException> {
            str.interpolate(listOf<String>() as Iterable<Any>)
        }
    }

    // regex tests

    @Test
    fun `non closed delimiters are not interpolated`() {
        val str = "Hello {{nope this should not be interpreted"
        assertDoesNotThrow {
            str.interpolate(null, resolver = throwResolver)
        }
    }

    @Test
    fun `escaped delimiters are not interpreted`() {
        val str = "Hello \\{{this should not be interpreted}}"
        assertDoesNotThrow {
            str.interpolate(null, resolver = throwResolver)
        }
    }

    @Test
    fun `escaped closing delimiters are not interpreted`() {
        val str = "Hello {{this should not be interpreted\\}}"
        assertDoesNotThrow {
            str.interpolate(null, resolver = throwResolver)
        }
    }

    @Test
    fun `escaped closing delimiters are part of the value`() {
        val str = "Hello {{this should be interpreted\\}}}"
        val result = str.interpolate(null) { name, _ ->
            assertEquals("this should be interpreted\\}", name)
            "ok"
        }
        assertEquals("Hello ok", result)
    }

    @Test
    fun `one char delimiters are interpreted`() {
        val str = "Hello {this should be interpreted}"
        val result = str.interpolate(null, simpleDelimiter) { name, _ ->
            assertEquals("this should be interpreted", name)
            "ok"
        }
        assertEquals("Hello ok", result)
    }

    @Test
    fun `one char delimiters are not interpreted if escaped`() {
        val str = "Hello \\{this should not be interpreted}"
        assertDoesNotThrow {
            str.interpolate(null, simpleDelimiter, throwResolver)
        }
    }

    @Test
    fun `one char delimiters are not interpreted if escaped (2)`() {
        val str = "Hello {this should not be interpreted\\}"
        assertDoesNotThrow {
            str.interpolate(null, simpleDelimiter, throwResolver)
        }
    }

    @Test
    fun `escaped one char delimiters are part of the value`() {
        val str = "Hello {this should be interpreted\\}}"
        val result = str.interpolate(null, simpleDelimiter) { name, _ ->
            assertEquals("this should be interpreted\\}", name)
            "ok"
        }
        assertEquals("Hello ok", result)
    }

    private val throwResolver: VariableResolver<Any?> = VariableResolver { _, _ -> Assertions.fail() }

    private val simpleDelimiter = VariableDelimiter.create("{", "}")

}
