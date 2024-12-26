@file:JvmName("EncoderUtils")

package com.kamelia.sprinkler.transcoder.binary.encoder

import com.kamelia.sprinkler.transcoder.binary.encoder.core.Encoder
import com.zwendo.restrikt2.annotation.HideFromJava

/**
 * Creates an encoder that encodes an object [R] using a given encoder of type [T]. The created encoder will map the
 * input object [R] to an object of type [T] using the given [mapper] function, and then delegate the encoding to the
 * original encoder.
 *
 * Here is an example of how to use this function:
 * ```
 * val longEncoder = LongEncoder()
 * val instantEncoder: Encoder<Instant> = longEncoder.withMappedInput(Instant::toEpochMilli)
 * ```
 *
 * The above code creates an encoder that encodes [Instant][java.time.Instant] objects by first converting them to a
 * long value using [Instant.toEpochMilli][java.time.Instant.toEpochMilli], and then encoding the long value using the
 * longEncoder.
 *
 * @receiver the original encoder of type [T]
 * @param mapper the function that maps the input object [R] to an object of type [T]
 * @return an encoder that encodes an object [R]
 */
fun <T, R> Encoder<T>.withMappedInput(mapper: (R) -> T): Encoder<R> =
    Encoder { obj, output -> encode(mapper(obj), output) }

/**
 * Creates an encoder that encodes an [Iterable] of objects of type [T]. The created encoder will encode each object
 * in the iterable using the original encoder, and then encode the [endMarker] using the original encoder. The end
 * marker is used to indicate the end of the iterable.
 *
 * @receiver the original encoder of type [T]
 * @param endMarker the end marker that is used to indicate the end of the iterable
 * @return an encoder that encodes an [Iterable] of objects of type [T]
 */
fun <T> Encoder<T>.toIterable(endMarker: T): Encoder<Iterable<T>> =
    Encoder { obj, output ->
        obj.forEach { encode(it, output) }
        encode(endMarker, output)
    }

/**
 * Creates an encoder that encodes a [Collection] of objects of type [T]. The created encoder will encode the size of
 * the collection using the given [sizeEncoder], and then encode each object in the collection using the original
 * encoder.
 *
 * @receiver the original encoder of type [T]
 * @param sizeEncoder the encoder that is used to encode the size of the collection (defaults to the default
 * [IntEncoder])
 * @return an encoder that encodes a [Collection] of objects of type [T]
 */
@JvmOverloads
fun <T> Encoder<T>.toCollection(sizeEncoder: Encoder<Int> = IntEncoder()): Encoder<Collection<T>> =
    Encoder { obj, output ->
        sizeEncoder.encode(obj.size, output)
        obj.forEach { encode(it, output) }
    }

/**
 * Creates an encoder that encodes a [Map] of objects of type [K] and [V]. The created encoder will encode the size of
 * the map using the given [sizeEncoder], and then encode each entry in the map as a [Pair] using the original encoder.
 *
 * @receiver the original encoder of [Pairs][Pair] of [K] and [V]
 * @param sizeEncoder the encoder that is used to encode the size of the map (defaults to the default [IntEncoder])
 * @return an encoder that encodes a [Map] of objects of type [K] and [V]
 */
@JvmOverloads
fun <K, V> Encoder<Pair<K, V>>.toMap(sizeEncoder: Encoder<Int> = IntEncoder()): Encoder<Map<K, V>> =
    Encoder { obj, output ->
        sizeEncoder.encode(obj.size, output)
        obj.forEach { (key, value) -> encode(key to value, output) }
    }

/**
 * Creates an encoder that encodes a [Map] of objects of type [K] and [V]. The created encoder will encode each entry
 * in the map as a [Pair] using the original encoder, and then encode the [endMarker] using the original encoder. The
 * end marker is used to indicate the end of the map.
 *
 * @receiver the original encoder of [Pairs][Pair] of [K] and [V]
 * @param endMarker the end marker that is used to indicate the end of the map
 * @return an encoder that encodes a [Map] of objects of type [K] and [V]
 */
fun <K, V> Encoder<Pair<K, V>>.toMap(endMarker: Pair<K, V>): Encoder<Map<K, V>> =
    Encoder { obj, output ->
        obj.forEach { (key, value) ->
            encode(key to value, output)
        }
        encode(endMarker, output)
    }

