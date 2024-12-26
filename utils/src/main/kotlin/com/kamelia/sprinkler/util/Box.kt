package com.kamelia.sprinkler.util

/**
 * Represents a box containing a value. This interface aims at providing a way to declare a property with a value that
 * is not available at the time of declaration. Any read access to the [value] property will throw an exception if the
 * value has not been set.
 *
 * It differs from [Lazy] in that the value can be set from outside the class. A typical use case is to declare an
 * object containing a property subject to a circular dependency without having to resort to a mutable property in the
 * class.
 *
 * Example:
 * ```
 * class Foo(intBox: Box<Int>) {
 *
 *     val i by intBox
 *
 * }
 *
 * fun main() {
 *    val box = Box.singleWrite<Int>()
 *    val foo = Foo(box)
 *
 *    runCatching {
 *      println(foo.i) // Throws an exception
 *    }
 *
 *    box.fill(1)
 *    println(foo.i) // Prints 1
 * }
 *
 * ```
 *
 * @param T the type of the value
 * @see Box.Mutable
 */
interface Box<out T> {

    /**
     * The value contained in the box.
     * @throws IllegalStateException if the value has not been set.
     */
    val value: T

    /**
     * Whether the box has been filled.
     */
    val isFilled: Boolean

    /**
     * Delegate operator for the [value] property.
     */
//    @HideFromJava
    operator fun getValue(thisRef: Any?, property: Any?): T = value

    /**
     * Represents a mutable box. It allows to fill the box with a value.
     */
    interface Mutable<T> : Box<T> {

        /**
         * Fills the box with the given value.
         * @param value the value to fill the box with
         * @return true if the box value was set, false otherwise
         */
        fun fill(value: T): Boolean

        /**
         * Delegate operator for the [fill] function.
         */
//        @HideFromJava
        operator fun setValue(thisRef: Any?, property: Any?, value: T) {
            fill(value)
        }

    }

    companion object {

        /**
         * Creates a box that can be filled only once.
         *
         * @param T the type of the value
         * @return a box that can be filled only once
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T> singleWrite(): Mutable<T> = object : Mutable<T> {

            private var _value: T? = null

            override val value: T
                get() = if (isFilled) _value as T else throw IllegalStateException("Box not filled")

            override var isFilled: Boolean = false
                private set

            override fun fill(value: T): Boolean {
                if (isFilled) return false
                _value = value
                isFilled = true
                return true
            }

        }

        /**
         * Creates a box that can be filled multiple times. Any previous value will be overwritten.
         *
         * @param T the type of the value
         * @return a box that can be filled multiple times
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T> rewritable(): Mutable<T> = object : Mutable<T> {

            private var _value: T? = null

            override val value: T
                get() = if (isFilled) _value as T else throw IllegalStateException("Box not filled")

            override var isFilled: Boolean = false
                private set

            override fun fill(value: T): Boolean {
                _value = value
                isFilled = true
                return true
            }

        }

        /**
         * Creates a box that can be filled multiple times. Any previous value will be overwritten.
         *
         * @param T the type of the value
         * @param initial the initial value of the box
         * @return a box that can be filled multiple times
         */
        @JvmStatic
        fun <T> rewritable(initial: T): Mutable<T> = rewritable<T>().apply { fill(initial) }

        /**
         * Creates a box prefilled with the given value. The box will always return the same value.
         *
         * @param T the type of the value
         * @param value the value to fill the box with
         */
        @JvmStatic
        fun <T> prefilled(value: T): Box<T> = object : Box<T> {

            override val value: T = value

            override val isFilled: Boolean
                get() = true

        }

        /**
         * Creates a new [EmptyBox].
         *
         * @param T the type of the value
         * @return a new [EmptyBox]
         */
        @JvmStatic
        fun <T> empty(): Box<T> = EmptyBox

    }

}

private object EmptyBox : Box<Nothing> {

    override val value: Nothing
        get() = throw IllegalStateException("Box is not filled.")

    override val isFilled: Boolean
        get() = false

}
