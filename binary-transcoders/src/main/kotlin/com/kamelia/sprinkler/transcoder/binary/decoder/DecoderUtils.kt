@file:JvmName("DecoderUtils")

package com.kamelia.sprinkler.transcoder.binary.decoder

import com.kamelia.sprinkler.transcoder.binary.decoder.core.ConstantArityReductionDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.Decoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.DecoderInput
import com.kamelia.sprinkler.transcoder.binary.decoder.core.MarkerEndedReductionDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.PrefixedArityReductionDecoder
import com.kamelia.sprinkler.util.ExtendedCollectors
import com.kamelia.sprinkler.util.unsafeCast
import com.zwendo.restrikt2.annotation.HideFromJava
import java.util.stream.Collector
import java.util.stream.Collectors

/**
 * Creates a decoder that maps the result of `this` decoder to another decoder. The created decoder will delegate to
 * `this` decoder and then apply the given [mapper] to the result to produce the next decoder.
 *
 * &nbsp;
 *
 * Here is an example of a decoder that decodes different types of numbers depending on the first byte:
 *
 * ```
 * val numberDecoder = ByteDecoder().mapTo {
 *     when (it.toInt()) {
 *       0 -> ByteDecoder()
 *       1 -> ShortDecoder()
 *       2 -> IntDecoder()
 *       else -> LongDecoder()
 *     }
 * }
 *
 * val result = numberDecoder.decode(byteArrayOf(1, 0, 7)).get()
 * // will return 7, 1st byte = 1 => next decoder will read a short which is 7
 * ```
 *
 * @receiver the decoder producing the result to be mapped
 * @param mapper the mapping function
 * @return a decoder producing a result of type [R]
 * @param T the type of the result of the decoder
 * @param R the type of the result of the returned decoder
 */
fun <T, R> Decoder<T>.mapTo(mapper: (T) -> Decoder<R>): Decoder<R> = object : Decoder<R> {
    private var nextReader: Decoder<R>? = null

    override fun decode(input: DecoderInput): Decoder.State<R> = if (nextReader == null) {
        this@mapTo.decode(input).mapState {
            nextReader = mapper(it)
            decodeNext(input)
        }
    } else {
        decodeNext(input)
    }

    private fun decodeNext(input: DecoderInput) = nextReader!!.decode(input).ifDone { nextReader = null }

    override fun reset() {
        this@mapTo.reset()
        nextReader = null
    }

}

/**
 * Creates a new decoder by applying the given [mapper] to the result of this decoder, to map it to a new value. The
 * created decoder will delegate to this decoder and then apply the given [mapper] to the result.
 *
 * &nbsp;
 *
 * Here is an example of a decoder that decodes an integer and then squares it:
 *
 * ```
 * val numberDecoder = IntDecoder().mapResult { it * it }
 *
 * val result = numberDecoder.decode(byteArrayOf(0, 0, 0, 7)).get() // will return 49
 * ```
 *
 * **NOTE**: This method uses [Decoder.State.mapResult] to map the result, which means that the [mapper] will be called
 * only if the decoder returns a [Decoder.State.Done] state.
 *
 * &nbsp;
 *
 * @receiver the decoder producing the result to be mapped
 * @param mapper the mapping function
 * @return a decoder producing a result of type [R]
 * @param T the type of the result of the decoder
 * @param R the type of the result of the returned decoder
 */
fun <T, R> Decoder<T>.mapResult(mapper: (T) -> R): Decoder<R> = object : Decoder<R> {

    override fun decode(input: DecoderInput): Decoder.State<R> = this@mapResult.decode(input).mapResult(mapper)

    override fun reset() = this@mapResult.reset()

}

/**
 * Creates a new decoder by applying the given [mapper] to the result of this decoder, to map it to a new state. The
 * created decoder will delegate to this decoder and then apply the given [mapper] to the result.
 *
 * &nbsp;
 *
 * This method is usually preferred over [mapResult] when validation is required, as it allows the decoder to return
 * a [Decoder.State.Error] instead of throwing an exception.
 *
 * &nbsp;
 *
 * Here is an example of a decoder that decodes a byte and inverses it if it is non-zero:
 *
 * ```
 * val numberDecoder = IntDecoder().mapState {
 *     if (it == 0) {
 *         Decoder.State.Error("Cannot inverse zero")
 *     } else {
 *         Decoder.State.Done(1 / it.toDouble())
 *     }
 * }
 *
 * val result = numberDecoder.decode(byteArrayOf(2)).get() // will return 0.5
 *
 * val error = numberDecoder.decode(byteArrayOf(0)) // will return an error
 * ```
 *
 * **NOTE**: This method uses [Decoder.State.mapState] to map the result, which means that the [mapper] will not be
 * called if the decoder returns an error.
 *
 * &nbsp;
 *
 * @receiver the decoder producing the result to be mapped
 * @param mapper the mapping function
 * @return a decoder producing a result of type [R]
 * @param T the type of the result of the decoder
 * @param R the type of the result of the returned decoder
 */
