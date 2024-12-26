package com.kamelia.sprinkler.util

import java.util.stream.Collector
import java.util.stream.Collectors

/**
 * Class containing additional [collectors][Collectors] for [streams][java.util.stream.Stream].
 *
 * @see Collector
 * @see Collectors
 */
object ExtendedCollectors {

    /**
     * Returns a [collector][Collector] that collects elements to a [map][Map] from [pairs][Pair] of keys and values.
     *
     * @param K the type of keys
     * @param V the type of values
     * @return a [collector][Collector] that creates a [map][Map] from [pairs][Pair] of keys and values
     */
    @JvmStatic
    fun <K, V> toMap(): Collector<Pair<K, V>, *, Map<K, V>> = Collectors.toMap(Pair<K, V>::first, Pair<K, V>::second)

    /**
     * Returns a [collector][Collector] that collects elements to a [LinkedHashMap] from [pairs][Pair] of keys and
     * values.
     *
     * @param mergeFunction a merge function, used to resolve collisions between values associated with the same key
     * @param K the type of keys
     * @param V the type of values
     * @return a [collector][Collector] that creates a [LinkedHashMap] from [pairs][Pair] of keys and values
     */
    @JvmStatic
    @JvmOverloads
    fun <K, V> toLinkedHashMap(
        mergeFunction: (V, V) -> V = { _, b -> b }
    ): Collector<Pair<K, V>, *, LinkedHashMap<K, V>> =
        Collectors.toMap(Pair<K, V>::first, Pair<K, V>::second, mergeFunction, ::LinkedHashMap)

    /**
     * Returns a [collector][Collector] that collects elements to a [map][Map] from [entries][Map.Entry].
     *
     * @param mergeFunction a merge function, used to resolve collisions between values associated with the same key
     * @param K the type of keys
     * @param V the type of values
     * @return a [collector][Collector] that creates a [map][Map] from [entries][Map.Entry]
     */
    @JvmStatic
    @JvmOverloads
    fun <K, V> toMapUsingEntries(mergeFunction: (V, V) -> V = { _, b -> b }): Collector<Map.Entry<K, V>, *, Map<K, V>> =
        Collectors.toMap(Map.Entry<K, V>::key, Map.Entry<K, V>::value, mergeFunction)

    /**
     * Returns a [collector][Collector] that collects elements to a [LinkedHashMap] from [entries][Map.Entry].
     *
     * @param mergeFunction a merge function, used to resolve collisions between values associated with the same key
     * @param K the type of keys
     * @param V the type of values
     * @return a [collector][Collector] that creates a [map][Map] from [entries][Map.Entry]
     */
    @JvmStatic
    @JvmOverloads
    fun <K, V> toLinkedHashMapUsingEntries(
        mergeFunction: (V, V) -> V = { _, b -> b },
    ): Collector<Map.Entry<K, V>, *, LinkedHashMap<K, V>> =
        Collectors.toMap(Map.Entry<K, V>::key, Map.Entry<K, V>::value, mergeFunction, ::LinkedHashMap)

    /**
     * Returns a [collector][Collector] that collects elements to an [array][Array].
     *
     * @param T the type of elements
     * @param factory a function that creates an array of the desired type and the given size
     * @return a [collector][Collector] that creates an [array][Array] from elements
     */
    @JvmStatic
    fun <T> toArray(factory: (Int) -> Array<T?>): Collector<T, *, Array<T>> = Collector.of<T, ArrayList<T>, Array<T>?>(
        ::ArrayList,
        { list, t -> list.add(t) },
        { list1, list2 -> list1.apply { addAll(list2) } },
        { list ->
            val array = factory(list.size).unsafeCast<Array<T>>()
            list.forEachIndexed { index, t -> array[index] = t }
            array
        }
    )

    /**
     * Returns a [collector][Collector] that collects elements to an [int array][IntArray].
     *
     * @return a [collector][Collector] that creates an [int array][IntArray] from elements
     */
    @JvmStatic
    fun toIntArray(): Collector<Int, *, IntArray> = toPrimitiveArray(::IntArray, IntArray::set)

    /**
     * Returns a [collector][Collector] that collects elements to a [long array][LongArray].
     *
     * @return a [collector][Collector] that creates a [long array][LongArray] from elements
     */
    @JvmStatic
    fun toLongArray(): Collector<Long, *, LongArray> = toPrimitiveArray(::LongArray, LongArray::set)

    /**
     * Returns a [collector][Collector] that collects elements to a [double array][DoubleArray].
     *
     * @return a [collector][Collector] that creates a [double array][DoubleArray] from elements
     */
    @JvmStatic
    fun toDoubleArray(): Collector<Double, *, DoubleArray> = toPrimitiveArray(::DoubleArray, DoubleArray::set)

    /**
     * Returns a [collector][Collector] that collects elements to a [float array][FloatArray].
     *
     * @return a [collector][Collector] that creates a [float array][FloatArray] from elements
     */
    @JvmStatic
    fun toFloatArray(): Collector<Float, *, FloatArray> = toPrimitiveArray(::FloatArray, FloatArray::set)

    /**
     * Returns a [collector][Collector] that collects elements to a [short array][ShortArray].
     *
     * @return a [collector][Collector] that creates a [short array][ShortArray] from elements
     */
    @JvmStatic
    fun toShortArray(): Collector<Short, *, ShortArray> = toPrimitiveArray(::ShortArray, ShortArray::set)

    /**
     * Returns a [collector][Collector] that collects elements to a [byte array][ByteArray].
     *
     * @return a [collector][Collector] that creates a [byte array][ByteArray] from elements
     */
    @JvmStatic
    fun toByteArray(): Collector<Byte, *, ByteArray> = toPrimitiveArray(::ByteArray, ByteArray::set)

    /**
     * Returns a [collector][Collector] that collects elements to a [char array][CharArray].
     *
     * @return a [collector][Collector] that creates a [char array][CharArray] from elements
     */
    @JvmStatic
    fun toCharArray(): Collector<Char, *, CharArray> = toPrimitiveArray(::CharArray, CharArray::set)

    /**
     * Returns a [collector][Collector] that collects elements to a [boolean array][BooleanArray].
     *
     * @return a [collector][Collector] that creates a [boolean array][BooleanArray] from elements
     */
    @JvmStatic
    fun toBooleanArray(): Collector<Boolean, *, BooleanArray> = toPrimitiveArray(::BooleanArray, BooleanArray::set)

    private fun <T, R> toPrimitiveArray(
        arrayFactory: (Int) -> R,
        arrayAccumulator: R.(Int, T) -> Unit,
    ): Collector<T, *, R> = Collector.of(
        ::ArrayList,
        ArrayList<T>::add,
        { list1, list2 -> list1.apply { addAll(list2) } },
        { list ->
            val r = arrayFactory(list.size)
            list.forEachIndexed { index, t -> r.arrayAccumulator(index, t) }
            r
        }
    )

}
