package com.kamelia.sprinkler.collection.readonly

import com.zwendo.restrikt2.annotation.PackagePrivate

@PackagePrivate
internal class ReadOnlyMapImpl<K, V>(private val inner: Map<K, V>) : ReadOnlyMap<K, V>, Map<K, V> by inner {

    override val keys: ReadOnlySet<K>
        get() = inner.keys.asReadOnlySet()

    override val values: ReadOnlyCollection<V>
        get() = inner.values.asReadOnlyCollection()

    override val entries: ReadOnlySet<ReadOnlyMap.Entry<K, V>>
        get() = @Suppress("UNCHECKED_CAST") (ReadOnlyMapEntries(inner.entries) as ReadOnlySet<ReadOnlyMap.Entry<K, V>>)

    override fun equals(other: Any?): Boolean = inner == other

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()

    class Entry<K, V>(private val inner: Map.Entry<K, V>) : ReadOnlyMap.Entry<K, V>, Map.Entry<K, V> by inner {

        override fun equals(other: Any?): Boolean = inner == other

        override fun hashCode(): Int = inner.hashCode()

        override fun toString(): String = inner.toString()

    }

}

private class ReadOnlyMapEntries<K, V>(private val inner: Set<Map.Entry<K, V>>) : ReadOnlySet<Any?> {

    override fun iterator() = object : ReadOnlyIterator<ReadOnlyMap.Entry<K, V>> {

        private var it = inner.iterator()

        override fun hasNext(): Boolean = it.hasNext()

        override fun next(): ReadOnlyMap.Entry<K, V> = ReadOnlyMapImpl.Entry(it.next())

    }

    override val size: Int
        get() = inner.size

    override fun contains(element: Any?): Boolean = inner.contains(element as Any)

    override fun containsAll(elements: Collection<Any?>): Boolean = inner.containsAll(elements)

    override fun isEmpty(): Boolean = inner.isEmpty()

    override fun equals(other: Any?): Boolean = inner == other

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()

}