fun <T, R> Decoder<T>.mapState(mapper: (T) -> Decoder.State<R>): Decoder<R> = object : Decoder<R> {

    override fun decode(input: DecoderInput): Decoder.State<R> = this@mapState.decode(input).mapState(mapper)

    override fun reset() = this@mapState.reset()

}

/**
 * Creates a new decoder that decodes a nullable [T]. The created decoder will read first a boolean value from the input
 * to determine whether the value is null or not. If the value is null, the decoder will return `null`, otherwise it
 * will delegate to this decoder and return the result.
 *
 * @receiver the decoder decoding the non-null value
 * @param nullabilityDecoder the decoder decoding the nullability flag (defaults to [BooleanDecoder])
 * @return a decoder decoding a nullable [T]
 * @param T the type of the result of the decoder
 */
@JvmOverloads
fun <T : Any> Decoder<T>.toOptional(
    nullabilityDecoder: Decoder<Boolean> = BooleanDecoder(),
): Decoder<T?> {
    val nullDecoder = NullDecoder<T>()
    return nullabilityDecoder.mapTo {
        if (it) {
            this@toOptional
        } else {
            nullDecoder
        }
    }
}

/**
 * Creates a new decoder that decodes a collection of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read first the size of the
 * collection from the input, then it will read the specified number of elements from the input and return them as a
 * collection.
 *
 * @receiver the decoder decoding the elements of the collection
 * @param collector the collector used to create the collection
 * @param sizeDecoder the decoder decoding the size of the collection (defaults to the default [IntDecoder])
 * @return a decoder decoding a [R] collection of [T]s
 * @param T the type of the elements of the collection
 * @param C the type of the accumulator of the collector
 * @param R the type of the result of the collector
 */
@JvmOverloads
fun <T, C, R> Decoder<T>.toCollection(
    collector: Collector<T, C, R>,
    sizeDecoder: Decoder<Number> = IntDecoder(),
): Decoder<R> = PrefixedArityReductionDecoder(collector, this, sizeDecoder)

/**
 * Creates a new decoder that decodes a collection of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read the specified number of elements from the input and return them as a collection.
 *
 * @receiver the decoder decoding the elements of the collection
 * @param collector the collector used to create the collection
 * @param size the size of the collection
 * @return a decoder decoding a [R] collection of [T]s
 * @param T the type of the elements of the collection
 * @param C the type of the accumulator of the collector
 * @param R the type of the result of the collector
 */
fun <T, C, R> Decoder<T>.toCollection(
    collector: Collector<T, C, R>,
    size: Int,
): Decoder<R> {
    require(size >= 0) { "Size must be non-negative, but was $size" }
    return ConstantArityReductionDecoder(collector, this, size)
}

/**
 * Creates a new decoder that decodes a collection of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read elements from the input until the specified [shouldStop] returns `true`, then it will
 * return the read elements as a collection. If [keepLast] is `true`, the last element will be kept in the collection.
 *
 * @receiver the decoder decoding the elements of the collection
 * @param collector the collector used to create the collection
 * @param keepLast whether the last element should be kept in the collection (defaults to `false`)
 * @param shouldStop the predicate used to determine whether the decoder should stop reading elements
 * @return a decoder decoding a [R] collection of [T]s
 * @param T the type of the elements of the collection
 * @param C the type of the accumulator of the collector
 * @param R the type of the result of the collector
 * @param shouldStop the predicate used to determine whether the decoder should stop reading elements
 */
@JvmOverloads
fun <T, C, R> Decoder<T>.toCollection(
    collector: Collector<T, C, R>,
    keepLast: Boolean = false,
    shouldStop: (T) -> Boolean,
): Decoder<R> = MarkerEndedReductionDecoder(collector, this, keepLast, shouldStop)

/**
 * Creates a new decoder that decodes a list of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read first the size of the list from the input, then it will read the specified number of
 * elements from the input and return them as a list.
 *
 * @receiver the decoder decoding the elements of the list
 * @param sizeDecoder the decoder decoding the size of the list (defaults to the default [IntDecoder])
 * @return a decoder decoding a list of [T]s
 * @param T the type of the elements of the list
 * @see toCollection
 */
@JvmOverloads
fun <T> Decoder<T>.toList(sizeDecoder: Decoder<Number> = IntDecoder()): Decoder<List<T>> =
    toCollection(toListCollector(), sizeDecoder)

