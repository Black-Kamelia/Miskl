@file:Suppress("NOTHING_TO_INLINE")
@file:HideFromJava

package com.kamelia.sprinkler.bridge

import com.zwendo.restrikt2.annotation.HideFromJava
import java.util.concurrent.Callable
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.DoubleBinaryOperator
import java.util.function.DoubleConsumer
import java.util.function.DoubleFunction
import java.util.function.DoublePredicate
import java.util.function.DoubleSupplier
import java.util.function.DoubleToIntFunction
import java.util.function.DoubleToLongFunction
import java.util.function.DoubleUnaryOperator
import java.util.function.Function
import java.util.function.IntBinaryOperator
import java.util.function.IntConsumer
import java.util.function.IntFunction
import java.util.function.IntPredicate
import java.util.function.IntSupplier
import java.util.function.IntToDoubleFunction
import java.util.function.IntToLongFunction
import java.util.function.IntUnaryOperator
import java.util.function.LongBinaryOperator
import java.util.function.LongConsumer
import java.util.function.LongFunction
import java.util.function.LongPredicate
import java.util.function.LongSupplier
import java.util.function.LongToDoubleFunction
import java.util.function.LongToIntFunction
import java.util.function.LongUnaryOperator
import java.util.function.ObjDoubleConsumer
import java.util.function.ObjIntConsumer
import java.util.function.ObjLongConsumer
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.function.ToDoubleBiFunction
import java.util.function.ToDoubleFunction
import java.util.function.ToIntBiFunction
import java.util.function.ToIntFunction
import java.util.function.ToLongBiFunction
import java.util.function.ToLongFunction

/**
 * Alias for [Callable.call].
 *
 * @receiver the called [Callable]
 * @return [Callable.call] result
 */
inline operator fun <T> Callable<T>.invoke(): T = call()

/**
 * Alias for [Runnable.run].
 *
 * @receiver the called [Runnable]
 */
inline operator fun Runnable.invoke(): Unit = run()

//region Consumer

/**
 * Alias for [Consumer.accept].
 *
 * @receiver the called [Consumer]
 * @param t value to accept
 * @param T the type of the value to accept
 */
inline operator fun <T> Consumer<T>.invoke(t: T): Unit = accept(t)

/**
 * Alias for [IntConsumer.accept].
 *
 * @receiver the called [IntConsumer]
 * @param i value ([Int]) to accept
 */
inline operator fun IntConsumer.invoke(i: Int): Unit = accept(i)

/**
 * Alias for [LongConsumer.accept].
 *
 * @receiver the called [LongConsumer]
 * @param l value ([Long]) to accept
 */
inline operator fun LongConsumer.invoke(l: Long): Unit = accept(l)

/**
 * Alias for [DoubleConsumer.accept].
 *
 * @receiver the called [DoubleConsumer]
 * @param d value ([Double]) to accept
 */
inline operator fun DoubleConsumer.invoke(d: Double): Unit = accept(d)

//endregion

//region BiConsumer

/**
 * Alias for [BiConsumer.accept].
 *
 * @receiver the called [BiConsumer]
 * @param t first value to accept
 * @param u second value to accept
 * @param T the type of the first value to accept
 * @param U the type of the second value to accept
 */
inline operator fun <T, U> BiConsumer<T, U>.invoke(t: T, u: U): Unit = accept(t, u)

/**
 * Alias for [ObjIntConsumer.accept].
 *
 * @receiver the called [ObjIntConsumer]
 * @param t first value to accept
 * @param i second value ([Int]) to accept
 * @param T the type of the first value to accept
 */
inline operator fun <T> ObjIntConsumer<T>.invoke(t: T, i: Int): Unit = accept(t, i)

/**
 * Alias for [ObjLongConsumer.accept].
 *
 * @receiver the called [ObjLongConsumer]
 * @param t first value to accept
 * @param l second value ([Long]) to accept
 * @param T the type of the first value to accept
 */
inline operator fun <T> ObjLongConsumer<T>.invoke(t: T, l: Long): Unit = accept(t, l)

/**
 * Alias for [ObjDoubleConsumer.accept].
 *
 * @receiver the called [ObjDoubleConsumer]
 * @param t first value to accept
 * @param d second value ([Double]) to accept
 * @param T the type of the first value to accept
 */
inline operator fun <T> ObjDoubleConsumer<T>.invoke(t: T, d: Double): Unit = accept(t, d)

//endregion

//region Function

/**
 * Alias for [Function.apply].
 *
 * @receiver the called [Function]
 * @param t value to apply
 * @param T the type of the value to apply
 * @param R the type of the result of the function
 * @return [Function.apply] result
 */
inline operator fun <T, R> Function<T, R>.invoke(t: T): R = apply(t)

/**
 * Alias for [IntFunction.apply].
 *
 * @receiver the called [IntFunction]
 * @param i value ([Int]) to apply
 * @param T the type of the result of the function
 * @return [IntFunction.apply] result
 */
inline operator fun <T> IntFunction<T>.invoke(i: Int): T = apply(i)

