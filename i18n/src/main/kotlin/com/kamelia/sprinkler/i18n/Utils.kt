@file:PackagePrivate

package com.kamelia.sprinkler.i18n

import com.zwendo.restrikt2.annotation.PackagePrivate
import org.intellij.lang.annotations.Language

/**
 * Here we cannot use `\w` because it includes `_` and we don't want that.
 */
@PackagePrivate
@Language("RegExp")
internal const val IDENTIFIER = """[a-zA-Z\d]+(?:[-_][a-zA-Z\d]+)*"""

@PackagePrivate
internal const val KEY_DOCUMENTATION =
    "For more details about translation keys, see TranslationKey typealias documentation"

@PackagePrivate
internal const val NESTED_KEY_CHAR = '\\'
