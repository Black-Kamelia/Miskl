# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2.2.0 (2024-03-26)

### Added
 
- Collectors for LinkedHashMap 
([#67](https://github.com/Black-Kamelia/Sprinkler/pull/67/commits/e546a63e410f96c212ba5c0c7c48b5ccd752c04f))

### Changed

- Renamed VariableFormatter default instance's JVM name to be compatible with java (a hidden method with the old name has
been also added for compatibility)
([#67](https://github.com/Black-Kamelia/Sprinkler/pull/67/commits/e546a63e410f96c212ba5c0c7c48b5ccd752c04f))
- Nullable type boxes now work correctly when filled with null
([#67](https://github.com/Black-Kamelia/Sprinkler/pull/67/commits/e546a63e410f96c212ba5c0c7c48b5ccd752c04f))
- Fixed the behavior of `closeableScope` to properly re-throw the exception of the `try` block even when the closing
  process fails too.


## 2.1.0 (2024-02-02)

### Added

- String interpolation API. ([#45](https://github.com/Black-Kamelia/Sprinkler/issues/45))
- Added the `illegalArgument` and `assertionFailed` functions to throw Throwable with a message.
([!47](https://github.com/Black-Kamelia/Sprinkler/pull/47))
- Added `cast` and `castIfNotNull` functions to cast objects

## 2.0.0 (2023-07-31)

### BREAKING CHANGES

#### Removed

- `KotlinDslAdapter` interface has been moved to the `jvm-bridge` module.
([#30](https://github.com/Black-Kamelia/Sprinkler/issues/30))
- `LambdaAdapters`, `InvokeExtensions` and `CollectorExtensions` classes have been moved to the `jvm-bridge` module.
([#40](https://github.com/Black-Kamelia/Sprinkler/issues/40))

### Added

- `castOrNull` extension to cast `Any?` to `T?` if possible.
- `setValue' operator for `Box.Mutable` to allow setting the value through the property syntax.

## 1.0.0 (2023-07-27)

Initial release.
