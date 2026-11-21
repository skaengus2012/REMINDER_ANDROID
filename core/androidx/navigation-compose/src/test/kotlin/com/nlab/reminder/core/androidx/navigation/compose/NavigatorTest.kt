/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.androidx.navigation.compose

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

private object StartKey : NavKey
private object SecondKey : NavKey
private object ThirdKey : NavKey

/**
 * @author Thalys
 */
class NavigatorTest {
    private fun genNavigator(): Pair<NavigationState, Navigator> {
        val state = NavigationState(startKey = StartKey, backStack = NavBackStack<NavKey>(StartKey))
        return state to Navigator(state)
    }

    @Test
    fun `Given initial state, When read keys, Then current key is start key`() {
        val (state, _) = genNavigator()

        assertThat(state.startKey, equalTo(StartKey))
        assertThat(state.currentKey, equalTo(StartKey))
    }

    @Test
    fun `Given initial state, When navigate to key, Then key is added to the top`() {
        val (state, navigator) = genNavigator()

        navigator.navigate(SecondKey)

        assertThat(state.backStack.toList(), equalTo(listOf<NavKey>(StartKey, SecondKey)))
        assertThat(state.currentKey, equalTo(SecondKey))
    }

    @Test
    fun `Given key on top, When navigate to same key, Then stack has no duplicate`() {
        val (state, navigator) = genNavigator()
        navigator.navigate(SecondKey)

        navigator.navigate(SecondKey)

        assertThat(state.backStack.toList(), equalTo(listOf<NavKey>(StartKey, SecondKey)))
    }

    @Test
    fun `Given key in the middle, When navigate to it again, Then it moves to the top`() {
        val (state, navigator) = genNavigator()
        navigator.navigate(SecondKey)
        navigator.navigate(ThirdKey)

        navigator.navigate(SecondKey)

        assertThat(state.backStack.toList(), equalTo(listOf<NavKey>(StartKey, ThirdKey, SecondKey)))
        assertThat(state.currentKey, equalTo(SecondKey))
    }

    @Test
    fun `Given multiple keys, When navigate to start key, Then stack is cleared to start key`() {
        val (state, navigator) = genNavigator()
        navigator.navigate(SecondKey)
        navigator.navigate(ThirdKey)

        navigator.navigate(StartKey)

        assertThat(state.backStack.toList(), equalTo(listOf<NavKey>(StartKey)))
        assertThat(state.currentKey, equalTo(StartKey))
    }

    @Test
    fun `Given two keys, When go back once, Then stack has only start key`() {
        val (state, navigator) = genNavigator()
        navigator.navigate(SecondKey)

        navigator.goBack()

        assertThat(state.backStack.toList(), equalTo(listOf<NavKey>(StartKey)))
        assertThat(state.currentKey, equalTo(StartKey))
    }

    @Test
    fun `Given three keys, When go back twice, Then stack has only start key`() {
        val (state, navigator) = genNavigator()
        navigator.navigate(SecondKey)
        navigator.navigate(ThirdKey)

        navigator.goBack()
        assertThat(state.currentKey, equalTo(SecondKey))
        navigator.goBack()

        assertThat(state.backStack.toList(), equalTo(listOf<NavKey>(StartKey)))
    }

    @Test
    fun `Given only start key, When go back, Then throw IllegalStateException`() {
        val (_, navigator) = genNavigator()

        assertThrows(IllegalStateException::class.java) { navigator.goBack() }
    }
}
