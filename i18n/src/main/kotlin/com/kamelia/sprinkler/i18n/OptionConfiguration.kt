package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.bridge.KotlinDslAdapter
import com.kamelia.sprinkler.util.VariableDelimiter
import com.zwendo.restrikt.annotation.PackagePrivate
import java.util.*

class OptionConfiguration @PackagePrivate internal constructor(
    internal val alwaysEnableNestedParsing: Boolean,
    internal val nestingVariableDelimiter: VariableDelimiter,
    internal val interpolationDelimiter: VariableDelimiter,
    internal val pluralMapper: (Locale, Int) -> Options.Plurals,
) {

    companion object {

        inline fun create(block: Builder.() -> Unit): OptionConfiguration = Builder().apply(block).build()

    }

    class Builder @PublishedApi internal constructor() : KotlinDslAdapter {

        var alwaysEnableNestedParsing: Boolean = false

        var nestingVariableDelimiter: VariableDelimiter = VariableDelimiter('[', ']')

        var interpolationDelimiter: VariableDelimiter = VariableDelimiter.DEFAULT

        var pluralMapper: (Locale, Int) -> Options.Plurals = Options.Plurals.Companion::defaultCountMapper

        @PublishedApi
        internal fun build(): OptionConfiguration = OptionConfiguration(
            alwaysEnableNestedParsing,
            nestingVariableDelimiter,
            interpolationDelimiter,
            pluralMapper,
        )

    }

}