/**
 * Creates an encoder that encodes a [Map] of objects of type [K] and [V]. The created encoder will encode the size of
 * the map using the given [sizeEncoder], and then encode each key and value in the map using the original encoder for
 * keys and the given [valueEncoder] for values.
 *
 * @receiver the original encoder of keys of type [K]
 * @param valueEncoder the encoder that is used to encode the values of the map
 * @param sizeEncoder the encoder that is used to encode the size of the map (defaults to the default [IntEncoder])
 * @return an encoder that encodes a [Map] of objects of type [K] and [V]
 */
fun <K, V> Encoder<K>.toMap(valueEncoder: Encoder<V>, sizeEncoder: Encoder<Int> = IntEncoder()): Encoder<Map<K, V>> =
    Encoder { obj, output ->
        sizeEncoder.encode(obj.size, output)
        obj.forEach { (key, value) ->
            encode(key, output)
            valueEncoder.encode(value, output)
        }
    }

/**
 * Creates an encoder that encodes a [Map] of objects of type [K] and [V]. The created encoder will encode each key and
 * value in the map using the original encoder for keys and the given [valueEncoder] for values, and then encode the
 * [endMarker] in the same way. The end marker is used to indicate the end of the map.
 *
 * @receiver the original encoder of keys of type [K]
 * @param valueEncoder the encoder that is used to encode the values of the map
 * @param endMarker the end marker that is used to indicate the end of the map
 * @return an encoder that encodes a [Map] of objects of type [K] and [V]
 */
fun <K, V> Encoder<K>.toMap(valueEncoder: Encoder<V>, endMarker: Pair<K, V>): Encoder<Map<K, V>> =
    Encoder { obj, output ->
        obj.forEach { (key, value) ->
            encode(key, output)
            valueEncoder.encode(value, output)
        }
        encode(endMarker.first, output)
        valueEncoder.encode(endMarker.second, output)
    }

/**
 * Creates an encoder that encodes an [Array] of objects of type [T]. The created encoder will encode the size of the
 * array using the given [sizeEncoder], and then encode each object in the array using the original encoder.
 *
 * @receiver the original encoder of type [T]
 * @param sizeEncoder the encoder that is used to encode the size of the array (defaults to the default [IntEncoder])
 * @return an encoder that encodes an [Array] of objects of type [T]
 */
@JvmOverloads
fun <T> Encoder<T>.toArray(sizeEncoder: Encoder<Int> = IntEncoder()): Encoder<Array<T>> =
    Encoder { obj, output ->
        sizeEncoder.encode(obj.size, output)
        obj.forEach { encode(it, output) }
    }

/**
 * Creates an encoder that encodes an [Array] of objects of type [T]. The created encoder will encode each object in
 * the array using the original encoder, and then encode the [endMarker] in the same way. The end marker is used to
 * indicate the end of the array.
 *
 * @receiver the original encoder of type [T]
 * @param endMarker the end marker that is used to indicate the end of the array
 * @return an encoder that encodes an [Array] of objects of type [T]
 */
fun <T> Encoder<T>.toArray(endMarker: T): Encoder<Array<T>> =
    Encoder { obj, output ->
        obj.forEach { encode(it, output) }
        encode(endMarker, output)
    }

/**
 * Creates an encoder that encodes a nullable [T] object. The created encoder will encode a [Boolean] indicating
 * whether the object is null or not, and then encode the object using the original encoder if it is not null.
 *
 * @receiver the original encoder of type [T]
 * @param nullabilityEncoder the encoder that is used to encode the nullability of the object (defaults to the
 * default [BooleanEncoder])
 * @return an encoder that encodes a nullable [T] object
 */
@JvmOverloads
fun <T : Any> Encoder<T>.toOptional(nullabilityEncoder: Encoder<Boolean> = BooleanEncoder()): Encoder<T?> =
    Encoder { obj, output ->
        if (obj == null) {
            nullabilityEncoder.encode(false, output)
            return@Encoder
        }
        nullabilityEncoder.encode(true, output)
        encode(obj, output)
    }

/**
 * Creates a new encoder that encodes a pair of [T]s and [U]s from two encoders.
 *
 * @receiver the encoder encoding the first element of the pair
 * @param other the encoder encoding the second element of the pair
 * @return an encoder encoding a pair of [T]s and [U]s
 * @param T the type of the first element of the pair
 * @param U the type of the second element of the pair
 * @see PairEncoder
 */
@HideFromJava
infix fun <T, U> Encoder<T>.and(other: Encoder<U>): Encoder<Pair<T, U>> = PairEncoder(this, other)
