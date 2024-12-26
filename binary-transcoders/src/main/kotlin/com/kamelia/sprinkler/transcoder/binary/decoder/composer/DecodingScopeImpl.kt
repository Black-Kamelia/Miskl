package com.kamelia.sprinkler.transcoder.binary.decoder.composer

import com.kamelia.sprinkler.transcoder.binary.decoder.BooleanDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.ByteDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.DoubleDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.FloatDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.IntDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.LongDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.ShortDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.Decoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.DecoderInput
import com.kamelia.sprinkler.transcoder.binary.decoder.toCollection
import com.kamelia.sprinkler.util.unsafeCast
import com.zwendo.restrikt2.annotation.PackagePrivate
import java.nio.ByteOrder
import java.util.stream.Collector

@PackagePrivate
@Suppress("INAPPLICABLE_JVM_NAME")
internal class DecodingScopeImpl<E>(
    private val accumulatorProvider: () -> ElementsAccumulator,
    private val cache: HashMap<Class<*>, Decoder<*>>,
    private val endianness: ByteOrder,
) : DecodingScope<E> {

    var input: DecoderInput = DecoderInput.nullInput()

    private val accumulator: ElementsAccumulator
        get() = accumulatorProvider()

    var currentIndex = 0

    override val self: Decoder<E> = SelfDecoder()

    override fun <T> decode(decoder: Decoder<T>): T =
        if (currentIndex < accumulator.size) { // already decoded
            accumulator[currentIndex++].unsafeCast()
        } else { // decode
            currentIndex++
            when (val value = decoder.decode(input)) {
                is Decoder.State.Done -> value.value.also(accumulator::add)
                is Decoder.State.Error -> throw ErrorStateHolder(value)
                is Decoder.State.Processing -> throw ProcessingMarker
            }
        }

    override fun <T> objectScope(block: () -> T): T = if (currentIndex < accumulator.size) { // already decoded
        accumulator[currentIndex++].unsafeCast()
    } else { // decode
        currentIndex++
        block().also(accumulator::add)
    }

    override fun skip(count: Long) {
        require(count >= 0) { "Count must be positive, but was $count" }
        val toSkip: Long? = if (currentIndex < accumulator.size) {
            accumulator[currentIndex].unsafeCast()
        } else {
            count.also(accumulator::add)
        }
        when (toSkip) {
            null -> currentIndex++ // bytes already skipped
            0L -> accumulator[currentIndex++] = null // method has been called with count = 0
            else -> {
                val leftToSkip = toSkip - input.skip(toSkip)
                if (leftToSkip > 0) {
                    accumulator[currentIndex++] = leftToSkip
                    throw ProcessingMarker
                } else {
                    accumulator[currentIndex++] = null
                }
            }
        }
    }

    override fun errorState(state: Decoder.State.Error): Nothing = throw ErrorStateHolder(state)

    @JvmName("decodeByte")
    override fun byte(): Byte = decodeWithComputed { ByteDecoder() }

    @JvmName("decodeByte")
    override fun short(): Short = decodeWithComputed { ShortDecoder(endianness) }

    @JvmName("decodeByte")
    override fun int(): Int = decodeWithComputed { IntDecoder(endianness) }

    @JvmName("decodeByte")
    override fun long(): Long = decodeWithComputed { LongDecoder(endianness) }

    @JvmName("decodeByte")
    override fun float(): Float = decodeWithComputed { FloatDecoder(endianness) }

    @JvmName("decodeByte")
    override fun double(): Double = decodeWithComputed { DoubleDecoder(endianness) }

    @JvmName("decodeByte")
    override fun boolean(): Boolean = decodeWithComputed { BooleanDecoder() }

    @JvmName("decodeString")
    override fun string(): String = decodeWithComputed {
        throw AssertionError("A String decoder should always be present")
    }

    @JvmName("decodeSelfCollection")
    override fun <R> selfCollection(collector: Collector<E, *, R>): R {
        val decoder = objectScope {
            self.toCollection(collector, computed { IntDecoder(endianness) })
        }
        return decode(decoder)
    }

    private inline fun <reified T> decodeWithComputed(noinline block: () -> Decoder<T>): T {
        val decoder = cache.computeIfAbsent(T::class.java) { block() }
        val result = decode(decoder)
        return try {
            result as T
        } catch (e: ClassCastException) {
            throw IllegalStateException(castErrorMessage(result, T::class.java))
        }
    }

    private inline fun <reified T> computed(noinline block: () -> Decoder<T>): Decoder<T> =
        cache.computeIfAbsent(T::class.java) { block() }.unsafeCast()

    private inner class SelfDecoder : Decoder<E> {

        override fun decode(input: DecoderInput): Decoder.State<E> = if (accumulator.hasRecursionElement()) {
            val element = accumulator.getFromRecursion()
            currentIndex++
            Decoder.State.Done(element.unsafeCast())
        } else {
            throw RecursionMarker
        }

        override fun reset() = Unit

    }

}
