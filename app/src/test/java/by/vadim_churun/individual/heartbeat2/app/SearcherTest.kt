package by.vadim_churun.individual.heartbeat2.app

import by.vadim_churun.individual.heartbeat2.app.model.logic.internal.Searcher
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


/** Tests for [Searcher] class to validate the app's basic search logic. **/
@RunWith(JUnit4::class)
class SearcherTest {
    private val searcher = Searcher<String>()

    private fun test(input: List<String>, query: String, expected: List<String>) {
        searcher.prepare(input) { item ->
            item.split(' ').filter { it.isNotEmpty() }
        }
        val actual = searcher.search(query)
        assertArrayEquals(expected.toTypedArray(), actual.toTypedArray())
    }

    @Test
    fun singleFullMatch() {
        test(
            input    = listOf("Matched"),
            query    = "Matched",
            expected = listOf("Matched")
        )
    }

    @Test
    fun ignoreCaseMatch() {
        test(
            input    = listOf("IgnoreCase"),
            query    = "ignoreCase",
            expected = listOf("IgnoreCase")
        )
    }

    @Test
    fun substringMatch() {
        test(
            input    = listOf("FullString"),
            query    = "String",
            expected = listOf("FullString")
        )
    }

    @Test
    fun multipleWordsPartialMatch() {
        test(
            input    = listOf("Text containing multiple words"),
            query    = "multiple words",
            expected = listOf("Text containing multiple words")
        )
    }

    @Test
    fun multipleWordsSubstringMatch() {
        test(
            input    = listOf("Text containing multiple words"),
            query    = "Te nt ip ord",
            expected = listOf("Text containing multiple words")
        )
    }

    @Test
    fun multipleWordsUnorderedMatch() {
        test(
            input    = listOf("Text containing multiple words"),
            query    = "words Text multiple containing",
            expected = listOf("Text containing multiple words")
        )
    }

    @Test
    fun multipleEntriesAllMatch() {
        test(
            input = listOf(
                "Entry 1",
                "Entry 2"
            ),
            query = "Entry",
            expected = listOf(
                "Entry 1",
                "Entry 2"
            )
        )
    }

    @Test
    fun multipleEntriesSomeMatch() {
        test(
            input = listOf(
                "This is a standard entry",
                "But this one is different",
                "Now we have one even more unique",
                "And this one is standard again"
            ),
            query = "standard",
            expected = listOf(
                "This is a standard entry",
                "And this one is standard again"
            )
        )
    }

    @Test
    fun singleNotMatch() {
        test(
            input    = listOf("Banana"),
            query    = "Apple",
            expected = listOf()
        )
    }

    @Test
    fun substringNotMatch() {
        test(
            input    = listOf("Banana"),
            query    = "YellowBanana",
            expected = listOf()
        )
    }

    @Test
    fun multipleWordsNotMatch() {
        test(
            input    = listOf("This string contains some words."),
            query    = "This query contains words not present in the input.",
            expected = listOf()
        )
    }
}