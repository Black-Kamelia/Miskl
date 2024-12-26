package com.kamelia.benchmark.sprinkler.transcoder.binary.`object`

import com.kamelia.sprinkler.transcoder.binary.decoder.IntDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.UTF8StringDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.composer.composedDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.Decoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.DecoderInput
import com.kamelia.sprinkler.transcoder.binary.encoder.IntEncoder
import com.kamelia.sprinkler.transcoder.binary.encoder.UTF8StringEncoder
import com.kamelia.sprinkler.transcoder.binary.encoder.composer.composedEncoder
import com.kamelia.sprinkler.transcoder.binary.encoder.core.Encoder


data class BasicPerson(
    val name: String,
    val age: Int,
)

class BasicPersonDecoder : Decoder<BasicPerson> {

    private var name: String? = null
    private var age: Int = 0
    private var index: Int = 0

    private val stringDecoder = UTF8StringDecoder()
    private val intDecoder = IntDecoder()

    override fun decode(input: DecoderInput): Decoder.State<BasicPerson> {
        if (index == 0) {
            val state = stringDecoder.decode(input)
            if (state.isNotDone()) return state.mapEmptyState()
            name = state.get()
            index = 1
        }
        if (index == 1) {
            val state = intDecoder.decode(input)
            if (state.isNotDone()) return state.mapEmptyState()
            age = state.get()
        }
        index = 0
        return Decoder.State.Done(BasicPerson(name!!, age))
    }

    override fun reset() {
        name = null
        index = 0
        stringDecoder.reset()
        intDecoder.reset()
    }

}

fun basicPersonDecoder(): Decoder<BasicPerson> = composedDecoder {
    val name = string()
    val age = int()
    BasicPerson(name, age)
}

fun BasicPersonEncoder(): Encoder<BasicPerson> {
    val stringEncoder = UTF8StringEncoder()
    val intEncoder = IntEncoder()
    return Encoder { obj, output ->
        stringEncoder.encode(obj.name, output)
        intEncoder.encode(obj.age, output)
    }
}

fun basicPersonEncoder(): Encoder<BasicPerson> = composedEncoder {
    encode(it.name)
    encode(it.age)
}