/**
 * Alias for [LongFunction.apply].
 *
 * @receiver the called [LongFunction]
 * @param l value ([Long]) to apply
 * @param T the type of the result of the function
 * @return [LongFunction.apply] result
 */
inline operator fun <T> LongFunction<T>.invoke(l: Long): T = apply(l)

/**
 * Alias for [DoubleFunction.apply].
 *
 * @receiver the called [DoubleFunction]
 * @param d value ([Double]) to apply
 * @param T the type of the result of the function
 * @return [DoubleFunction.apply] result
 */
inline operator fun <T> DoubleFunction<T>.invoke(d: Double): T = apply(d)

/**
 * Alias for [ToIntFunction.applyAsInt].
 *
 * @receiver the called [ToIntFunction]
 * @param t value to apply
 * @param T the type of the value to apply
 * @return [ToIntFunction.applyAsInt] result
 */
inline operator fun <T> ToIntFunction<T>.invoke(t: T): Int = applyAsInt(t)

/**
 * Alias for [ToLongFunction.applyAsLong].
 *
 * @receiver the called [ToLongFunction]
 * @param t value to apply
 * @param T the type of the value to apply
 * @return [ToLongFunction.applyAsLong] result
 */
inline operator fun <T> ToLongFunction<T>.invoke(t: T): Long = applyAsLong(t)

/**
 * Alias for [ToDoubleFunction.applyAsDouble].
 *
 * @receiver the called [ToDoubleFunction]
 * @param t value to apply
 * @param T the type of the value to apply
 * @return [ToDoubleFunction.applyAsDouble] result
 */
inline operator fun <T> ToDoubleFunction<T>.invoke(t: T): Double = applyAsDouble(t)

/**
 * Alias for [IntToLongFunction.applyAsLong].
 *
 * @receiver the called [IntToLongFunction]
 * @param i value ([Int]) to apply
 * @return [IntToLongFunction.applyAsLong] result
 */
inline operator fun IntToLongFunction.invoke(i: Int): Long = applyAsLong(i)

/**
 * Alias for [IntToDoubleFunction.applyAsDouble].
 *
 * @receiver the called [IntToDoubleFunction]
 * @param i value ([Int]) to apply
 * @return [IntToDoubleFunction.applyAsDouble] result
 */
inline operator fun IntToDoubleFunction.invoke(i: Int): Double = applyAsDouble(i)

/**
 * Alias for [LongToIntFunction.applyAsInt].
 *
 * @receiver the called [LongToIntFunction]
 * @param l value ([Long]) to apply
 * @return [LongToIntFunction.applyAsInt] result
 */
inline operator fun LongToIntFunction.invoke(l: Long): Int = applyAsInt(l)

/**
 * Alias for [LongToDoubleFunction.applyAsDouble].
 *
 * @receiver the called [LongToDoubleFunction]
 * @param l value ([Long]) to apply
 * @return [LongToDoubleFunction.applyAsDouble] result
 */
inline operator fun LongToDoubleFunction.invoke(l: Long): Double = applyAsDouble(l)

/**
 * Alias for [DoubleToIntFunction.applyAsInt].
 *
 * @receiver the called [DoubleToIntFunction]
 * @param d value ([Double]) to apply
 * @return [DoubleToIntFunction.applyAsInt] result
 */
inline operator fun DoubleToIntFunction.invoke(d: Double): Int = applyAsInt(d)

/**
 * Alias for [DoubleToLongFunction.applyAsLong].
 *
 * @receiver the called [DoubleToLongFunction]
 * @param d value ([Double]) to apply
 * @return [DoubleToLongFunction.applyAsLong] result
 */
inline operator fun DoubleToLongFunction.invoke(d: Double): Long = applyAsLong(d)

//endregion

//region BiFunction

/**
 * Alias for [BiFunction.apply].
 *
 * @receiver the called [BiFunction]
 * @param t first value to apply
 * @param u second value to apply
 * @param T the type of the first value to apply
 * @param U the type of the second value to apply
 * @param R the type of the result of the function
 * @return [BiFunction.apply] result
 */
inline operator fun <T, U, R> BiFunction<T, U, R>.invoke(t: T, u: U): R = apply(t, u)

/**
 * Alias for [ToIntBiFunction.applyAsInt].
 *
 * @receiver the called [ToIntBiFunction]
 * @param t first value to apply
 * @param u second value to apply
 * @param T the type of the first value to apply
 * @param U the type of the second value to apply
 * @return [ToIntBiFunction.applyAsInt] result
 */
inline operator fun <T, U> ToIntBiFunction<T, U>.invoke(t: T, u: U): Int = applyAsInt(t, u)

/**
 * Alias for [ToLongBiFunction.applyAsLong].
 *
 * @receiver the called [ToLongBiFunction]
 * @param t first value to apply
 * @param u second value to apply
 * @param T the type of the first value to apply
 * @param U the type of the second value to apply
 * @return [ToLongBiFunction.applyAsLong] result
 */
inline operator fun <T, U> ToLongBiFunction<T, U>.invoke(t: T, u: U): Long = applyAsLong(t, u)

