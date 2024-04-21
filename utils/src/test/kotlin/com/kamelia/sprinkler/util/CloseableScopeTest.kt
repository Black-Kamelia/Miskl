package com.kamelia.sprinkler.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.Closeable

class CloseableScopeTest {

    open class CloseableMock : Closeable {
        var isClosed = false
        override fun close() {
            isClosed = true
        }
    }

    class NestedCloseableMock : CloseableMock() {
        fun other(): CloseableMock = CloseableMock()
    }

    class ThrowingCloseableMock : CloseableMock() {
        override fun close() {
            super.close()
            throw Exception()
        }

        val index = i++

        class Exception: RuntimeException()

        companion object {
            var i = 0
        }
    }

    @Test
    fun `should close scoped closeables normally`() {
        val closeable1 = CloseableMock()
        lateinit var closeable2: CloseableMock
        lateinit var closeable3: CloseableMock
        lateinit var closeable4: CloseableMock
        lateinit var closeable5: CloseableMock

        closeableScope(closeable1) {
            closeable2 = using(CloseableMock())
            closeable3 = using(CloseableMock())

            closeable5 = NestedCloseableMock()
                .also { closeable4 = it }.usingSelf()
                .other().usingSelf()
        }

        assertTrue(closeable1.isClosed)
        assertTrue(closeable2.isClosed)
        assertTrue(closeable3.isClosed)
        assertTrue(closeable4.isClosed)
        assertTrue(closeable5.isClosed)
    }

    @Test
    fun `should close scoped closeables even if an exception is thrown`() {
        val closeable1 = CloseableMock()
        lateinit var closeable2: CloseableMock
        lateinit var closeable3: CloseableMock
        lateinit var closeable4: CloseableMock
        lateinit var closeable5: CloseableMock

        runCatching {
            closeableScope(closeable1) {
                closeable2 = using(CloseableMock())
                closeable3 = using(CloseableMock())

                closeable5 = NestedCloseableMock()
                    .also { closeable4 = it }.usingSelf()
                    .other().usingSelf()

                throw RuntimeException()
            }
        }

        assertTrue(closeable1.isClosed)
        assertTrue(closeable2.isClosed)
        assertTrue(closeable3.isClosed)
        assertTrue(closeable4.isClosed)
        assertTrue(closeable5.isClosed)
    }

    @Test
    fun `should close scoped closeables even if an exception is thrown in a closeable`() {
        ThrowingCloseableMock.i = 0
        lateinit var closeable1: ThrowingCloseableMock
        lateinit var closeable2: ThrowingCloseableMock

        val res = runCatching {
            closeableScope {
                closeable1 = using(ThrowingCloseableMock())
                closeable2 = using(ThrowingCloseableMock())
            }
        }

        assertTrue(closeable1.isClosed)
        assertTrue(closeable2.isClosed)
        assertEquals(0, closeable1.index)
        assertEquals(1, closeable2.index)
        assertTrue(res.isFailure)
        val exception = res.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is ThrowingCloseableMock.Exception)
        assertEquals(1, exception!!.suppressed.size)
        assertEquals(0, exception.suppressed[0].suppressed.size)
        assertTrue(exception.suppressed[0] is ThrowingCloseableMock.Exception)
    }

    @Test
    fun `should close scoped closeables even if an exception is thrown in a closeable and in the scope and the former should be suppressed under the latter`() {
        ThrowingCloseableMock.i = 0
        lateinit var closeable1: ThrowingCloseableMock
        lateinit var closeable2: ThrowingCloseableMock

        class ScopeException : RuntimeException()

        val res = runCatching {
            closeableScope {
                closeable1 = using(ThrowingCloseableMock())
                closeable2 = using(ThrowingCloseableMock())
                throw ScopeException()
            }
        }

        assertTrue(closeable1.isClosed)
        assertTrue(closeable2.isClosed)
        assertEquals(0, closeable1.index)
        assertEquals(1, closeable2.index)
        assertTrue(res.isFailure)
        val exception = res.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception!! is ScopeException, "Expected ScopeException but got ${exception::class}")
        assertEquals(2, exception.suppressed.size)
        assertEquals(0, exception.suppressed[0].suppressed.size)
        assertEquals(0, exception.suppressed[1].suppressed.size)
        assertTrue(exception.suppressed[0] is ThrowingCloseableMock.Exception)
        assertTrue(exception.suppressed[1] is ThrowingCloseableMock.Exception)
    }

    @Test
    fun _forceCallPrimaryConstructorOfValueClassForCoverage() {
        listOf(CloseableScope())
    }

}
