package com.kamelia.sprinkler.transcoder.binary.decoder.composer

import com.kamelia.sprinkler.transcoder.binary.decoder.core.Decoder
import com.zwendo.restrikt2.annotation.PackagePrivate

@PackagePrivate
internal object ProcessingMarker : Throwable(null, null, true, false) {

    override fun fillInStackTrace(): Throwable = this

}

@PackagePrivate
internal object RecursionMarker : Throwable(null, null, true, false) {

    override fun fillInStackTrace(): Throwable = this

}

@PackagePrivate
internal class ErrorStateHolder(
    val errorState: Decoder.State.Error
) : Throwable(null, null, false, false) {

    override fun fillInStackTrace(): Throwable = this

}
