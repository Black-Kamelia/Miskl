package com.kamelia.sprinkler.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Casts this nullable object to the specified type [T]. This method can be used in the context of generic types to
 * cast.
 *
 * @receiver the object to cast or null
 * @return the object cast as [T]
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T> Any?.unsafeCast(): T = this as T

/**
 * Tries to cast this nullable object to the specified type [T].
 * If the cast fails, returns null.
 *
 * @receiver the object to cast or null
 * @return the object cast as [T], or null if the cast fails or if the object is null
 */
inline fun <reified T> Any?.castOrNull(): T? = this as? T

/**
 * Tries to cast this nullable object to the specified type [T]. This method returns null if the object is null, or
 * throws a [ClassCastException] if the cast fails.
 *
 * @receiver the object to cast
 * @return the object cast as [T], or null if the object is null
 * @throws ClassCastException if the cast fails
 */
inline fun <reified T> Any?.castIfNotNull(): T? = this?.let { this as T }

/**
 * Tries to cast this object to the specified type [T]. This method throws a [ClassCastException] if the cast
 * fails, or a [NullPointerException] if the object is null.
 *
 * @receiver the object to cast
 * @return the object cast as [T]
 * @throws ClassCastException if the cast fails
 * @throws NullPointerException if the object is null
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T : Any> Any?.cast(): T {
    contract {
        returns() implies (this@cast is T)
    }
    return this as T
}

/**
 * Checks if this object is an instance of the specified type [T].
 *
 * @receiver the object to check
 * @return true if the object is an instance of [T], false otherwise
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T : Any> Any?.isInstance(): Boolean {
    contract {
        returns(true) implies (this@isInstance is T)
        returns(false) implies (this@isInstance !is T)
    }
    return this is T
}
