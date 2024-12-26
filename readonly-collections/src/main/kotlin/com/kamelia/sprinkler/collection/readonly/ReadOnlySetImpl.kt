package com.kamelia.sprinkler.collection.readonly

import com.zwendo.restrikt2.annotation.PackagePrivate

@PackagePrivate
internal class ReadOnlySetImpl<E>(
    private val inner: Set<E>,
) : ReadOnlySet<E>, Set<E> by inner {

    override fun iterator(): ReadOnlyIterator<E> = inner.readOnlyIterator()

    override fun equals(other: Any?): Boolean = inner == other

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()

}
