/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.practice2021.domain.feature.home

import com.nlab.practice2021.core.effect.android.navigation.NavigationMessage
import com.nlab.practice2021.core.effect.android.navigation.SendNavigationEffect
import com.nlab.practice2021.core.state.StateMachine
import com.nlab.practice2021.domain.common.effect.android.navigation.AllEndNavigationEffect
import com.nlab.practice2021.domain.common.effect.android.navigation.FlaggedEndNavigationEffect
import com.nlab.practice2021.domain.common.effect.android.navigation.ScheduledEndNavigationEffect
import com.nlab.practice2021.domain.common.effect.android.navigation.TodayEndNavigationMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeStateMachineKtTest {
    private val dummyActions: Set<HomeAction> = setOf(
        HomeAction.Fetch,
        HomeAction.OnTodayEndClicked,
        HomeAction.OnScheduledEndClicked,
        HomeAction.OnFlaggedEndClicked,
        HomeAction.OnAllEndClicked
    )

    private val dummyStates: Set<HomeState> = setOf(
        HomeState.Init,
        HomeState.Loading,
        HomeStateLoaded(HomeSummary())
    )

    private fun createHomeStateMachine(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        initState: HomeState = HomeState.Init,
        navigationEffect: SendNavigationEffect = mock(),
        getHomeSummary: GetHomeSummaryUseCase = mock { whenever(mock()) doReturn flow { emit(HomeSummary()) } },
        onHomeSummaryLoaded: (HomeSummary) -> Unit = mock(),
        onTodayClicked: () -> Unit = mock()
    ): HomeStateMachine = HomeStateMachineFactory(getHomeSummary, initState).create(
        scope,
        navigationEffect,
        onHomeSummaryLoaded,
        onTodayClicked
    )

    @Test
    fun `holds injected state when machine created`() = runTest {
        val initState = HomeStateLoaded(HomeSummary(todayNotificationCount = 1))
        val stateMachine = createHomeStateMachine(initState = initState)
        assertThat(stateMachine.state.value, sameInstance(initState))
    }

    @Test
    fun `holds init state when machine created`() {
        assertThat(
            HomeStateMachineFactory(getHomeSummary = mock())
                .create(CoroutineScope(Dispatchers.Default), mock(), mock(), mock())
                .state
                .value,
            equalTo(HomeState.Init)
        )
    }

    @Test
    fun `keep state init even when action occurs until fetched`() = runTest {
        val stateMachine: HomeStateMachine = createHomeStateMachine()
        val initState: HomeState = HomeState.Init
        dummyActions
            .asSequence()
            .filter { it !is HomeAction.Fetch }
            .forEach { action ->
                stateMachine.send(action).join()
                assertThat(stateMachine.state.value, sameInstance(initState))
            }

        stateMachine.send(HomeAction.Fetch).join()
        assertThat(stateMachine.state.value, not(sameInstance(initState)))
    }

    @Test
    fun `fetch is executed when state is init`() = runTest {
        fun createHomeStateMachineWithEmptySummary(
            initState: HomeState
        ): HomeStateMachine = createHomeStateMachine(
            getHomeSummary = mock { onBlocking { invoke() } doReturn flow {} },
            initState = initState
        )

        dummyStates
            .asSequence()
            .filter { it !is HomeState.Init }
            .map { state -> createHomeStateMachineWithEmptySummary(state) }
            .forEach { machine ->
                val curState: HomeState = machine.state.value
                machine.send(HomeAction.Fetch).join()
                assertThat(machine.state.value, sameInstance(curState))
            }

        val stateMachine: StateMachine<HomeAction, HomeState> = createHomeStateMachineWithEmptySummary(HomeState.Init)
        stateMachine.send(HomeAction.Fetch).join()
        assertThat(stateMachine.state.value, equalTo(HomeState.Loading))
    }

    @Test
    fun `Notify Loaded when fetch is called`() = runTest {
        val getHomeSummaryUseCase: GetHomeSummaryUseCase = mock {
            whenever(mock()) doReturn flow {
                repeat(10) { number ->
                    emit(HomeSummary(todayNotificationCount = number.toLong()))
                }
            }
        }
        val collectedStates: MutableList<HomeSummary> = mutableListOf()
        val onHomeSummaryLoaded: (HomeSummary) -> Unit = { collectedStates += it }
        val stateMachine: HomeStateMachine = createHomeStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            getHomeSummary = getHomeSummaryUseCase,
            onHomeSummaryLoaded = onHomeSummaryLoaded
        )
        stateMachine
            .send(HomeAction.Fetch)
            .join()
        assertThat(
            collectedStates,
            equalTo((0..9).map { HomeSummary(todayNotificationCount = it.toLong()) })
        )
    }

    @Test
    fun `Navigate today end when today clicked`() = runTest {
        testNavigationEnd(HomeAction.OnTodayEndClicked, TodayEndNavigationMessage)
    }

    @Test
    fun `Navigate scheduled end when today clicked`() = runTest {
        testNavigationEnd(HomeAction.OnScheduledEndClicked, ScheduledEndNavigationEffect)
    }

    @Test
    fun `Navigate all end when today clicked`() = runTest {
        testNavigationEnd(HomeAction.OnAllEndClicked, AllEndNavigationEffect)
    }

    @Test
    fun `Navigate flagged end when today clicked`() = runTest {
        testNavigationEnd(HomeAction.OnFlaggedEndClicked, FlaggedEndNavigationEffect)
    }

    private suspend fun testNavigationEnd(
        navigateAction: HomeAction,
        expectedNavigationMessage: NavigationMessage
    ) {
        val navigationEffect: SendNavigationEffect = mock()
        val stateMachine: HomeStateMachine = createHomeStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            initState = HomeStateLoaded(HomeSummary(todayNotificationCount = 10)),
            navigationEffect = navigationEffect
        )
        stateMachine
            .send(navigateAction)
            .join()
        verify(navigationEffect, times(1)).send(expectedNavigationMessage)
    }
}