package com.kamelia.sprinkler.binary.decoder.core

import com.kamelia.sprinkler.binary.decoder.util.assertDoneAndGet
import com.kamelia.sprinkler.binary.decoder.util.get
import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ConstantSizeDecoderTest {

    @Test
    fun `works correctly`() {
        val decoder = ConstantSizeDecoder(2) {
            ((this[0].toInt() shl 8) or (this[1].toInt() and 0xFF)).toShort()
        }

        val value = 12.toShort()
        val data = byteArrayOf(value[1], value[0])
        val result = decoder.decode(data).assertDoneAndGet()
        assertEquals(value, result)
    }

    @Test
    fun `throws on negative size`() {
        assertThrows<IllegalArgumentException> {
            ConstantSizeDecoder(-1) { 0 }
        }
    }

    @Test
    fun `stores read bytes to decode in several steps`() {
        val decoder = ConstantSizeDecoder(2) {
            ((this[0].toInt() shl 8) or (this[1].toInt() and 0xFF)).toShort()
        }

        val value = 12.toShort()
        val data = byteArrayOf(value[1])

        val processing = decoder.decode(data)
        assertInstanceOf(Decoder.State.Processing::class.java, processing)
        val result = decoder.decode(byteArrayOf(value[0])).assertDoneAndGet()

        assertEquals(value, result)
    }

    @Test
    fun `reset works correctly`() {
        val decoder = ConstantSizeDecoder(2) {
            ((this[0].toInt() shl 8) or (this[1].toInt() and 0xFF)).toShort()
        }

        val value = 12.toShort()
        val data = byteArrayOf(value[1], value[0])
        val result = decoder.decode(data).assertDoneAndGet()
        assertEquals(value, result)

        decoder.reset()
        val result2 = decoder.decode(data).assertDoneAndGet()
        assertEquals(value, result2)
    }

    @Test
    fun `successive calls to decode work correctly`() {
        val decoder = ConstantSizeDecoder(2) {
            ((this[0].toInt() shl 8) or (this[1].toInt() and 0xFF)).toShort()
        }

        val value = 12.toShort()
        val data = byteArrayOf(value[1], value[0])
        val result = decoder.decode(data).assertDoneAndGet()
        assertEquals(value, result)

        val value2 = 13.toShort()
        val data2 = byteArrayOf(value2[1], value2[0])
        val result2 = decoder.decode(data2).assertDoneAndGet()
        assertEquals(value2, result2)
    }

    @Test
    fun `size of zero doesn't modify the input`() {
        val value = "a"
        val decoder = ConstantSizeDecoder(0) { value }
        val data = ByteArrayInputStream(byteArrayOf(1, 2, 3))
        val result = decoder.decode(data).assertDoneAndGet()
        assertEquals(value, result)
        assertEquals(1, data.read())
    }

}