/**
 * Creates a new decoder that decodes a list of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read the specified number of elements from the input and return them as a list.
 *
 * @receiver the decoder decoding the elements of the list
 * @param size the size of the list
 * @return a decoder decoding a list of [T]s
 * @param T the type of the elements of the list
 * @see toCollection
 */
fun <T> Decoder<T>.toList(size: Int): Decoder<List<T>> {
    require(size >= 0) { "Size must be non-negative, but was $size" }
    return toCollection(toListCollector(), size)
}

/**
 * Creates a new decoder that decodes a list of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read elements from the input until the specified [shouldStop] returns `true`, then it will
 * return the read elements as a list. If [keepLast] is `true`, the last element will be kept in the list.
 *
 * @receiver the decoder decoding the elements of the list
 * @param keepLast whether the last element should be kept in the list (defaults to `false`)
 * @param shouldStop the predicate used to determine whether the decoder should stop reading elements
 * @return a decoder decoding a list of [T]s
 * @param T the type of the elements of the list
 * @see toCollection
 */
@JvmOverloads
fun <T> Decoder<T>.toList(keepLast: Boolean = false, shouldStop: (T) -> Boolean): Decoder<List<T>> =
    toCollection(toListCollector(), keepLast, shouldStop)

/**
 * Creates a new decoder that decodes a set of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read first the size of the set from the input, then it will read the specified number of
 * elements from the input and return them as a set.
 *
 * @receiver the decoder decoding the elements of the set
 * @param sizeDecoder the decoder decoding the size of the set (defaults to the default [IntDecoder])
 * @return a decoder decoding a set of [T]s
 * @param T the type of the elements of the set
 * @see toCollection
 */
@JvmOverloads
fun <T> Decoder<T>.toSet(sizeDecoder: Decoder<Number> = IntDecoder()): Decoder<Set<T>> =
    toCollection(toSetCollector(), sizeDecoder)

/**
 * Creates a new decoder that decodes a set of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read the specified number of elements from the input and return them as a set.
 *
 * @receiver the decoder decoding the elements of the set
 * @param size the size of the set
 * @return a decoder decoding a set of [T]s
 * @param T the type of the elements of the set
 * @see toCollection
 */
fun <T> Decoder<T>.toSet(size: Int): Decoder<Set<T>> {
    require(size >= 0) { "Size must be non-negative, but was $size" }
    return toCollection(toSetCollector(), size)
}

/**
 * Creates a new decoder that decodes a set of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read elements from the input until the specified [shouldStop] returns `true`, then it will
 * return the read elements as a set. If [keepLast] is `true`, the last element will be kept in the set.
 *
 * @receiver the decoder decoding the elements of the set
 * @param keepLast whether the last element should be kept in the set (defaults to `false`)
 * @param shouldStop the predicate used to determine whether the decoder should stop reading elements
 * @return a decoder decoding a set of [T]s
 * @param T the type of the elements of the set
 * @see toCollection
 */
@JvmOverloads
fun <T> Decoder<T>.toSet(keepLast: Boolean = false, shouldStop: (T) -> Boolean): Decoder<Set<T>> =
    toCollection(toSetCollector(), keepLast, shouldStop)

/**
 * Creates a new decoder that decodes a map of [K]s to [V]s from a [Pair] decoder.
 *
 * &nbsp;
 *
 * The created decoder will read first the size of the map from the input, then it will read the specified number of
 * elements from the input and return them as a map.
 *
 * @receiver a [Pair] decoder decoding the keys and values of the map
 * @param sizeDecoder the decoder decoding the size of the map (defaults to the default [IntDecoder])
 * @return a decoder decoding a map of [K]s to [V]s
 * @param K the type of the keys of the map
 * @param V the type of the values of the map
 * @see toCollection
 */
@JvmOverloads
fun <K, V> Decoder<Pair<K, V>>.toMap(sizeDecoder: Decoder<Number> = IntDecoder()): Decoder<Map<K, V>> =
    toCollection(toMapCollector(), sizeDecoder)

/**
 * Creates a new decoder that decodes a map of [K]s to [V]s from a [Pair] decoder.
 *
 * &nbsp;
 *
 * The created decoder will read the specified number of elements from the input and return them as a map.
 *
 * @receiver a [Pair] decoder decoding the keys and values of the map
 * @param size the size of the map
 * @return a decoder decoding a map of [K]s to [V]s
 * @param K the type of the keys of the map
 * @param V the type of the values of the map
 * @see toCollection
 */
fun <K, V> Decoder<Pair<K, V>>.toMap(size: Int): Decoder<Map<K, V>> {
    require(size >= 0) { "Size must be non-negative, but was $size" }
    return toCollection(toMapCollector(), size)
}

