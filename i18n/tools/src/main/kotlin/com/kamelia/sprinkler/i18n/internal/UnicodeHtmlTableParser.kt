package com.kamelia.sprinkler.i18n.internal

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.PrintWriter
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.outputStream

class UnicodeHtmlTableParser(
    private val source: Path,
    private val dest: Path,
    private val printer: PrintWriter = PrintWriter(PrintWriter.nullWriter()),
) {

    private var mode = Mode.NAME
    private var code = ""
    private var cardinal = ""
    private var ordinal = ""

    private var minimalPairLeft = 0
    private var codeHeightLeft = 0
    private val categories = hashSetOf("cardinal", "ordinal", "range")
    private val rules = hashSetOf("zero", "one", "two", "few", "many", "other", "n/a")
    private val builder = ArrayList<Pair<String, String>>()


    fun process() {
        val doc = Jsoup.parse(source)
        val rows = doc.firstChild()!!.childNodes()[1]!!.firstChild()!!.childNodes()[1].childNodes()
        val writer = dest.outputStream().writer().buffered()

        writer.use { stream ->
            stream.write("code${SP}cardinal${SP}ordinal\n")
            val content = rows.asSequence()
                .filter { it.nodeName() != "#text" }
                .flatMap {
                    val s = it.childNode(1).firstChild() ?: return@flatMap emptySequence()
                    if (s is TextNode && s.text() == "Name") return@flatMap emptySequence()
                    it.childNodes().asSequence().filter { it !is TextNode }
                }
                .iterator()
                .asPeekIterator()

            while (content.hasNext()) {
                val elem = content.next()
                when (mode) {
                    Mode.NAME -> {
                        codeHeightLeft = if (elem.attributes().hasKeyIgnoreCase("rowspan")) {
                            elem.attributes().getIgnoreCase("rowspan").toInt()
                        } else {
                            0
                        }
                        val name = elem.getText()
                        printer.println("name: $name")
                        mode = Mode.CODE
                    }

                    Mode.CODE -> {
                        if (elem.attributes().hasKeyIgnoreCase("rowspan")) {
                            codeHeightLeft -= elem.attributes().getIgnoreCase("rowspan").toInt()
                        } else if (codeHeightLeft > 0) {
                            codeHeightLeft -= 1
                        }
                        code = elem.getText()
                        printer.println("code: $code")
                        mode = Mode.RULES
                    }

                    Mode.RULES -> {
                        val category = elem.getText()
                        if (category == "=") {
                            printer.println("category: same as above")
                            repeat(4) { content.next() }
                            writeLine()
                        } else {
                            printer.println("category: $category")
                            gatherRules(category, content)
                        }
                    }
                }
            }

            val result = builder.stream()
                .sorted { o1, o2 -> o1.first.compareTo(o2.first) }
                .map { it.second }
                .collect(Collectors.joining("\n"))
            stream.write(result)
        }

    }


    private fun gatherRules(category: String, iterator: PeekIterator<Node>) {
        var firstRule = true
        while (iterator.hasNext() && iterator.getText() in rules) {
            val rule = iterator.getText(false)
            iterator.next() // skip examples
            if (firstRule) { // for the first rule we check if there is a rowspan attribute on the minimal pairs cell that make it span multiple rows
                val minimalPairs = iterator.next()
                // if so, we set the minimalPairLeft to the rowspan value
                minimalPairLeft = if (minimalPairs.attributes().hasKeyIgnoreCase("rowspan")) {
                    minimalPairs.attributes().getIgnoreCase("rowspan").toInt() - 1
                } else {
                    0
                }
                firstRule = false // we don't want to check this again
            } else {
                if (minimalPairLeft > 0) {
                    minimalPairLeft--
                } else {
                    iterator.next()
                }
            }

            if (rule == "n/a" || rule == "range") {
                iterator.next()
            } else if (rule == "other") {
                iterator.next()
                if (getRules(category).isEmpty()) {
                    appendToRule(category, "*")
                }
            } else {
                val nxt = iterator.next()
                val value = if (nxt.childNodes().isEmpty()) {
                    throw AssertionError("No value found for rule $rule")
                } else {
                    nxt.childNodes().joinToString(separator = "") {
                        if (it !is TextNode) return@joinToString ""
                        it.text().replace("or", "||")
                            .replace("and", "&&")
                            .replace(" ", "")
                    }
                }
                appendToRule(category, "$rule:$value")
                if (iterator.hasNext() && iterator.getText() in rules && iterator.getText() != "other") {
                    appendToRule(category, RULE_SP)
                }
            }
        }
        if (!iterator.hasNext() || iterator.getText() !in categories) {
            writeLine()
        } else {
            printer.println("$category rules: ${getRules(category)}")
        }
    }

    private fun writeLine() {
        builder += code to "$code$SP$cardinal$SP$ordinal"
        if (codeHeightLeft > 0) {
            mode = Mode.CODE
        } else {
            assert(codeHeightLeft == 0) {
                "Code height left is not 0: $codeHeightLeft"
            }
            mode = Mode.NAME
            cardinal = ""
            ordinal = ""
        }
    }

    private fun PeekIterator<Node>.getText(peek: Boolean = true): String {
        val fc = if (peek) peek() else next()
        return fc.getText()
    }

    private fun Node.getText(): String = when (val fc = firstChild()) {
        is TextNode -> fc.text()
        is Element -> (fc.firstChild() as TextNode).text()
        else -> throw AssertionError("Unexpected node type: ${fc!!.javaClass}")
    }

    private fun appendToRule(category: String, value: String) {
        when (category) {
            "cardinal" -> cardinal += value
            "ordinal" -> ordinal += value
        }
    }

    private fun getRules(category: String): String = when (category) {
        "cardinal" -> cardinal
        "ordinal" -> ordinal
        else -> throw AssertionError("Unknown category: $category")
    }

    private fun <T> Iterator<T>.asPeekIterator(): PeekIterator<T> = PeekIterator(this)

    private class PeekIterator<T>(private val iterator: Iterator<T>) : Iterator<T> {

        private var next: T? = null
        private var isNextSet = false

        fun peek(): T {
            if (!isNextSet) {
                next = next()
                isNextSet = true
            }
            return next!!
        }

        override fun hasNext(): Boolean {
            return isNextSet || iterator.hasNext()
        }

        override fun next(): T {
            check(iterator.hasNext()) { "No more elements" }
            if (isNextSet) {
                val value = next!!
                isNextSet = false
                next = null
                return value
            }
            return iterator.next()
        }

    }

    enum class Mode {
        NAME, CODE, RULES
    }

    private companion object {

        private const val SP = ";"

        private const val RULE_SP = "//"

    }


}
