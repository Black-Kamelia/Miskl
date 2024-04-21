@file:HideFromJava("This is a Kotlin only API")

package com.kamelia.sprinkler.util

import com.zwendo.restrikt.annotation.HideFromJava

/**
 * A scope for managing the lifecycle of [AutoCloseable]s.
 */
@JvmInline
value class CloseableScope private constructor(private val closeables: ArrayList<AutoCloseable>) {

    @PublishedApi
    internal constructor() : this(ArrayList())

    /**
     * Adds a [AutoCloseable] to the scope. When the scope ends, this [AutoCloseable] will be closed.
     *
     * @param closeable The [AutoCloseable] to add.
     * @param T The type of the given [AutoCloseable].
     * @return The [AutoCloseable] that was added.
     */
    fun <T : AutoCloseable> using(closeable: T): T = closeable.also(closeables::add)

    /**
     * Adds a [AutoCloseable] to the scope. When the scope ends, this [AutoCloseable] will be closed.
     *
     * @receiver The [AutoCloseable] to add.
     * @param T The type of the received [AutoCloseable].
     * @return The [AutoCloseable] that was added.
     */
    fun <T : AutoCloseable> T.usingSelf(): T = using(this)

    @PublishedApi
    internal fun closeAll(initialException: Throwable?) {
        var exception: Throwable? = initialException
        for (i in closeables.lastIndex downTo 0) {
            try {
                closeables[i].close()
            } catch (e: Throwable) {
                if (exception == null) {
                    exception = e
                } else {
                    exception.addSuppressed(e)
                }
            }
        }
        exception?.let { throw it }
    }

}

/**
 * Creates a [CloseableScope] and adds the given [AutoCloseable]s to it.
 * Within the scope, one can use the [CloseableScope.using] function to add more [AutoCloseable]s.
 * When the scope ends, all registered [AutoCloseable]s will be closed.
 *
 * ```
 * val someCloseable = MyCloseable()
 * val ok = closeableScope(someCloseable) {
 *     val sin = using(System.`in`)
 *     val f = File("someFile")
 *         .inputStream().usingSelf()
 *         .buffered().usingSelf()
 *
 *     // Do stuff with sin and f
 *
 *     true
 * }
 * ```
 *
 * @param closeables The initial [AutoCloseable]s to add to the scope.
 * @param block The block to execute in the scope.
 * @param R The return type of the scope.
 * @return The result of the block.
 */
inline fun <R> closeableScope(vararg closeables: AutoCloseable, block: CloseableScope.() -> R): R {
    val scope = CloseableScope()
    var exception: Throwable? = null
    try {
        closeables.forEach(scope::using)
        return scope.block()
    } catch (e: Throwable) {
        exception = e
    } finally {
        scope.closeAll(exception)
    }
    throw AssertionError("Unreachable code")
}
