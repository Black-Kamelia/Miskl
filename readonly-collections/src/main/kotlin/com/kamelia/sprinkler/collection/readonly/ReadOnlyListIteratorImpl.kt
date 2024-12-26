package com.kamelia.sprinkler.collection.readonly

import com.zwendo.restrikt2.annotation.PackagePrivate

@PackagePrivate
internal class ReadOnlyListIteratorImpl<T>(inner: ListIterator<T>) : ReadOnlyListIterator<T>, ListIterator<T> by inner
