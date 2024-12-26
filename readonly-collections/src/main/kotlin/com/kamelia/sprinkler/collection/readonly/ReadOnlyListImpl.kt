package com.kamelia.sprinkler.collection.readonly

import com.zwendo.restrikt2.annotation.PackagePrivate

@PackagePrivate
internal class ReadOnlyListImpl<E>(private val inner: List<E>) : ReadOnlyList<E>, List<E> by inner {

    override fun iterator(): ReadOnlyIterator<E> = inner.readOnlyIterator()

    override fun listIterator(): ReadOnlyListIterator<E> = inner.readOnlyListIterator()

    override fun subList(fromIndex: Int, toIndex: Int): ReadOnlyList<E> = inner.subList(fromIndex, toIndex).asReadOnlyList()

    override fun equals(other: Any?): Boolean = inner == other

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()

}
