@file:HideFromJava

package com.kamelia.sprinkler.util

import com.zwendo.restrikt2.annotation.HideFromJava
import java.util.List as JavaList
import java.util.Map as JavaMap
import java.util.Set as JavaSet


fun <T : Any> Collection<T>.toUnmodifiableList(): List<T> = JavaList.copyOf(this)

fun <T : Any> Array<T>.toUnmodifiableList(): List<T> = VarargCopyWorkaround.unmodifiableListOf(this)

fun <T : Any> unmodifiableListOf(): List<T> = JavaList.of()

fun <T : Any> unmodifiableListOf(e1: T): List<T> = JavaList.of(e1)

fun <T : Any> unmodifiableListOf(e1: T, e2: T): List<T> = JavaList.of(e1, e2)

fun <T : Any> unmodifiableListOf(e1: T, e2: T, e3: T): List<T> = JavaList.of(e1, e2, e3)

fun <T : Any> unmodifiableListOf(e1: T, e2: T, e3: T, e4: T): List<T> = JavaList.of(e1, e2, e3, e4)

fun <T : Any> unmodifiableListOf(e1: T, e2: T, e3: T, e4: T, e5: T): List<T> = JavaList.of(e1, e2, e3, e4, e5)

fun <T : Any> unmodifiableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T): List<T> = JavaList.of(e1, e2, e3, e4, e5, e6)

fun <T : Any> unmodifiableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T): List<T> = JavaList.of(e1, e2, e3, e4, e5, e6, e7)

fun <T : Any> unmodifiableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T): List<T> = JavaList.of(e1, e2, e3, e4, e5, e6, e7, e8)

fun <T : Any> unmodifiableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T, e9: T): List<T> = JavaList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9)

fun <T : Any> unmodifiableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T, e9: T, e10: T): List<T> = JavaList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10)

fun <T : Any> unmodifiableListOf(vararg elements: T): List<T> = elements.toUnmodifiableList()



fun <T : Any> Collection<T>.toUnmodifiableSet(): Set<T> = JavaSet.copyOf(this)

fun <T : Any> Array<T>.toUnmodifiableSet(): Set<T> = VarargCopyWorkaround.unmodifiableSetOf(this)

fun <T : Any> unmodifiableSetOf(): Set<T> = JavaSet.of()

fun <T : Any> unmodifiableSetOf(e1: T): Set<T> = JavaSet.of(e1)

fun <T : Any> unmodifiableSetOf(e1: T, e2: T): Set<T> = JavaSet.of(e1, e2)

fun <T : Any> unmodifiableSetOf(e1: T, e2: T, e3: T): Set<T> = JavaSet.of(e1, e2, e3)

fun <T : Any> unmodifiableSetOf(e1: T, e2: T, e3: T, e4: T): Set<T> = JavaSet.of(e1, e2, e3, e4)

fun <T : Any> unmodifiableSetOf(e1: T, e2: T, e3: T, e4: T, e5: T): Set<T> = JavaSet.of(e1, e2, e3, e4, e5)

fun <T : Any> unmodifiableSetOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T): Set<T> = JavaSet.of(e1, e2, e3, e4, e5, e6)

fun <T : Any> unmodifiableSetOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T): Set<T> = JavaSet.of(e1, e2, e3, e4, e5, e6, e7)

fun <T : Any> unmodifiableSetOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T): Set<T> = JavaSet.of(e1, e2, e3, e4, e5, e6, e7, e8)

fun <T : Any> unmodifiableSetOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T, e9: T): Set<T> = JavaSet.of(e1, e2, e3, e4, e5, e6, e7, e8, e9)

fun <T : Any> unmodifiableSetOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T, e9: T, e10: T): Set<T> = JavaSet.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10)

fun <T : Any> unmodifiableSetOf(vararg elements: T): Set<T> = elements.toUnmodifiableSet()



fun <K : Any, V : Any> entryOf(key: K, value: V): Map.Entry<K, V> = JavaMap.entry(key, value)

fun <K : Any, V : Any> Map<K, V>.toUnmodifiableMap(): Map<K, V> = JavaMap.copyOf(this)

fun <K : Any, V : Any> Collection<Pair<K, V>>.toUnmodifiableMap(): Map<K, V> = this.toTypedArray().toUnmodifiableMap()

@JvmName("toUnmodifiableMapFromEntries")
fun <K : Any, V : Any> Collection<Map.Entry<K, V>>.toUnmodifiableMap(): Map<K, V> = this.toTypedArray().toUnmodifiableMap()

fun <K : Any, V : Any> Array<out Pair<K, V>>.toUnmodifiableMap(): Map<K, V> {
    val array = Array<Map.Entry<K, V>>(size) {
        val pair = this[it]
        JavaMap.entry(pair.first, pair.second)
    }
    return array.toUnmodifiableMap()
}

fun <K : Any, V : Any> Array<out Map.Entry<K, V>>.toUnmodifiableMap(): Map<K, V> =
    VarargCopyWorkaround.unmodifiableMapOf(this)

fun <K : Any, V : Any> unmodifiableMapOf(): Map<K, V> = JavaMap.of()

fun <K : Any, V : Any> unmodifiableMapOf(k1: K, v1: V): Map<K, V> = JavaMap.of(k1, v1)

fun <K : Any, V : Any> unmodifiableMapOf(k1: K, v1: V, k2: K, v2: V): Map<K, V> = JavaMap.of(k1, v1, k2, v2)

fun <K : Any, V : Any> unmodifiableMapOf(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V): Map<K, V> = JavaMap.of(k1, v1, k2, v2, k3, v3)

fun <K : Any, V : Any> unmodifiableMapOf(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V): Map<K, V> = JavaMap.of(k1, v1, k2, v2, k3, v3, k4, v4)

fun <K : Any, V : Any> unmodifiableMapOf(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V): Map<K, V> = JavaMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5)

fun <K : Any, V : Any> unmodifiableMapOf(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V, k6: K, v6: V): Map<K, V> = JavaMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6)

fun <K : Any, V : Any> unmodifiableMapOf(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V, k6: K, v6: V, k7: K, v7: V): Map<K, V> = JavaMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7)

fun <K : Any, V : Any> unmodifiableMapOf(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V, k6: K, v6: V, k7: K, v7: V, k8: K, v8: V): Map<K, V> = JavaMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8)

fun <K : Any, V : Any> unmodifiableMapOf(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V, k6: K, v6: V, k7: K, v7: V, k8: K, v8: V, k9: K, v9: V): Map<K, V> = JavaMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9)

fun <K : Any, V : Any> unmodifiableMapOf(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V, k6: K, v6: V, k7: K, v7: V, k8: K, v8: V, k9: K, v9: V, k10: K, v10: V): Map<K, V> = JavaMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10)

fun <K : Any, V : Any> unmodifiableMapOf(vararg pairs: Pair<K, V>): Map<K, V> = pairs.toUnmodifiableMap()

fun <K : Any, V : Any> unmodifiableMapOf(vararg entries: Map.Entry<K, V>): Map<K, V> =
    VarargCopyWorkaround.unmodifiableMapOf(entries)

