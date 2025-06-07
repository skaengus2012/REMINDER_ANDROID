package com.nlab.reminder.core.text

import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class UiTextKtTest {
    @Test
    fun `Given text, When creating UiText, Then return correct value`() {
        val text = genBothify()
        val expected = UiText.Direct(text)
        val actual = UiText(text)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given resId, When creating UiText, Then return correct value`() {
        val resId = genInt()
        val expected = UiText.ResId(
            resId = resId,
            args = null
        )
        val actual = UiText(resId = resId)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given resId and argument, When creating UiText, Then return correct value`() {
        val resId = genInt()
        val argument = genBothify()
        val expected = UiText.ResId(
            resId = resId,
            args = arrayOf(argument)
        )
        val actual = UiText(resId = resId, argument)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given resId and twice arguments, When creating UiText, Then return correct value`() {
        val resId = genInt()
        val firstArgument = genBothify()
        val secondArgument = genBothify()
        val expected = UiText.ResId(
            resId = resId,
            args = arrayOf(firstArgument, secondArgument)
        )
        val actual = UiText(resId = resId, firstArgument, secondArgument)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given resId and 3 or more arguments, When creating UiText, Then return correct value`() {
        val resId = genInt()
        val arguments = List(size = genInt(min = 3, max = 10)) { genBothify() }
        val expected = UiText.ResId(
            resId = resId,
            args = arguments.toTypedArray()
        )
        val actual = UiText(resId = resId, arguments[0], arguments[1], *arguments.drop(2).toTypedArray())
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given resId and count, When creating PluralsUiText, Then return correct value`() {
        val resId = genInt()
        val count = genInt()
        val expected = UiText.PluralsResId(
            resId = resId,
            count = count,
            args = null
        )
        val actual = PluralsUiText(resId = resId, count = count)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given resId, count and argument, When creating PluralsUiText, Then return correct value`() {
        val resId = genInt()
        val count = genInt()
        val argument = genBothify()
        val expected = UiText.PluralsResId(
            resId = resId,
            count = count,
            args = arrayOf(argument)
        )
        val actual = PluralsUiText(resId = resId, count = count, argument)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given resId, count and twice arguments, When creating PluralsUiText, Then return correct value`() {
        val resId = genInt()
        val count = genInt()
        val firstArgument = genBothify()
        val secondArgument = genBothify()
        val expected = UiText.PluralsResId(
            resId = resId,
            count = count,
            args = arrayOf(firstArgument, secondArgument)
        )
        val actual = PluralsUiText(resId = resId, count = count, firstArgument, secondArgument)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given resId, count and 3 or more arguments, When creating PluralsUiText, Then return correct value`() {
        val resId = genInt()
        val count = genInt()
        val arguments = List(size = genInt(min = 3, max = 10)) { genBothify() }
        val expected = UiText.PluralsResId(
            resId = resId,
            count = count,
            args = arguments.toTypedArray()
        )
        val actual = PluralsUiText(
            resId = resId,
            count = count,
            arguments[0],
            arguments[1],
            *arguments.drop(2).toTypedArray()
        )
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `When creating EmptyUiText, Then return correct value`() {
        val expected = UiText.Direct(value = "")
        val actual = EmptyUiText()
        assertThat(actual, equalTo(expected))
    }
}