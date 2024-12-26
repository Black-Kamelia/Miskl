package com.kamelia.sprinkler.collection.readonly

import com.zwendo.restrikt2.annotation.PackagePrivate

@PackagePrivate
internal class ReadOnlyIterableImpl<T>(private val inner: Iterable<T>) : ReadOnlyIterable<T>, Iterable<T> {

    override fun iterator(): ReadOnlyIterator<T> = inner.readOnlyIterator()

    override fun equals(other: Any?): Boolean = inner == other

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()

}
