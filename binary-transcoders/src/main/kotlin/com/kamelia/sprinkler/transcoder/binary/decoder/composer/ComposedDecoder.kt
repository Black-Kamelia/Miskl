@file:JvmName("ComposedDecoderFactory")

package com.kamelia.sprinkler.transcoder.binary.decoder.composer

import com.kamelia.sprinkler.transcoder.binary.decoder.UTF8StringDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.Decoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.DecoderInput
import java.nio.ByteOrder

/**
 * Creates a new encoder of type [T] using the given function [block]. The [block] parameter is a lambda accepting an
 * object of type [T] and a [DecodingScope]. The given [DecodingScope] will have the following properties:
 *
 * - All primitive objects will be decoded with the default decoders present in the `BaseDecoders` file, and with the
 * given [endianness].
 * - For [String] objects decoding, the [stringDecoder] parameter will be used.
 * - Nullable objects decoding will be decoded assuming that the object is prefixed with a [Boolean] (represented as a
 * single byte) that indicates if the object is present or not (`true` if present, `false` if not).
 * - Each created decoder will be cached and reused for the same type (except for decoders composed with the
 * [self][DecodingScope.self] property).
 * - All collection decoding will be decoded assuming that the collection is prefixed with its size represented by an
 * [Int].
 * - All nullable collections decoding will be decoded assuming that the collection is prefixed with a [Boolean] that
 * indicates if the collection is present or not (`true` if present, `false` if not). Then, if the collection is
 * present, the size of the collection will be decoded as an [Int]. Finally, if the collection is present and its size
 * is greater than 0, the elements of the collection will be decoded.
 *
 * **NOTE**: The [DecodingScope] used in the lambda [block] is not designed to be used outside the lambda. Any use of
 * the scope outside the lambda may lead to unexpected results and can change the behaviour of the scope decoding
 * process.
 *
 * @param endianness the endianness of the decoder (defaults to [ByteOrder.BIG_ENDIAN])
 * @param stringDecoder the decoder to use for [String] objects (defaults to the default [UTF8StringDecoder])
 * @param block the block that will decode the object
 * @return the created decoder of type [T]
 * @see DecodingScope
 */
@JvmOverloads
fun <T> composedDecoder(
    endianness: ByteOrder = ByteOrder.BIG_ENDIAN,
    stringDecoder: Decoder<String> = UTF8StringDecoder(),
    block: DecodingScope<T>.() -> T,
): Decoder<T> {
    val cache = HashMap<Class<*>, Decoder<*>>().apply {
        put(String::class.java, stringDecoder)
    }
    return ComposedDecoderImpl(endianness, cache, block)
}

private class ComposedDecoderImpl<E>(
    endianness: ByteOrder,
    private val cache: HashMap<Class<*>, Decoder<*>>,
    private val block: DecodingScope<E>.() -> E,
) : Decoder<E> {

    private var elements = ElementsAccumulator()
    private val scope = DecodingScopeImpl<E>(::elements, cache, endianness)

    override fun decode(input: DecoderInput): Decoder.State<E> {
        scope.input = input
        while (true) {
            scope.currentIndex = 0
            try {
                val result = scope.block()
                if (elements.isLastLayer) { // there is no recursion layer, we are done
                    elements = ElementsAccumulator()
                    return Decoder.State.Done(result)
                }

                elements.popRecursion() // else, we pop the recursion layer
                elements.addToRecursion(result) // and add the result to the previous layer
            } catch (_: ProcessingMarker) { // bytes are missing
                return Decoder.State.Processing
            } catch (_: RecursionMarker) { // recursion
                elements.recurse()
            } catch (e: ErrorStateHolder) { // an error state should be returned
                return e.errorState
            }
        }
    }

    override fun reset() {
        elements = ElementsAccumulator()
        cache.values.forEach(Decoder<*>::reset)
        scope.reset()
    }

}
