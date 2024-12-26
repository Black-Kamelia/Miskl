package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.i18n.TranslatorConfiguration.MissingKeyPolicy
import com.kamelia.sprinkler.util.VariableDelimiter
import com.zwendo.restrikt2.annotation.PackagePrivate
import java.util.*

@PackagePrivate
internal class TranslatorData(
    val defaultLocale: Locale,
    val translations: Map<Locale, Map<String, String>>,
    val interpolationDelimiter: VariableDelimiter,
    val pluralMapper: (Locale) -> Plural.Mapper,
    val formatters: (String) -> VariableFormatter<out Any>,
    val missingKeyPolicy: MissingKeyPolicy,
)
