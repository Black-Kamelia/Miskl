package com.kamelia.sprinkler.transcoder.binary.decoder.composer

import com.zwendo.restrikt2.annotation.PackagePrivate

@PackagePrivate
internal class ElementsAccumulator {

    private var list = ArrayList<Any?>()

    private var _recursionElements: ArrayDeque<Any?>? = null

    private val recursionElements: ArrayDeque<Any?>
        get() {
            if (_recursionElements == null) _recursionElements = ArrayDeque()
            return _recursionElements!!
        }

    private var currentLayer = DEFAULT_LAYER

    val size: Int
        get() = list.size - currentLayer.start

    val isLastLayer: Boolean
        get() = currentLayer.previous == null

    fun add(element: Any?) {
        list.add(element)
    }

    fun addToRecursion(element: Any?) {
        recursionElements.add(element)
    }

    operator fun get(index: Int): Any? {
        require(index >= 0) { "Index $index is negative" }
        val actualIndex = currentLayer.start + index
        require(actualIndex < list.size) {
            "Index $actualIndex ($index + ${currentLayer.start}) is out of bounds for size ${list.size}"
        }
        return list[actualIndex]
    }

    operator fun set(index: Int, element: Any?) {
        require(index >= 0) { "Index $index is negative" }
        val actualIndex = currentLayer.start + index
        require(actualIndex < list.size) {
            "Index $actualIndex ($index + ${currentLayer.start}) is out of bounds for size ${list.size}"
        }
        list[actualIndex] = element
    }

    fun hasRecursionElement(): Boolean = recursionElements.isNotEmpty()

    fun getFromRecursion(): Any? = recursionElements.removeFirst()

    fun recurse() {
        currentLayer = Layer(list.size, currentLayer)
    }

    fun popRecursion() {
        val previous = currentLayer.previous
        check(previous != null) { "No element to pop" }
        val toPop = list.size - currentLayer.start
        repeat(toPop) { list.removeLast() }
        currentLayer = previous
    }

    fun reset() {
        list = ArrayList()
        _recursionElements = null
        currentLayer = DEFAULT_LAYER
    }

    class Layer(
        @JvmField
        val start: Int,
        @JvmField
        val previous: Layer?,
    )

}
