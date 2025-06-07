/*
 * Copyright (C) 2025 The N's lab Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nlab.reminder.core.text.ui

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nlab.reminder.core.text.PluralsUiText
import com.nlab.reminder.core.text.UiText
import com.nlab.testkit.faker.genBothify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.nlab.reminder.core.uitext.test.R
import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author Doohyun
 */
@RunWith(AndroidJUnit4::class)
class UiTextExtKtTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun givenFourDepthNestedUiText_whenToText_thenReturnsCorrectString() {
        val text1 = genBothify()
        val text2 = genBothify()
        val text3 = genBothify()
        val text4 = genBothify()
        val text5 = genBothify()
        val singleQuantity = 1
        val otherQuantity = genInt(min = 2, max = 10)
        val expected = context.getString(
            R.string.twice_text_combine,
            text1,
            context.resources.getQuantityString(
                R.plurals.twice_text_combine_plurals,
                singleQuantity,
                text2,
                context.resources.getQuantityString(
                    R.plurals.twice_text_combine_plurals,
                    otherQuantity,
                    text3,
                    context.getString(R.string.twice_text_combine, text4, text5)
                )
            )
        )
        val uiText = UiText(
            resId = R.string.twice_text_combine,
            text1,
            PluralsUiText(
                resId = R.plurals.twice_text_combine_plurals,
                count = singleQuantity,
                text2,
                PluralsUiText(
                    resId = R.plurals.twice_text_combine_plurals,
                    count = otherQuantity,
                    text3,
                    UiText(
                        resId = R.string.twice_text_combine,
                        text4,
                        text5
                    )
                )
            )
        )

        val actual = uiText.toText(context)
        assertThat(actual, equalTo(expected))
    }
}