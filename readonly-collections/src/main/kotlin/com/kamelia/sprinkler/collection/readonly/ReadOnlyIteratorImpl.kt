package com.kamelia.sprinkler.collection.readonly

import com.zwendo.restrikt2.annotation.PackagePrivate

@PackagePrivate
internal class ReadOnlyIteratorImpl<T>(inner: Iterator<T>) : ReadOnlyIterator<T>, Iterator<T> by inner
