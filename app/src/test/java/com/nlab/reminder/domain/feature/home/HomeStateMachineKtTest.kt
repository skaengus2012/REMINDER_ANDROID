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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.effect.SideEffectHandle
import com.nlab.reminder.core.state.asContainer
import com.nlab.reminder.core.state.asContainerWithSubscription
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.genTag
import com.nlab.reminder.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeStateMachineKtTest {
    @Test
    fun `update to loading when state was init and fetch sent`() = runTest {
        testReduceTemplate(
            input = HomeEvent.Fetch,
            initState = HomeState.Init,
            expectedState = HomeState.Loading(HomeState.Init)
        )
    }

    @Test
    fun `update to loading when state was error and OnRetryClicked sent`() = runTest {
        val throwable = Throwable()
        testReduceTemplate(
            input = HomeEvent.OnRetryClicked,
            initState = HomeState.Error(throwable),
            expectedState = HomeState.Loading(HomeState.Error(throwable))
        )
    }

    @Test
    fun `update to Loaded when state was not init and OnHomeSummaryLoaded sent`() = runTest {
        val input = HomeEvent.OnSnapshotLoaded(genHomeSnapshot())
        genHomeStates()
            .filter { it != HomeState.Init }
            .map { initState ->
                launch { testReduceTemplate(input, initState, expectedState = HomeState.Loaded(input.snapshot)) }
            }
            .joinAll()
    }

    @Test
    fun `update to error when OnSnapshotLoadFailed sent`() = runTest {
        val input = HomeEvent.OnSnapshotLoadFailed(Throwable())
        testReduceTemplate(
            input,
            initState = genHomeStates().first(),
            expectedState = HomeState.Error(input.throwable)
        )
    }

    private suspend fun testReduceTemplate(
        input: HomeEvent,
        initState: HomeState,
        expectedState: HomeState
    ) {
        val stateContainer =
            genHomeStateMachine().asContainer(CoroutineScope(Dispatchers.Default), initState)
        stateContainer.send(input)

        assertThat(
            stateContainer.stateFlow
                .filter { state -> state != initState }
                .take(1)
                .first(),
            equalTo(expectedState)
        )
    }

    @Test
    fun `start collecting homeSnapshot when state was init, fetch sent`() = runTest {
        testHomeSnapshotSubscription(HomeState.Init, HomeEvent.Fetch)
    }

    @Test
    fun `start collecting homeSnapshot when state was error, OnRetryClicked sent`() = runTest {
        testHomeSnapshotSubscription(HomeState.Error(Throwable()), HomeEvent.OnRetryClicked)
    }

    private suspend fun testHomeSnapshotSubscription(initState: HomeState, event: HomeEvent) {
        val expected = genHomeSnapshot()
        val getHomeSnapshot: GetHomeSnapshotUseCase = mock {
            whenever(mock()) doReturn flow { emit(expected) }
        }
        val stateContainer =
            genHomeStateMachine(getHomeSnapshot = getHomeSnapshot)
                .asContainerWithSubscription(CoroutineScope(Dispatchers.Default), initState)
        stateContainer.send(event)
        assertThat(
            stateContainer.stateFlow
                .filterIsInstance<HomeState.Loaded>()
                .map { it.snapshot }
                .first(),
            equalTo(expected)
        )
    }

    @Test
    fun `send HomeSnapshotLoadFailed when getHomeSnapshot occurred error`() = runTest {
        val throwable = Throwable()
        val getHomeSnapshot: GetHomeSnapshotUseCase = mock {
            whenever(mock()) doReturn flow { throw throwable }
        }
        val stateController =
            genHomeStateMachine(getHomeSnapshot = getHomeSnapshot)
                .asContainerWithSubscription(CoroutineScope(Dispatchers.Default), HomeState.Init)
        val errorDeferred = async {
            stateController.stateFlow
                .filterIsInstance<HomeState.Error>()
                .first()
        }
        stateController.send(HomeEvent.Fetch).join()
        assertThat(errorDeferred.await(), instanceOf(HomeState.Error::class))
    }

    @Test
    fun `throw exception when getHomeSnapshot occurred error`() = runTest {
        val getHomeSnapshot: GetHomeSnapshotUseCase = mock {
            whenever(mock()) doReturn flow { throw Throwable() }
        }
        val catchUseCase: (Throwable) -> Unit = mock()
        val stateController =
            genHomeStateMachine(getHomeSnapshot = getHomeSnapshot)
                .apply { catch { catchUseCase(it) } }
                .asContainerWithSubscription(CoroutineScope(Dispatchers.Default), HomeState.Init)
        stateController
            .send(HomeEvent.Fetch)
            .join()
        verify(catchUseCase, once())(any())
    }

    @Test
    fun `navigate today end when today category clicked`() = runTest {
        testNavigationEnd(
            navigateEvent = HomeEvent.OnTodayCategoryClicked,
            expectedSideEffect = HomeSideEffect.NavigateToday
        )
    }

    @Test
    fun `navigate timetable end when timetable category clicked`() = runTest {
        testNavigationEnd(
            navigateEvent = HomeEvent.OnTimetableCategoryClicked,
            expectedSideEffect = HomeSideEffect.NavigateTimetable
        )
    }

    @Test
    fun `navigate all end when all category clicked`() = runTest {
        testNavigationEnd(
            navigateEvent = HomeEvent.OnAllCategoryClicked,
            expectedSideEffect = HomeSideEffect.NavigateAllSchedule
        )
    }

    @Test
    fun `navigate tag end when tag element clicked`() = runTest {
        val testTag: Tag = genTag()
        val testSnapshots = listOf(
            genHomeSnapshot(tags = listOf(testTag)),
            genHomeSnapshot(tags = emptyList())
        )
        testSnapshots.forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagClicked(testTag),
                expectedSideEffect = HomeSideEffect.NavigateTag(testTag)
            )
        }
    }

    @Test
    fun `navigate tag config end when tag element long clicked`() = runTest {
        val testTag: Tag = genTag()
        val testSummaries = listOf(
            genHomeSnapshot(tags = listOf(testTag)),
            genHomeSnapshot(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagLongClicked(testTag),
                expectedSideEffect = HomeSideEffect.NavigateTagConfig(testTag)
            )
        }
    }

    @Test
    fun `navigate tag rename config when tag rename request invoked`() = runTest {
        val testTag: Tag = genTag()
        val testUsageCount = genLong()
        val testSummaries = listOf(
            genHomeSnapshot(tags = listOf(testTag)),
            genHomeSnapshot(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                getTagUsageCount = mock { whenever(mock(testTag)) doReturn testUsageCount },
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagRenameRequestClicked(testTag),
                expectedSideEffect = HomeSideEffect.NavigateTagRename(testTag, testUsageCount)
            )
        }
    }

    @Test
    fun `navigate tag delete confirm when tag delete request invoked`() = runTest {
        val testTag: Tag = genTag()
        val testUsageCount = genLong()
        val testSummaries = listOf(
            genHomeSnapshot(tags = listOf(testTag)),
            genHomeSnapshot(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                getTagUsageCount = mock { whenever(mock(testTag)) doReturn testUsageCount },
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagDeleteRequestClicked(testTag),
                expectedSideEffect = HomeSideEffect.NavigateTagDelete(testTag, testUsageCount)
            )
        }
    }

    private suspend fun testNavigationEnd(
        getTagUsageCount: GetTagUsageCountUseCase = mock(),
        initState: HomeState = HomeState.Loaded(genHomeSnapshot()),
        navigateEvent: HomeEvent,
        expectedSideEffect: HomeSideEffect,
    ) {
        val homeSideEffectHandle: SideEffectHandle<HomeSideEffect> = mock()
        genHomeStateMachine(homeSideEffectHandle = homeSideEffectHandle, getTagUsageCount = getTagUsageCount)
            .asContainer(CoroutineScope(Dispatchers.Default), initState)
            .send(navigateEvent)
            .join()
        verify(homeSideEffectHandle, once()).handle(expectedSideEffect)
    }

    @Test
    fun `modify tags when tag rename confirmed`() = runTest {
        val renameText = genBothify()
        val testTag: Tag = genTag()
        val modifyTagNameUseCase: ModifyTagNameUseCase = mock()
        val stateContainer =
            genHomeStateMachine(modifyTagName = modifyTagNameUseCase)
                .asContainer(CoroutineScope(Dispatchers.Default), HomeState.Loaded(genHomeSnapshot()))

        stateContainer
            .send(HomeEvent.OnTagRenameConfirmClicked(testTag, renameText))
            .join()
        verify(modifyTagNameUseCase, once())(testTag, renameText)
    }

    @Test
    fun `delete tag when delete confirm invoked`() = runTest {
        val deleteTagUseCase: DeleteTagUseCase = mock()
        val testTag: Tag = genTag()
        val stateContainer =
            genHomeStateMachine(deleteTag = deleteTagUseCase)
                .asContainer(CoroutineScope(Dispatchers.Default), HomeState.Loaded(genHomeSnapshot()))
        stateContainer
            .send(HomeEvent.OnTagDeleteConfirmClicked(testTag))
            .join()
        verify(deleteTagUseCase, once())(testTag)
    }
}