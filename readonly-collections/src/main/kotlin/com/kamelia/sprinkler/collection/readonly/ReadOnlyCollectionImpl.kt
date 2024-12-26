package com.kamelia.sprinkler.collection.readonly

import com.zwendo.restrikt2.annotation.PackagePrivate

@PackagePrivate
internal open class ReadOnlyCollectionImpl<E>(
    private val inner: Collection<E>,
) : ReadOnlyCollection<E>, Collection<E> by inner {

    override fun iterator(): ReadOnlyIterator<E> = inner.readOnlyIterator()

    override fun equals(other: Any?): Boolean = inner == other

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()

}