/**
 * Creates a new decoder that decodes a map of [K]s to [V]s from a [Pair] decoder.
 *
 * &nbsp;
 *
 * The created decoder will read elements from the input until the specified [shouldStop] returns `true`, then it will
 * return the read elements as a map. If [keepLast] is `true`, the last element will be kept in the map.
 *
 * @receiver a [Pair] decoder decoding the keys and values of the map
 * @param keepLast whether the last element should be kept in the map (defaults to `false`)
 * @param shouldStop the predicate used to determine whether the decoder should stop reading elements
 * @return a decoder decoding a map of [K]s to [V]s
 * @param K the type of the keys of the map
 * @param V the type of the values of the map
 * @see toCollection
 */
@JvmOverloads
fun <K, V> Decoder<Pair<K, V>>.toMap(
    keepLast: Boolean = false,
    shouldStop: (Pair<K, V>) -> Boolean,
): Decoder<Map<K, V>> = toCollection(toMapCollector(), keepLast, shouldStop)

/**
 * Creates a new decoder that decodes an array of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read the specified number of elements from the input and return them as an array.
 *
 * &nbsp;
 *
 * **NOTE**: most of the time the [factory] parameter can be [::arrayOfNulls][arrayOfNulls].
 *
 * &nbsp;
 *
 * @receiver the decoder decoding the elements of the array
 * @param factory the factory used to create the array from the size
 * @param sizeDecoder the decoder decoding the size of the array (defaults to the default [IntDecoder])
 * @return a decoder decoding an array of [T]s
 * @param T the type of the elements of the array
 * @see toCollection
 */
@JvmOverloads
fun <T> Decoder<T>.toArray(
    factory: (Int) -> Array<T?>,
    sizeDecoder: Decoder<Number> = IntDecoder(),
): Decoder<Array<T>> = toCollection(ExtendedCollectors.toArray(factory), sizeDecoder)

/**
 * Creates a new decoder that decodes an array of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read the specified number of elements from the input and return them as an array.
 *
 * &nbsp;
 *
 * **NOTE**: most of the time the [factory] parameter can be [::arrayOfNulls][arrayOfNulls].
 *
 * &nbsp;
 *
 * @receiver the decoder decoding the elements of the array
 * @param factory the factory used to create the array from the size
 * @param size the size of the array
 * @return a decoder decoding an array of [T]s
 * @param T the type of the elements of the array
 * @see toCollection
 */
fun <T> Decoder<T>.toArray(factory: (Int) -> Array<T?>, size: Int): Decoder<Array<T>> {
    require(size >= 0) { "Size must be non-negative, but was $size" }
    return toCollection(ExtendedCollectors.toArray(factory), size)
}

/**
 * Creates a new decoder that decodes an array of [T]s.
 *
 * &nbsp;
 *
 * The created decoder will read elements from the input until the specified [shouldStop] returns `true`, then it will
 * return the read elements as an array. If [keepLast] is `true`, the last element will be kept in the array.
 *
 * &nbsp;
 *
 * **NOTE**: most of the time the [factory] parameter can be [::arrayOfNulls][arrayOfNulls].
 *
 * &nbsp;
 *
 * @receiver the decoder decoding the elements of the array
 * @param keepLast whether the last element should be kept in the array (defaults to `false`)
 * @param shouldStop the predicate used to determine whether the decoder should stop reading elements
 * @param factory the factory used to create the array from the size
 * @return a decoder decoding an array of [T]s
 * @param T the type of the elements of the array
 * @see toCollection
 */
@JvmOverloads
fun <T> Decoder<T>.toArray(
    factory: (Int) -> Array<T?>,
    keepLast: Boolean = false,
    shouldStop: (T) -> Boolean,
): Decoder<Array<T>> = toCollection(ExtendedCollectors.toArray(factory), keepLast, shouldStop)

/**
 * Creates a new decoder that decodes a pair of [T]s and [U]s from two decoders.
 *
 * @receiver the decoder decoding the first element of the pair
 * @param other the decoder decoding the second element of the pair
 * @return a decoder decoding a pair of [T]s and [U]s
 * @param T the type of the first element of the pair
 * @param U the type of the second element of the pair
 * @see PairDecoder
 */
@HideFromJava
infix fun <T, U> Decoder<T>.and(other: Decoder<U>): Decoder<Pair<T, U>> = PairDecoder(this, other)

internal fun <T> toListCollector(): Collector<T, *, List<T>> = toList.unsafeCast()

internal fun <T> toSetCollector(): Collector<T, *, Set<T>> = toSet.unsafeCast()

internal fun <K, V> toMapCollector(): Collector<Pair<K, V>, *, Map<K, V>> = toMap.unsafeCast()

private val toList = Collectors.toList<Any>()

private val toSet = Collectors.toSet<Any>()

private val toMap = ExtendedCollectors.toMap<Any, Any>()
