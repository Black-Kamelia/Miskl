package com.kamelia.sprinkler.i18n

import com.kamelia.sprinkler.util.assertionFailed
import java.nio.file.Path
import java.util.Locale

fun absoluteResource(vararg path: String): Path {
    require(path.isNotEmpty()) { "Path must not be empty" }
    val prefix = if (path[0].startsWith("/")) "" else "/"
    val actualPath = path.joinToString(separator = "/", prefix = prefix)
    val url = ResourceAccessor::class.java
        .getResource(actualPath)
        ?: assertionFailed("Resource $actualPath not found")
    return Path.of(url.toURI())
}

private sealed interface ResourceAccessor

fun <T> VariableFormatter<T>.format(value: T, locale: Locale, extraArgs: Map<String, String>): String {
    val builder = StringBuilder()
    format(builder, value, locale, extraArgs)
    return builder.toString()
}
