//package com.kamelia.sprinkler.transcoder.binary.decoder.composer2
//
//import com.kamelia.sprinkler.transcoder.binary.decoder.core.Decoder
//
//sealed interface ComposedDecoderBuilder<E> {
//
//    fun <T> decode(decoderKey: Key<T>): ComposedDecoderBuilder<E>
//
//    fun <T> decode(decoderKey: Class<T>): ComposedDecoderBuilder<E> = decode(Key.Simple(decoderKey))
//
//    fun decodeByte(): ComposedDecoderBuilder<E> = decode(Byte::class.java)
//
//    fun decodeShort(): ComposedDecoderBuilder<E> = decode(Short::class.java)
//
//    fun decodeInt(): ComposedDecoderBuilder<E> = decode(Int::class.java)
//
//    fun decodeLong(): ComposedDecoderBuilder<E> = decode(Long::class.java)
//
//    fun decodeFloat(): ComposedDecoderBuilder<E> = decode(Float::class.java)
//
//    fun decodeDouble(): ComposedDecoderBuilder<E> = decode(Double::class.java)
//
//    fun decodeBoolean(): ComposedDecoderBuilder<E> = decode(Boolean::class.java)
//
//    fun decodeString(): ComposedDecoderBuilder<E> = decode(String::class.java)
//
//    fun <T : Any> decodeNullable(
//        valueDecoderKey: Key<T>,
//        nullabilityDecoderKey: Key<Boolean>,
//    ): ComposedDecoderBuilder<E> = decode(Key.Nullable(valueDecoderKey, nullabilityDecoderKey))
//
//    fun <T : Any> decodeNullable(valueDecoderKey: Key<T>): ComposedDecoderBuilder<E> =
//        decode(Key.Nullable(valueDecoderKey, Key.Simple(Boolean::class.java)))
//
//    fun decodeSelfOrNull(nullabilityDecoderKey: Key<Boolean>): ComposedDecoderBuilder<E> =
//        decodeNullable(Key.Self, nullabilityDecoderKey)
//
//    fun decodeSelfOrNull(): ComposedDecoderBuilder<E> = decodeSelfOrNull(Key.Simple(Boolean::class.java))
//
//    fun build(): Decoder<E>
//
//    companion object {
//
//        @JvmStatic
//        @JvmOverloads
//        fun <T> create(
//            cacheFactory: () -> (Any) -> Decoder<*> = { defaultCache() },
//            contextFactory: () -> ComposedDecoderContext<T>,
//        ): ComposedDecoderBuilder<T> = ComposedDecoderBuilderImpl(contextFactory, cacheFactory)
//
//    }
//
//    sealed interface Key<T> {
//
//        class Simple<T>(internal val clazz: Class<T>) : Key<T> {
//
//            override fun toString(): String = "Key.Class(${clazz.simpleName})"
//
//            override fun equals(other: Any?): Boolean = other is Simple<*> && clazz == other.clazz
//
//            override fun hashCode(): Int = clazz.hashCode()
//
//        }
//
//        class Nullable<T : Any>(
//            internal val valueDecoder: Key<T>,
//            internal val nullabilityDecoder: Key<Boolean>,
//        ) : Key<T> {
//
//            override fun toString(): String = "Key.Nullable(${valueDecoder}, ${nullabilityDecoder})"
//
//            override fun equals(other: Any?): Boolean = other is Nullable<*> && valueDecoder == other.valueDecoder && nullabilityDecoder == other.nullabilityDecoder
//
//            override fun hashCode(): Int = valueDecoder.hashCode() * 31 + nullabilityDecoder.hashCode()
//
//        }
//
//        data object Self : Key<Any>
//
//        class Custom<T> @JvmOverloads constructor(private val label: String? = null) : Key<T> {
//
//            override fun toString(): String = label ?: "Key.Custom"
//
//        }
//
//    }
//
//}
//
