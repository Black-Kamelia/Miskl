package com.kamelia.sprinkler.util

import com.zwendo.restrikt.annotation.HideFromJava
import java.util.stream.Collector

/**
 * Shorthand method for [Collector#supplier().get()][Collector.supplier].
 *
 * @receiver the collector to get the supplier from
 * @return an instance of the collection type
 * @param C the type of the collection
 */
@HideFromJava
@Suppress("NOTHING_TO_INLINE")
inline fun <C> Collector<*, C, *>.supply(): C = supplier().get()

/**
 * Shorthand method for [Collector#accumulator().accept()][Collector.accumulator].
 *
 * @receiver the collector to get the accumulator from
 * @param T the type of the element to accumulate
 * @param C the type of the collection
 */
@HideFromJava
@Suppress("NOTHING_TO_INLINE")
inline fun <T, C> Collector<T, C, *>.accumulate(c: C, e: T): Unit = accumulator().accept(c, e)

/**
 * Shorthand method for [Collector#finisher().apply()][Collector.finisher].
 *
 * @receiver the collector to get the finisher from
 * @param C the type of the collection
 * @param R the type of the result
 * @return the result of the finisher
 */
@HideFromJava
@Suppress("NOTHING_TO_INLINE")
inline fun <C, R> Collector<*, C, R>.finish(c: C): R = finisher().apply(c)

/**
 * Shorthand method for [Collector#characteristics()][Collector.characteristics].
 *
 * @receiver the collector to get the characteristics from
 */
@HideFromJava
@Suppress("NOTHING_TO_INLINE")
inline val Collector<*, *, *>.characteristics: Set<Collector.Characteristics>
    get() = characteristics()

/**
 * Shorthand method for [Collector#combiner().apply()][Collector.combiner].
 *
 * @receiver the collector to get the combiner from
 * @param C the type of the collection
 */
@HideFromJava
@Suppress("NOTHING_TO_INLINE")
inline fun <C> Collector<*, C, *>.combine(c1: C, c2: C): C = combiner().apply(c1, c2)

