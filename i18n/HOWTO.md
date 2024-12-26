# How to (Sprinkler i18n)

## Create a translator

### Filled with different sources

```kt
val translator: Translator = Translator.builder(defaultLocale = Locale.ENGLISH)
    .addMap(Locale.ENGLISH, mapOf("key" to "value"))
    .addMaps(mapOf(Locale.FRENCH to mapOf("key" to "valeur")))
    .addFile(File("path/to/file.yml"))
    .addPath(Path.of("path/to/file.yaml"))
    .addURL(Translator::class.java.getResource("/path/to/file.json"))
    .build()
```

### With a custom configuration

```kt
val translator: Translator = Translator.builder(defaultLocale = Locale.ENGLISH)
    .addMap(Locale.ENGLISH, mapOf("key" to "value [count]"))
    .withConfiguration {
        missingKeyPolicy = MissingKeyPolicy.RETURN_KEY
        interpolationDelimiter = InterpolationDelimiter.create("[", "]")
    }
    .build()
```

## Use a translator

### Translate a key

```kt
val t1: String = translator.t("key", Locale.ENGLISH)
val t2: String = translator.t("path.to.key", Locale.FRENCH)
```

### Fallback on a specific key if the translation is missing

```kt
val t1: String = translator.t("key", Locale.ENGLISH, "fallback.key")
```
