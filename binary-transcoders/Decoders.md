# Decoders

Decoders serve the purpose of deserializing bytes into specific objects. They are the opposite of Encoders, and differ
from them in the way that they are stateful. Indeed, they are intended to decode streams of bytes that are not
necessarily given contiguously, as in, for example, on a network communication. Therefore, a Decoder should not be used
on different streams of bytes, as it will keep its state between calls.

We will see that the API provides building blocks to create decoders for any type of data, and compose them together
to create more complex decoders very easily.

For simplification and readability purposes, every class name referenced in the following sections will not be written
in their fqname form, but rather in their simple name form. For example,
`com.kamelia.sprinkler.transcoder.binary.decoder.Decoder` will be written as `Decoder`. The same goes for all classes in
the package `com.kamelia.sprinkler.transcoder.binary.decoder`.

## Summary

- [Main Interface](#main-interfaces)
    - [Decoder](#decoder)
    - [DecoderInput](#decoderinput)
- [Provided Decoders](#provided-decoders)
    - [Core Decoders](#core-decoders)
        - [ConstantSizedItemDecoder](#ConstantSizedItemDecoder)
        - [PrefixedSizeItemDecoder](#PrefixedSizeItemDecoder)
        - [MarkerEndedItemDecoder](#markerendeditemdecoder)
        - [ConstantArityReductionDecoder](#constantarityreductiondecoder)
        - [PrefixedArityReductionDecoder](#prefixedarityreductiondecoder)
        - [MarkerEndedReductionDecoder](#MarkerEndedReductionDecoder)
        - [NothingDecoder](#nothingdecoder)
    - [Base Decoders](#base-decoders)
        - [Primitive Decoders](#primitive-decoders)
        - [String Decoders](#string-decoders)
        - [Enum Decoders](#enum-decoders)
        - [Constant Decoders](#constant-decoders)
    - [Common Decoders](#common-decoders)
- [Decoder Mappers](#decoder-mappers)
    - [mapTo](#mapto)
    - [mapResult](#mapresult)
    - [mapState](#mapstate)
    - [toCollection, toMap and toArray](#collections-maps-and-arrays)
    - [toOptional](#tooptional)
- [Decoder Composition](#decoder-composition)
    - [DecodingScope interface](#decodingscope-interface)
    - [Scope usage](#scope-usage)
        - [Examples](#examples)
    - [Implementation, performances and advices](#implementation-performances-and-advices)
        - [Implementation](#implementation)
        - [Performances](#performances)
        - [Advices](#advices)
- [Complete Example](#complete-example)

## Main interfaces

The two main bricks of this API are the `Decoder` and `DecoderInput` interfaces.

They are used to define the behavior of the decoder and how to feed it the encoded data, respectively.
While they have several methods, only a few need to actually be implemented in each of them, because others have
default implementations depending on them.

Along with them, a [monadic](https://en.wikipedia.org/wiki/Monad_(functional_programming)) result type is also provided
to ensure that partial decoding is possible, in the form of `Decoder.State`.

### Decoder

A `Decoder<T>` should at least implement the following two methods: `decode(input: DecoderInput)` and `reset()`. The
`decode` method is the main brick of the API, and is used to define the behavior of the decoder. It should deserialize
the bytes coming from the `input` and return a `Decoder.State`. The `reset` method is used to reset the state of the
decoder; However, the decoder automatically should reset itself whenever it fully decodes an object (the method is
useful when an error occurs, or when the decoding must be interrupted).

Here is an example of a decoder:

```kt
class MyBytePair(val first: Byte, val second: Byte)

fun myDecoder(): Decoder<MyBytePair> = object : Decoder<MyBytePair> {
    private var first: Byte? = null

    override fun decode(input: DecoderInput): Decoder.State<MyBytePair> {
        if (first == null) {                                            // if the first byte has not been read yet
            val byte: Int = input.read()                                // read the first byte
            if (byte == -1) return Decoder.State.Processing             // if the input is empty, return Processing
            first = byte.toByte()                                       // otherwise, store the byte
        }

        val second: Int = input.read()                                  // read the second byte
        if (second == -1) return Decoder.State.Processing               // if the input is empty, return Processing
        reset()                                                         // otherwise, reset the state of the decoder
        return Decoder.State.Done(MyBytePair(first!!, second.toByte())) // and return the decoded object
    }

    override fun reset() {
        first = null
    }

}
```

This decoder simply reads two bytes from the input and assembles them into a `MyBytePair` object. This example
illustrates the stateful aspect of decoders, as the first byte is stored in the decoder until the second byte is read.
It also shows the decoder automatically resets itself when it fully decodes an object. The `DecoderInput` is an
abstraction to represent the stream of bytes to decode (see the next section for more details).

The previously defined decoder can be used as follows:

```kt
val input: DecoderInput = MyDecoderInput()
val myPair: MyBytePair = myDecoder().decode(input).get() // decode the input and get the result
```

(We will expand on the `DecoderInput` interface in the next section)

You can notice the `get()` method call on the result of the decoding. This is because the `decode` method returns a
`Decoder.State`, which is a [monadic](https://en.wikipedia.org/wiki/Monad_(functional_programming)) type that represents
the result of the decoding.

It can be either:

- `Decoder.State.Done`, which means that the decoder has fully decoded the object and returns it (the instance contains
  the decoded object) ;
- `Decoder.State.Processing`, which means that the decoder needs more bytes to fully decode the object ;
- `Decoder.State.Error`, which means that an error occurred during the decoding (the instance contains the error).

It offers several methods to perform different operations on the return object:

- Unwrap operations, through `get` , `getOrNull`, `getOrElse` or `getOrThrow` ;
- Bind operations, through `mapState`, `mapResult` or `mapEmptyState` ;
- Utility operations, through `isDone`, `isNotDone`, `ifDone`, `ifError`, etc.

### DecoderInput

The `DecoderInput` is an abstraction that serves to map the behavior of an object to that of something similar
to an `InputStream`. It is used to feed the `Decoder` with the encoded data, and is passed to the `Decoder` when
decoding.

The methods that need to be implemented are `read()` and `readBit()`.
The `read` method reads a single byte from the input and returns it as an `Int`. If the input is empty, it returns `-1`.
The `readBit` method reads a single bit from the input and returns it as an `Int`. If the input is empty, it
returns `-1`.

However, often, one needs quite a bit more than just these two methods to feed the `Decoder` with the encoded data
in an efficient way (reading whole bytes, or even groups of bytes, instead of relying on the slow default
implementation). To that effect, there are several sensible factories to create `DecoderInput`s.

Indeed, say we want to read the encoded data from the natural integers sequence, here's an example of how we could do
it:

```kt
var value: Byte = 0.toByte()
val stdinDecoderInput: DecoderInput = DecoderInput.from { value++ } // will return 0, 1, 2, ...

stdinDecoderInput.read()    // returns 0
stdinDecoderInput.read()    // returns 1
stdinDecoderInput.readBit() // returns 0 (the most significant bit of 2 = 0000_0010)
```

(Obviously, this is not the most realistic implementation, but it is just an example.)

The `from` function above takes in a function which returns a `Byte` as an argument. There are also several other helper
`from` methods, such as ones which take in an `InputStream`, a `ByteBuffer`, or a `ByteArray` as an argument.

```kt
val input1: DecoderInput = DecoderInput.from(System.`in`) // from(InputStream)
val input2: DecoderInput = DecoderInput.from(ByteBuffer.allocate(10)) // from(ByteBuffer)
val input3: DecoderInput = DecoderInput.from(byteArrayOf(1, 2, 3)) // from(ByteArray)
```

Note that there is a third factory to create a `DecoderInput`, which is `DecoderInput::nullInput`. It returns a
`DecoderInput` which never reads from anything. It is a no-op, and is useful for testing purposes, for example.

## Provided Decoders

This library provides a lot of essential "atomic" decoders, which are used to decode most of the basic types, and some
more complex but common ones, as well as factories to create them easily.

### Core Decoders

The core decoders implement and factorize the different ways to interpret a sequence of bytes; that is to say, for
example, how to decode an arbitrary number of bytes from a sequence, a constant number of bytes from the sequence, etc.
This API offers relatively low-level decoders, which are used to create more complex ones. It is usually not necessary
to use them directly, but they are provided for completeness.

#### ConstantSizedItemDecoder

The `ConstantSizedItemDecoder` is used to decode a sequence of bytes of a constant size. It
is created from the number of bytes to read, and a function to convert the read bytes to the desired object.

Here is an example of a decoder that decodes a single byte:

```kt
val byteDecoder: Decoder<Byte> = ConstantSizedItemDecoder(
    byteSize = 1,
    converter = { get(0) } // this: ByteArray
)
```

#### PrefixedSizeItemDecoder

The `PrefixedSizeItemDecoder` decoder is used to decode a sequence of bytes of a variable size. It first reads a prefix
of a constant size which represents the size of the sequence to read, and then reads the sequence itself. It is created
from a decoder that decodes a `Number` and a function to convert the read bytes to the desired object.

The following example shows an implementation of a decoder of `String` objects using the `PrefixedSizeItemDecoder` (it
first decodes the size of the string represented as a `Byte`, and then reads the string itself).

```kt
val stringDecoder: Decoder<String> = PrefixedSizeItemDecoder(
    sizeDecoder = byteDecoder, // the previously defined byte decoder
    converter = { decodeToString() } // this: ByteArray, it: Int (size)
)
```

#### MarkerEndedItemDecoder

The `MarkerEndedItemDecoder` decoder is used to decode a sequence of bytes of a variable size, which is ended by a
specific byte. It is created from an end marker (a `ByteArray`) and a function to convert the read bytes to the desired
object.

The following example shows an implementation of a decoder of `String` objects using the `MarkerEndedItemDecoder` (it
stops decoding when it reads a `0` byte).

```kt
val stringDecoder: Decoder<String> = MarkerEndedItemDecoder(
    endMarker = byteArrayOf(0), // the end marker
    converter = { decodeToString() } // this: ByteArray, it: Int (size)
)
```

#### ConstantArityReductionDecoder

The `ConstantArityReductionDecoder` decoder is used to decode the same object a constant number of times. It is created
from the number of times to decode the object, a decoder that decodes the objects and a `Collector` to collect the
decoded objects.

```kt
val byteListDecoder: Decoder<List<Byte>> = ConstantArityReductionDecoder(
    collector = Collectors.toList(),
    elementDecoder = byteDecoder, // the previously defined byte decoder
    arity = 3,
)
```

#### PrefixedArityReductionDecoder

The `PrefixedArityReductionDecoder` decoder is used to decode the same object a variable number of times. It is created
from a decoder that decodes the number of times to decode the objects, a decoder that decodes the object and a
`Collector` to collect the decoded objects.

```kt
val byteListDecoder: Decoder<List<Byte>> = PrefixedArityReductionDecoder(
    collector = Collectors.toList(),
    elementDecoder = byteDecoder, // the previously defined byte decoder
    sizeDecoder = byteDecoder,    // the previously defined byte decoder
)
```

#### MarkerEndedReductionDecoder

The `MarkerEndedReductionDecoder` decoder is used to decode the same object a variable number of times, until a
specific object is read. It is created from a decoder that decodes the objects, a function that tests the decoded
elements and returns whether the decoding should stop, a boolean that indicates whether the end marker should be
included in the decoded objects, and a `Collector` to collect the decoded objects.

The following example shows an implementation of a decoder of `List<Byte>` objects using the
`MarkerEndedReductionDecoder` (it stops decoding when it reads a `0` byte).

```kt 
val byteListDecoder: Decoder<List<Byte>> = MarkerEndedReductionDecoder(
    collector = Collectors.toList(),
    elementDecoder = byteDecoder, // the previously defined byte decoder
    keepLast = false,
    shouldStop = { it == 0 }, // the end marker
)
```

#### NothingDecoder

The `NothingDecoder` decoder is a special decoder that always returns `Decoder.State.Error`. It is useful when one
needs a decoder of a specific type and does not have one. The class offers several constructors to create a decoder
using a specific error message, a specific exception or a factory function.

```kt
val nothingDecoder: Decoder<Nothing> = NothingDecoder("This does not work")

fun foo(decoder: Decoder<Int>) {
    // does some stuff
}

foo(nothingDecoder) // valid, we can test the behavior of the function without having an actual decoder
```

### Base Decoders

Base decoders are the most basic decoders, and decode all the primitive types, as well as enum variants, and even
`String`s from a chosen encoding.

#### Primitive Decoders

The ones corresponding to the most common types decode the JVM primitive types.

Indeed, the provided factories of primitive decoders are :

- `ByteDecoder`
- `ShortDecoder`
- `IntDecoder`
- `LongDecoder`
- `FloatDecoder`
- `DoubleDecoder`
- `BooleanDecoder`

All of those, except for `ByteDecoder` and `BooleanDecoder`, accept a `ByteOrder` as an argument, which is used to
determine the byte order (endianness) of the encoded data. If you do not provide one, it will default to
`ByteOrder.BIG_ENDIAN`.

#### String Decoders

In binary decoding in general, there are two major ways to decode strings of text (aside from the charset):

- Fixed-length decoding, where the length of the string is known beforehand, and is decoded before the string itself.
- Variable-length decoding, where the length of the string is not known beforehand, in which case, a terminator flag
  (or, end marker) is decoded after the string to indicate the end of the string, in the same vein as C-style strings,
  which are null-terminated.

To that effect, the two main string decoder factories are two overloads of `StringDecoder`, both of them accept a
`Charset`, and one also takes a `Decoder<Int>` to decode the length of the string, while the other one uses a
`ByteArray` corresponding to the end marker.

For example:

```kt
val utf8PrefixedLengthDecoder: Decoder<String> = StringDecoder(Charsets.UTF_8, IntDecoder())
val asciiNullTerminatedDecoder: Decoder<String> = StringDecoder(Charsets.US_ASCII, byteArrayOf(0))
```

Fortunately, there are also some predefined factories for the most common charsets, which are:

- `UTF8StringDecoder`
- `UTF16StringDecoder`
- `ASCIIStringDecoder`
- `Latin1StringDecoder`
- `UTF8StringDecoderEM` (EM stands for End Marker)
- `UTF16StringDecoderEM` (EM stands for End Marker)
- `ASCIIStringDecoderEM` (EM stands for End Marker)
- `Latin1StringDecoderEM` (EM stands for End Marker)

They have sensible defaults for the length decoder, and the end marker, and are the recommended way to decode strings.

#### Enum Decoders

Enum decoders are used to decode enum variants. They are very simple, and come in two flavors:

- `EnumDecoder` which decodes the enum variant from its ordinal.
- `EnumDecoderString` which decodes the enum variant from its name.

#### Constant Decoders

Constant decoders are used to decode a constant value. The two base factories to create them accept either a constant
value that will be decoded, or a function that returns the constant value.

For example:

```kt
val constantDecoder: Decoder<Int> = ConstantDecoder(42)
val nowDecoder: Decoder<Long> = ConstantDecoder { System.currentTimeMillis() }
```

There also are two predefined factories for the most common constant decoders, which are:

- `NullDecoder` which always decodes `null` ;
- `UnitDecoder` which always decodes `Unit`.

### Common Decoders

Common decoders decode more complex types that are commonly used, and are composed of other decoders.

Here is a list of the provided common decoders:

- `UUIDDecoder` which decodes a `UUID` from two `Long`s.
- `UUIDStringDecoder` which decodes a `UUID` from a `String` using the `UUID.fromString()` method.
- `PairDecoder<T, U>` which composes two decoders (of `T` and `U`) to decode a `Pair<T, U>`.
- Time related decoders :
    - `InstantDecoder`
    - `LocalTimeDecoder`
    - `LocalDateDecoder`
    - `LocalDateTimeDecoder`
    - `DateDecoder`
    - `ZoneIdDecoder`
    - `ZonedDateTimeDecoder`

## Decoder Mappers

Often times, one will want to decode a derived type from a base type, or a collection of a type we already have a
decoder for. To that effect, there are several decoder mappers that can be used to map a decoder to another type.

### mapTo

The first one is `Decoder<T>.mapTo(mapper: (T) -> Decoder<R>): Decoder<R>`. It is used to map a decoder to another type
using a function that takes in the decoded object and returns a decoder for the desired type.

Say we want, for example, a decoder that decodes all types of integers, by first decoding a byte the number of bytes to
read, and then reading the bytes themselves. We could use the `mapTo` method to map the decoder to a `Decoder<Number>`:

```kt
val numberDecoder: Decoder<Number> = ByteDecoder().mapTo {
    when (it.toInt()) {
        1 -> ByteDecoder()
        2 -> ShortDecoder()
        4 -> IntDecoder()
        8 -> LongDecoder()
        else -> NothingDecoder("Invalid number of bytes $it")
    }
}
```

Note that in this example, it could be more efficient to instantiate the decoders beforehand, and use a `when` clause
to return the correct decoder, instead of creating a new decoder every time.

### mapResult

The `Decoder<T>.mapResult(mapper: (T) -> R): Decoder<R>` to create a decoder that will map the decoded object to
another type using a function before returning it. It is useful when one wants to decode a type that is not the same
as the decoded object, but has the same representation.

Imagine for example a decoder that decodes `Int` between 0 and 255, meaning that they can be serialized as a single
byte. We could use the `mapResult` to create a decoder that decodes `Int` stored on 1 byte:

```kt
val intDecoder: Decoder<Int> = ByteDecoder().mapResult { it.toInt() }
```

### mapState

`Decoder<T>.mapState(mapper: (T) -> Decoder.State<R>): Decoder<R>` is used to create a new decoder that
will map the result object `T` of the original decoder to a `Decoder.State<R>` using a mapper. This function
offers slightly more flexibility than `Decoder::mapResult`, as it allows to return a `Decoder.State` instead of a
simple object, which can be useful when one wants to return a `Decoder.State.Error` for example. It therefore allows
to validate the decoded object before returning it.

Here is an example of an enum decoder that decodes a `String` and returns the corresponding enum variant, or an error
if the string does not correspond to any variant:

```kt
enum class MyEnum { A, B, C }

val myEnumDecoder: Decoder<MyEnum> = UTF8StringDecoder().mapState {
    try {
        Decoder.State.Done(MyEnum.valueOf(it))
    } catch (e: IllegalArgumentException) {
        Decoder.State.Error("Unknown enum variant $it")
    }
}
```

### Collections, Maps and Arrays

Sometimes, one will have a decoder for a type, and will want to decode a collection, map, or array of that type. To that
effect, there are several mapper functions to create decoders for collections, maps, and arrays.

The base mapper, `toCollection`, uses a `Collector` to determine how to collect the decoded objects. It exists in three
variants:

- `toCollection(collector: Collector<T, C, R>, sizeDecoder: Decoder<Number>): Decoder<R>`, which takes in, in addition
  to the collector, a decoder that decodes the size of the collection.
- `toCollection(collector: Collector<T, C, R>, size: Int): Decoder<R>`, where the size of the collection is known
  beforehand.
- `toCollection(collector: Collector<T, C, R>, keepLast: Boolean, shouldStop: (T) -> Boolean): Decoder<R>`, which
  accepts a function that takes each decoded object and returns whether the decoding should stop. It also accepts a
  boolean that indicates whether the last decoded object should be included in the collection.

From this base mapper, there are also several predefined mappers for the most common collections (all predefined mappers
exist in the three previously mentioned variants):

- `toList` which decodes a `List<T>` ;
- `toSet` which decodes a `Set<T>` ;
- `toArrayList` which decodes an `ArrayList<T>` ;
- `toMap` which decodes a `Map<K, V>`.

### toOptional

`toOptional` transforms a decoder of `T` to a decoder of `T?` using a prefixed encoded boolean to determine the
presence of the value. The nullability is expected to be encoded with a single byte prefixed to the encoded value,
where `0` means that the value is not present, and any other value means that the value is present.

```kt
val booleanDecoder: Decoder<Boolean> = BooleanDecoder()
val nullableBooleanDecoder: Decoder<Boolean?> = booleanDecoder.toOptional()
```

Note that in the Java world, there is no difference in the type itself, so one may need to name or document the decoder
properly to clear the potential ambiguity.

## Decoder Composition

The heart and goal of this library is to provide a way to compose atomic decoders together to create more complex
decoders, which can in turn be used to compose even more complex decoders, and so on.

For that, the API provides a neat DSL to compose decoders together, and even allow easy decoding of recursive data
structures (a data structure which contains a nullable reference to its own type, or a collection of objects of its own
type).

### DecodingScope interface

The composition API takes shape through the `DecodingScope` interface. This interface simplifies the decoding of an
object by decoding each of the different components required to create the object. It also allows decoding objects with
a recursive structure, that is to say, an object which contains a reference to its own type, nullable or not, or even in
a collection. For example, a linked list node is a recursive data structure.

To decode an object using a `DecodingScope`, the interface has a `decode(decoder: Decoder<T>): T` method. This method
is the main entry point for decoding and is the foundation of the interface as a whole. It enables the decoding of any
type of object, provided an appropriate decoder is given for that purpose.

However, the main advantage of the `DecodingScope` does not lie in this method but rather in the overloads provided by
the interface. These overloads allow the decoding of basic types (`Int`, `Long`, `String`, etc.). The benefit is that
the way these objects are decoded is determined by the scope itself, in particular, by its implementation.

It is thus possible, using the `composedDecoder` method (which creates a decoder from a `DecodingScope` and will be
presented in the next section in more details), to write the following code (note that the scope is passed as the
receiver object):

```kt
class Person(val name: String, val age: Int)

val decoder: Decoder<Person> = composedDecoder<Person> { // this: DecodingScope<Person>
    val name: String = string()
    val age: Int = int()

    Person(name, age)
}
```

As explained earlier, the interface also allows the encoding of recursive structures. This functionality is achieved
using the decoder accessible through the `self` property, which is a special decoder that can decode an object of the
same type as the scope to which it belongs to, by repeating the same calls as those made on the scope itself.

Thus, it is possible to recursively decode a linked list as follows:

```kt
class Node(val value: Int, val next: Node?)

val decoder: Decoder<Node> = composedDecoder<Node> { // this: DecodingScope<Node>
    val value = int()         // DecodingScope.decode(Int)
    val hasNext = boolean()   // DecodingScope.decode(Boolean) / check if it is the last node
    val next = if (hasNext) { // if it is not the last node
        decode(self)          // DecodingScope.decode(Node) // DecodingScope.self / decode the next node recursively
    } else {
        null // don't decode anymore
    }
    Node(value, next) // build the current node
}
```

Similarly to the interface's overloads for decoding basic types, it also provides overloads to decode common recursive
cases, such as:

- `selfOrNull(): E?` for decoding optional recursion (like in the example above)
- `selfCollection(Collector<E, *, R>): R` for decoding recursion represented by a collection
- `selfList(): List<E>` which is an alias for `selfCollection(toList())`
- `selfSet(): Set<E>` which is an alias for `selfCollection(toSet())`
- `selfArray((Int) -> Array<E?>): Array<E>` which is an alias for `selfCollection(ExtendedCollectors.toArray(factory))`
- `selfCollectionOrNull(Collector<E, *, R>): R?` for decoding optional recursion represented by a collection
- `selfListOrNull(): List<E>?` which is an alias for `selfCollectionOrNull(toList())`
- `selfSetOrNull(): Set<E>?` which is an alias for `selfCollectionOrNull(toSet())`
- `selfArrayOrNull((Int) -> Array<E?>): Array<E>?` which is an alias for
  `selfCollectionOrNull(ExtendedCollectors.toArray(factory))`
- `self(): E` for encoding direct recursion ; this method needs to be used within a conditional block to avoir infinite
  recursion.

> **NOTE**: The self decoder is not implemented as a "typical" decoder and must be used with caution.
> Any usage that does not adhere to the instructions provided in the documentation may result in unexpected behavior.

Moreover, there are way to perform complex conditional decoding cases in an elegant manner thanks to some methods
provided by the `DecodingScope`:

The `objectScoped(() -> T): T` method executes the given function only once per scope, that is to say, in the case of
recursive encoding, any subsequent call to this method will return the cached result for the same object.

It is mainly used to create a derived decoder using the `self` decoder, as it is not possible to declare it before the
scope, and because it must be cached in the case where decoding cannot be done in a single pass (to keep track of the
partial data between calls).

Here is an example of how to use it when creating a custom decoder using `self`:

```kt
class Box<T>(val value: T)
class Person(val name: String, val fatherBox: Box<Person?>)

val personDecoder: Decoder<Person> = composedDecoder<Person> { // this: DecodingScope<Person>
    val name: String = string()

    val fatherBoxDecoder: Decoder<Box<Person?>> = objectScoped { self.toOptional().mapResult { Box(it) } }
    val fatherBox: Box<Person?> = decode(fatherBoxDecoder)

    Person(name, fatherBox)
}
```

The API also provides the `skip(Long)` to skip a given amount of bytes. Useful when the format of the encoded object
contains data that is not needed for the decoding process.

```kt
class Person(val name: String, val age: Int)

val personDecoder: Decoder<Person> = composedDecoder<Person> { // this: DecodingScope<Person>
    val name: String = string()
    skip(Double.SIZE_BYTES * 2) // skips two doubles representing the latitude and longitude of the person's location
    val age: Int = int()

    Person(name, age)
}
```

The available method is `errorState(Decoder.State.Error): Nothing`. It stops the decoding process and returns the given
error state for the current decoder. This method allows to return an error state from the composed decoder.

The following example shows how to use it to return an error state when the age of the person is negative:

```kt
class Person(val name: String, val age: Int)

val personDecoder: Decoder<Person> = composedDecoder<Person> { // this: DecodingScope<Person>
    val name: String = string()
    val age: Int = int()

    if (age < 0) {
        errorState(Decoder.State.Error("Age cannot be negative"))
    }

    Person(name, age)
}
```

### Scope usage

As of now, the `DecodingScope` interface is used through the `composedDecoder` top level function. This function allows
one to declare and define a sequence of instructions to apply to the `DecodingScope` (the order of the scope's method
calls is significant and determines the order of reads to the `DecoderOutput`), in order to decode an object of the
given type.

The scope provided to the user and on which the decoding method calls can be made is an implementation using overloads
based on the basic decoders of the library (which decode the basic types mentioned previously: `Int`, `Long`, `String`,
etc.)

Note that it is also possible to specify the endianness of the number `Decoder`s for the entire scope, as shown below:

```kt
class Person(val name: String, val age: Int)

val decoder: Decoder<Person> = composedDecoder<Person>(
    ByteOrder.LITTLE_ORDER // Int, Long, Float and Double will be interpreted in little endian
) { // this: DecodingScope<Person>
    val name: String = string()
    val age: Int = int() // interpreted in little endian

    Person(name, age)
}
```

#### Examples

This section shows some examples of the use of the `composedDecoder` function.

- Simple object decoding

```kt
class Enemy(val name: String, var hp: Int)

val enemyDecoder: Decoder<Enemy> = composedDecoder<Enemy> { // this: DecodingScope<Node>
    val name: String = string()
    val hp: Int = int()

    Enemy(name, hp)
}
```

- Recursive decoding: deserializing to a binary tree

```kt
sealed interface Node {
    val value: Int

    class Inner(override val value: Int, val left: Node, val right: Node)
    class Leaf(override val value: Int)
}

val decoder: Decoder<Node> = composedDecoder<Node> { // this: DecodingScope<Node>
    val isLeaf: Boolean = boolean()
    if (isLeaf) {
        val value: Int = int()

        Node.Leaf(value)
    } else {
        val value: Int = int()
        val left: Node = self()
        val right: Node = self()

        Node.Inner(value, left, right)
    }
}
```

### Implementation, performances and advices

#### Implementation

One of the big specificities of the implementation of the decoder composition is that it is done using exceptions. You
may have noticed that inside the `composedDecoder` function, the `decode` method and all its shorthand methods always
return the actual object, and never a `Decoder.State` in case of missing bytes or error. This is because the method
will throw an exception if the actual `Decoder.decode` call does not return a `Decoder.State.Done` value. This exception
will then be caught by the `composedDecoder` function, which will then return the `Decoder.State` value to the user.

```kt
class Person(val name: String, val age: Int)

val myDecoder: Decoder<Person> = composedDecoder<Person> { // this: DecodingScope<Person>
    val name: String = string()
    val age: Int = int()

    Person(name, age)
}

val input: DecoderInput = DecoderInput.nullInput()
val result: Decoder.State<Person> = myDecoder.decode(input)
```

In the above example, let's say that the `string` method call cannot fully read the string because there are not enough
bytes yet in the `DecoderOutput` to do so. The steps of the decoding process will be the following:

<div align="center">

![Decoding process diagram](./assets/img/bintrans_exceptions.dark.svg#gh-dark-mode-only)
![Decoding process diagram](./assets/img/bintrans_exceptions.light.svg#gh-light-mode-only)

</div>

Other parts of the implementation of the scope also rely on exceptions, such as the return of `Decoder.State.Error`
values from the underlying decoders, recursive decoding or the `errorState` method call.

This design choice allows users to write their decoders in a more imperative way, without having to worry about the
`Decoder.State` values returned by the `decode` method calls. However, this design choice has a cost in terms of
performances, a topic that will be discussed in the next section.

#### Performances

The use of exceptions in the implementation of the decoder composition has a cost in terms of performances. Despite the
small optimizations that have been made (use of singleton exceptions, reduction of allocations between the `decode`
method calls, etc.), the performances of the decoder created through composition are significantly lower than those of
a "handmade" decoder (a class implementing the `Decoder` interface).

However, the performance offered by the decoder composition is still, in most cases, sufficient for the decoding of
binary data. The following table shows the results of a benchmark comparing the performances of a handmade decoder and a
decoder created through composition (benchmarks done using jmh) (higher is better):

|    cases / ops per ms    | Composition |  Handmade  |
|:------------------------:|:-----------:|:----------:|
|  ByteArray single step   |  8 533.491  | 48 436.599 |
| ByteArray several steps  |   476.447   | 48 895.678 |
|  ByteBuffer single step  |  7 410.855  | 50 410.224 |
| ByteBuffer several steps |   731.728   | 50 385.284 |


|    cases \ ops per ms    | Composition V1 | Composition V2 |  Handmade  |
|:------------------------:|----------------|:--------------:|:----------:|
|  ByteArray single step   | 8 533.491      |   17 429.932   | 27 547.215 |
| ByteArray several steps  | 476.447        |   4 009.594    | 6 778.241  |
|  ByteBuffer single step  | 7 410.855      |   15 940.209   | 27 547.215 |
| ByteBuffer several steps | 731.728        |   2 957.169    | 4 791.257  |

> **Note**: in the table above, the "single step" cases correspond to the decoding processes that run in a single call
> to the `decode` method, while the "several steps" cases correspond to the decoding processes that run in the worst
> case scenario, where the input size is one byte and the decoder is called for each byte.

> Specs of the machine used for the benchmark:
> - Windows 11
> - CPU: Intel Core i9-10900KF @ 3.70 GHz, 10 cores, 20 logical processors
> - 32 GB of RAM DDR4 @ 3200 MHz
>
> Benchmark config:
> - JMH version: 1.33
> - VM version: JDK 17
> - Benchmark mode: Throughput, ops/time
> - Warmup: 5 iterations, 60 s each
> - Measurement: 5 iterations, 60 s each
> - Forks: 5

The results clearly show that the performances of the decoder created through composition are significantly lower than
those of the handmade decoder, with an average of 27 times faster.

Finally, as stated previously, the performance of the decoders created through composition is still (most of the time)
sufficient for the decoding of binary data. For example, an online game client receives an average of 150MB of data per
hour, while the composition decoder would be able to decode 731.728 * 1000 * 3600 = 2634220800.0 B/h, or 2.6GB/h in the
worst case scenario (where the bytes are received one by one from the network, which will never happen in practice).

#### Advices

In the case where the performances of the decoder created through composition are not sufficient, the user can always
create their own decoder by hand.

The following example shows how a simple decoder can be created by hand:

```kt
class Person(val name: String, val age: Int)

class PersonDecoder : Decoder<Person> {

    private val stringDecoder: Decoder<String> = UTF8StringDecoder()
    private val intDecoder: Decoder<Int> = IntDecoder()

    private var name: String? = null

    override fun decode(input: DecoderInput): Decoder.State<Person> {
        if (name == null) { // decode the name if it has not been decoded yet
            val state: Decoder.State<String> = stringDecoder.decode(input)
            // return the state if the name has not been fully decoded
            if (state.isNotDone()) return state.mapEmptyState()
            name = state.get()
        }

        val state: Decoder.State<Int> = intDecoder.decode(input)
        // return the state if the age has not been fully decoded
        if (state.isNotDone()) return state.mapEmptyState()

        // store the name in a local variable to reset the field
        val personName = name!!
        name = null

        return Decoder.State.Done(Person(personName, state.get()))
    }

    override fun reset() {
        name = null
        stringDecoder.reset()
        intDecoder.reset()
    }

}
```

As you can see, even for a simple object, the implementation of the decoder is quite big. This is why the composition
emphasizes on readability over performance. The complexity of the implementation also increases exponentially when it
comes to recursive decoding, which is why the composition is a good tool to describe how recursive decoding should be
done.

## Complete Example

Here is a more complex example of creation of several decoders using only decoder composition, the provided factories,
and recursive encoding:

```kt
class Location(val coords: Pair<Double, Double>, val name: String)
class Person(
    val name: String,
    val age: Int,
    val children: List<Person> = emptyList(),
    val godParent: Person? = null,
    val location: Location? = null,
)

// simple decoder
val doubleDecoder: Decoder<Double> = DoubleDecoder()

// aggregate decoders
// `and` is an infix shorthand for PairDecoder
val coordsDecoder: Decoder<Pair<Double, Double>> = doubleDecoder and doubleDecoder

// simply composed decoder
val locationDecoder: Decoder<Location> = composedDecoder<Location> { // this: DecodingScope<Location>
    val coords: Pair<Double, Double> = decode(coordsDecoder)
    val name: String = string()

    Location(coords, name)
}

// mapped decoder
val optionalLocationDecoder: Decoder<Location?> = locationDecoder.toOptional()

// complex recursive decoder
val personDecoder: Decoder<Person> = composedDecoder<Person> { // this: DecodingScope<Person>
    val name: String = string()
    val age: Int = int()
    val children: List<Person> = selfList() // recursively decode a collection of itself
    val godParent: Person? = selfOrNull() // recursively decode itself, or null
    val location: Location? = decode(optionalLocationDecoder)

    Person(name, age, children, godParent, location)
}
```
