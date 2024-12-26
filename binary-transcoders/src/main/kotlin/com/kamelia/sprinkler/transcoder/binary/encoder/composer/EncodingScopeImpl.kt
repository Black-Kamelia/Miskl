package com.kamelia.sprinkler.transcoder.binary.encoder.composer

import com.kamelia.sprinkler.transcoder.binary.encoder.BooleanEncoder
import com.kamelia.sprinkler.transcoder.binary.encoder.ByteEncoder
import com.kamelia.sprinkler.transcoder.binary.encoder.DoubleEncoder
import com.kamelia.sprinkler.transcoder.binary.encoder.FloatEncoder
import com.kamelia.sprinkler.transcoder.binary.encoder.IntEncoder
import com.kamelia.sprinkler.transcoder.binary.encoder.LongEncoder
import com.kamelia.sprinkler.transcoder.binary.encoder.ShortEncoder
import com.kamelia.sprinkler.transcoder.binary.encoder.core.Encoder
import com.kamelia.sprinkler.transcoder.binary.encoder.core.EncoderOutput
import com.kamelia.sprinkler.util.unsafeCast
import com.zwendo.restrikt2.annotation.PackagePrivate
import java.nio.ByteOrder


@PackagePrivate
@Suppress("INAPPLICABLE_JVM_NAME")
internal class EncodingScopeImpl<E>(
    private val output: EncoderOutput,
    private val globalStack: ArrayList<() -> Unit>,
    private val recursionQueue: ArrayDeque<() -> Unit>,
    private val encoderMap: HashMap<Class<*>, Encoder<*>>,
    private val endianness: ByteOrder,
    inner: Encoder<E>,
) : EncodingScope<E> {

    private var selfQueue = ArrayList<() -> Unit>()

    private var recurse = false

    @get:JvmName("self")
    override val self: Encoder<E> = Encoder { obj, output ->
        recursionQueue.add { inner.encode(obj, output) } // add to recursion queue

        // after this point, recursion fields cannot be null
        if (recurse) return@Encoder

        recurse = true // set recursion flag to queue next encodings
        globalStack.add {
            for (i in selfQueue.size - 1 downTo 0) {
                globalStack += selfQueue[i]
            }
        }
    }

    @JvmName("encodeWith")
    override fun <T> encode(obj: T, encoder: Encoder<T>): EncodingScope<E> = apply {
        if (recurse) {
            selfQueue += { encoder.encode(obj, output) }
        } else {
            encoder.encode(obj, output)
        }
    }

    override fun encode(obj: Byte): EncodingScope<E> = encodeWithComputed(obj) { ByteEncoder() }

    override fun encode(obj: Short): EncodingScope<E> = encodeWithComputed(obj) { ShortEncoder(endianness) }

    override fun encode(obj: Int): EncodingScope<E> = encodeWithComputed(obj) { IntEncoder(endianness) }

    override fun encode(obj: Long): EncodingScope<E> = encodeWithComputed(obj) { LongEncoder(endianness) }

    override fun encode(obj: Float): EncodingScope<E> = encodeWithComputed(obj) { FloatEncoder(endianness) }

    override fun encode(obj: Double): EncodingScope<E> = encodeWithComputed(obj) { DoubleEncoder(endianness) }

    override fun encode(obj: Boolean): EncodingScope<E> = encodeWithComputed(obj) { BooleanEncoder() }

    override fun encode(obj: String): EncodingScope<E> = encodeWithComputed(obj) {
        throw AssertionError("A String encoder should always be present")
    }

    override fun encode(obj: Array<E>): EncodingScope<E> = apply {
        encode(obj.size)
        obj.forEach { encode(it, self) }
    }

    override fun encode(obj: Collection<E>): EncodingScope<E> = apply {
        encode(obj.size)
        obj.forEach { encode(it, self) }
    }

    override fun encode(obj: E?): EncodingScope<E> = apply {
        encode(obj != null)
        if (obj != null) {
            encode(obj, self)
        }
    }

    private inline fun <reified T> encodeWithComputed(obj: T, noinline block: () -> Encoder<T>): EncodingScope<E> {
        val encoder = encoderMap.computeIfAbsent(T::class.java) { block() }.unsafeCast<Encoder<T>>()
        return encode(obj, encoder)
    }

}
