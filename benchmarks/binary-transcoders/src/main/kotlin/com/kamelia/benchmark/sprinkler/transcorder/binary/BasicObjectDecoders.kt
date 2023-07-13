package com.kamelia.benchmark.sprinkler.transcorder.binary

import com.kamelia.sprinkler.transcoder.binary.decoder.IntDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.UTF8StringDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.composer.composedDecoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.Decoder
import com.kamelia.sprinkler.transcoder.binary.decoder.core.DecoderInput


data class BasicPerson(
    val name: String,
    val age: Int,
)

class BasicPersonDecoder : Decoder<BasicPerson> {

    private var name: String? = null
    private var age: Int? = null

    private val stringDecoder = UTF8StringDecoder()
    private val intDecoder = IntDecoder()

    override fun decode(input: DecoderInput): Decoder.State<BasicPerson> {
        while (true) {
            if (name == null) {
                val state = stringDecoder.decode(input)
                if (state.isDone()) {
                    name = state.get()
                } else {
                    return state.mapEmptyState()
                }
            }
            if (age == null) {
                val state = intDecoder.decode(input)
                if (state.isDone()) {
                    age = state.get()
                } else {
                    return state.mapEmptyState()
                }
            }
            return Decoder.State.Done(BasicPerson(name!!, age!!))
        }
    }

    override fun reset() {
        name = null
        age = null
        stringDecoder.reset()
        intDecoder.reset()
    }

}

fun basicPersonDecoder(): Decoder<BasicPerson> = composedDecoder {
    val name = string()
    val age = int()
    BasicPerson(name, age)
}