/**
 * Alias for [ToDoubleBiFunction.applyAsDouble].
 *
 * @receiver the called [ToDoubleBiFunction]
 * @param t first value to apply
 * @param u second value to apply
 * @param T the type of the first value to apply
 * @param U the type of the second value to apply
 * @return [ToDoubleBiFunction.applyAsDouble] result
 */
inline operator fun <T, U> ToDoubleBiFunction<T, U>.invoke(t: T, u: U): Double = applyAsDouble(t, u)

//endregion

//region Predicate

/**
 * Alias for [Predicate.test].
 *
 * @receiver the called [Predicate]
 * @param t value to test
 * @param T the type of the value to test
 * @return [Predicate.test] result
 */
inline operator fun <T> Predicate<T>.invoke(t: T): Boolean = test(t)

/**
 * Alias for [IntPredicate.test].
 *
 * @receiver the called [IntPredicate]
 * @param i value to test
 * @return [IntPredicate.test] result
 */
inline operator fun IntPredicate.invoke(i: Int): Boolean = test(i)

/**
 * Alias for [LongPredicate.test].
 *
 * @receiver the called [LongPredicate]
 * @param l value to test
 * @return [LongPredicate.test] result
 */
inline operator fun LongPredicate.invoke(l: Long): Boolean = test(l)

/**
 * Alias for [DoublePredicate.test].
 *
 * @receiver the called [DoublePredicate]
 * @param d value to test
 * @return [DoublePredicate.test] result
 */
inline operator fun DoublePredicate.invoke(d: Double): Boolean = test(d)

//endregion

//region Supplier

/**
 * Alias for [Supplier.get].
 *
 * @receiver the called [Supplier]
 * @param T the type of the result of the supplier
 * @return [Supplier.get] result
 */
inline operator fun <T> Supplier<T>.invoke(): T = get()

/**
 * Alias for [IntSupplier.getAsInt].
 *
 * @receiver the called [IntSupplier]
 * @return [IntSupplier.getAsInt] result
 */
inline operator fun IntSupplier.invoke(): Int = getAsInt()

/**
 * Alias for [LongSupplier.getAsLong].
 *
 * @receiver the called [LongSupplier]
 * @return [LongSupplier.getAsLong] result
 */
inline operator fun LongSupplier.invoke(): Long = getAsLong()

/**
 * Alias for [DoubleSupplier.getAsDouble].
 *
 * @receiver the called [DoubleSupplier]
 * @return [DoubleSupplier.getAsDouble] result
 */
inline operator fun DoubleSupplier.invoke(): Double = getAsDouble()

//endregion

//region UnaryOperator

/**
 * Alias for [IntUnaryOperator.applyAsInt].
 *
 * @receiver the called [IntUnaryOperator]
 * @param i value to apply
 * @return [IntUnaryOperator.applyAsInt] result
 */
inline operator fun IntUnaryOperator.invoke(i: Int): Int = applyAsInt(i)

/**
 * Alias for [LongUnaryOperator.applyAsLong].
 *
 * @receiver the called [LongUnaryOperator]
 * @param l value to apply
 * @return [LongUnaryOperator.applyAsLong] result
 */
inline operator fun LongUnaryOperator.invoke(l: Long): Long = applyAsLong(l)

/**
 * Alias for [DoubleUnaryOperator.applyAsDouble].
 *
 * @receiver the called [DoubleUnaryOperator]
 * @param d value to apply
 * @return [DoubleUnaryOperator.applyAsDouble] result
 */
inline operator fun DoubleUnaryOperator.invoke(d: Double): Double = applyAsDouble(d)

//endregion

//region BinaryOperator

/**
 * Alias for [IntBinaryOperator.applyAsInt].
 *
 * @receiver the called [IntBinaryOperator]
 * @param i1 first value to apply
 * @param i2 second value to apply
 * @return [IntBinaryOperator.applyAsInt] result
 */
inline operator fun IntBinaryOperator.invoke(i1: Int, i2: Int): Int = applyAsInt(i1, i2)

/**
 * Alias for [LongBinaryOperator.applyAsLong].
 *
 * @receiver the called [LongBinaryOperator]
 * @param l1 first value to apply
 * @param l2 second value to apply
 * @return [LongBinaryOperator.applyAsLong] result
 */
inline operator fun LongBinaryOperator.invoke(l1: Long, l2: Long): Long = applyAsLong(l1, l2)

/**
 * Alias for [DoubleBinaryOperator.applyAsDouble].
 *
 * @receiver the called [DoubleBinaryOperator]
 * @param d1 first value to apply
 * @param d2 second value to apply
 * @return [DoubleBinaryOperator.applyAsDouble] result
 */
inline operator fun DoubleBinaryOperator.invoke(d1: Double, d2: Double): Double = applyAsDouble(d1, d2)

//endregion

//region BiConsumer

/**
 * Alias for [BiPredicate.test].
 *
 * @receiver the called [BiPredicate]
 * @param t1 first value to test
 * @param t2 second value to test
 * @param T the type of the first value to test
 * @param U the type of the second value to test
 * @return [BiPredicate.test] result
 */
inline operator fun <T, U> BiPredicate<T, U>.invoke(t1: T, t2: U): Boolean = test(t1, t2)

//endregion
