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

import com.nlab.reminder.core.effect.SideEffectSender
import com.nlab.reminder.core.state.util.controlIn
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.genTag
import com.nlab.reminder.domain.common.tag.genTagWithResource
import com.nlab.reminder.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.advanceTimeBy
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
    private fun genEvents(): Set<HomeEvent> = setOf(
        HomeEvent.Fetch,
        HomeEvent.OnTodayCategoryClicked,
        HomeEvent.OnTimetableCategoryClicked,
        HomeEvent.OnAllCategoryClicked,
        HomeEvent.OnTagClicked(genTag()),
        HomeEvent.OnTagLongClicked(genTag()),
        HomeEvent.OnTagRenameConfirmClicked(genTag(), renameText = genLetterify()),
        HomeEvent.OnTagDeleteRequestClicked(genTag()),
        HomeEvent.OnTagDeleteConfirmClicked(genTag()),
        HomeEvent.OnSnapshotLoaded(genHomeSnapshot()),
        HomeEvent.OnSnapshotLoadFailed(Throwable())
    )

    private fun genStates(): Set<HomeState> = setOf(
        HomeState.Init,
        HomeState.Loading,
        HomeState.Loaded(genHomeSnapshot())
    )

    private fun genStateMachine(
        homeSideEffect: SideEffectSender<HomeSideEffect> = mock(),
        getHomeSnapshot: GetHomeSnapshotUseCase = mock { onBlocking { mock() } doReturn emptyFlow() },
        getTagUsageCount: GetTagUsageCountUseCase = mock(),
        modifyTagName: ModifyTagNameUseCase = mock(),
        deleteTag: DeleteTagUseCase = mock()
    ) = HomeStateMachine(
        homeSideEffect,
        getHomeSnapshot,
        getTagUsageCount,
        modifyTagName,
        deleteTag
    )

    @Test
    fun `update to loading when state was init and fetch sent`() = runTest {
        val stateController = genStateMachine().controlIn(CoroutineScope(Dispatchers.Default), HomeState.Init)
        stateController
            .send(HomeEvent.Fetch)
            .join()
        assertThat(stateController.state.value, equalTo(HomeState.Loading))
    }

    @Test
    fun `never updated when state was not init and fetch sent`() = runTest {
        val initAndStateControllers =
            genStates()
                .filter { it != HomeState.Init }
                .map { state -> state to genStateMachine().controlIn(CoroutineScope(Dispatchers.Default), state) }
        initAndStateControllers
            .map { it.second }
            .map { it.send(HomeEvent.Fetch) }
            .joinAll()

        assertThat(
            initAndStateControllers.all { (initState, controller) -> initState == controller.state.value },
            equalTo(true)
        )
    }

    @Test
    fun `update to Loaded when state was not init and OnHomeSummaryLoaded sent`() = runTest {
        val homeSummary = genHomeSnapshot()
        val stateControllers =
            genStates()
                .filter { it != HomeState.Init }
                .map { genStateMachine().controlIn(CoroutineScope(Dispatchers.Default), it) }
        stateControllers
            .map { it.send(HomeEvent.OnSnapshotLoaded(homeSummary)) }
            .joinAll()
        assertThat(
            stateControllers.map { it.state.value }.all { it == HomeState.Loaded(homeSummary) },
            equalTo(true)
        )
    }

    @Test
    fun `never updated when state was init and OnHomeSummaryLoaded sent`() = runTest {
        val stateController = genStateMachine().controlIn(CoroutineScope(Dispatchers.Default), HomeState.Init)
        stateController
            .send(HomeEvent.OnSnapshotLoaded(genHomeSnapshot()))
            .join()
        assertThat(stateController.state.value, equalTo(HomeState.Init))
    }

    @Test
    fun `never updated when any event excluded fetch and OnHomeSummaryLoaded and OnSnapshotLoadFailed sent`() = runTest {
        val initAndStateControllers = genStates().map { state ->
            state to genStateMachine().controlIn(CoroutineScope(Dispatchers.Default), state)
        }
        assertThat(
            genEvents()
                .asSequence()
                .filterNot { it is HomeEvent.Fetch }
                .filterNot { it is HomeEvent.OnSnapshotLoaded }
                .filterNot { it is HomeEvent.OnSnapshotLoadFailed }
                .map { event ->
                    initAndStateControllers.map { (initState, controller) ->
                        async {
                            controller
                                .send(event)
                                .join()
                            controller.state.value == initState
                        }
                    }
                }
                .flatten()
                .toList()
                .all { it.await() },
            equalTo(true)
        )
    }

    @Test
    fun `start homeSnapshot subscription when state was init and fetch sent`() {
        testHomeSnapshotSubscription(initState = HomeState.Init, event = HomeEvent.Fetch)
    }

    @Test
    fun `start homeSnapshot subscription when state was error and retry sent`() {
        testHomeSnapshotSubscription(
            initState = HomeState.Error(Throwable(), HomeState.Init),
            event = HomeEvent.OnRetryClicked
        )
    }

    private fun testHomeSnapshotSubscription(initState: HomeState, event: HomeEvent) = runTest {
        val expected = genHomeSnapshot()
        val getHomeSnapshot: GetHomeSnapshotUseCase =  mock {
            onBlocking { mock() } doReturn flow { emit(expected) }
        }
        val stateController =
            genStateMachine(getHomeSnapshot = getHomeSnapshot)
                .controlIn(CoroutineScope(Dispatchers.Unconfined), initState)
        stateController
            .send(event)
            .join()

        val deferred = CompletableDeferred<HomeSnapshot>()
        stateController.state
            .filterIsInstance<HomeState.Loaded>()
            .onEach { deferred.complete(it.snapshot) }
            .launchIn(genFlowObserveDispatcher())

        assertThat(deferred.await(), equalTo(expected))
    }

    @Test
    fun `send HomeSnapshotLoadFailed when getHomeSnapshot occurred error`() = runTest {
        val expectedSnapshot = genHomeSnapshot()
        val getHomeSnapshot: GetHomeSnapshotUseCase = mock {
            onBlocking { mock() } doReturn flow { emit(expectedSnapshot); delay(1_000); throw Throwable() }
                .flowOn(genFlowExecutionDispatcher(testScheduler))
        }
        val stateController =
            genStateMachine(getHomeSnapshot = getHomeSnapshot)
                .controlIn(CoroutineScope(Dispatchers.Unconfined), HomeState.Init)
        stateController
            .send(HomeEvent.Fetch)
            .join()

        val deferred = CompletableDeferred<HomeState.Error>()
        stateController.state
            .filterIsInstance<HomeState.Error>()
            .onEach { state -> deferred.complete(state) }
            .launchIn(genFlowObserveDispatcher())

        advanceTimeBy(1_200)
        assertThat(deferred.await().before, equalTo(HomeState.Loaded(expectedSnapshot)))
    }

    @Test
    fun `navigate today end when today category clicked`() = runTest {
        testNavigationEnd(
            navigateEvent = HomeEvent.OnTodayCategoryClicked,
            expectedSideEffectMessage = HomeSideEffect.NavigateToday
        )
    }

    @Test
    fun `navigate timetable end when timetable category clicked`() = runTest {
        testNavigationEnd(
            navigateEvent = HomeEvent.OnTimetableCategoryClicked,
            expectedSideEffectMessage = HomeSideEffect.NavigateTimetable
        )
    }

    @Test
    fun `navigate all end when all category clicked`() = runTest {
        testNavigationEnd(
            navigateEvent = HomeEvent.OnAllCategoryClicked,
            expectedSideEffectMessage = HomeSideEffect.NavigateAllSchedule
        )
    }

    @Test
    fun `navigate tag end when tag element clicked`() = runTest {
        val testTag: Tag = genTag()
        val testSummaries = listOf(
            genHomeSnapshot(tags = listOf(genTagWithResource(testTag))),
            genHomeSnapshot(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagClicked(testTag),
                expectedSideEffectMessage = HomeSideEffect.NavigateTag(testTag)
            )
        }
    }

    @Test
    fun `navigate tag config end when tag element long clicked`() = runTest {
        val testTag: Tag = genTag()
        val testSummaries = listOf(
            genHomeSnapshot(tags = listOf(genTagWithResource(testTag))),
            genHomeSnapshot(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagLongClicked(testTag),
                expectedSideEffectMessage = HomeSideEffect.NavigateTagConfig(testTag)
            )
        }
    }

    @Test
    fun `navigate tag rename config when tag rename request invoked`() = runTest {
        val testTag: Tag = genTag()
        val testUsageCount = genLong()
        val testSummaries = listOf(
            genHomeSnapshot(tags = listOf(genTagWithResource(testTag))),
            genHomeSnapshot(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                getTagUsageCount = mock { whenever(mock(testTag)) doReturn testUsageCount },
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagRenameRequestClicked(testTag),
                expectedSideEffectMessage = HomeSideEffect.NavigateTagRename(testTag, testUsageCount)
            )
        }
    }

    @Test
    fun `navigate tag delete confirm when tag delete request invoked`() = runTest {
        val testTag: Tag = genTag()
        val testUsageCount = genLong()
        val testSummaries = listOf(
            genHomeSnapshot(tags = listOf(genTagWithResource(testTag))),
            genHomeSnapshot(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                getTagUsageCount = mock { whenever(mock(testTag)) doReturn testUsageCount },
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagDeleteRequestClicked(testTag),
                expectedSideEffectMessage = HomeSideEffect.NavigateTagDelete(testTag, testUsageCount)
            )
        }
    }

    private suspend fun testNavigationEnd(
        getTagUsageCount: GetTagUsageCountUseCase = mock(),
        initState: HomeState = HomeState.Loaded(genHomeSnapshot()),
        navigateEvent: HomeEvent,
        expectedSideEffectMessage: HomeSideEffect,
    ) {
        val homeSideEffect: SideEffectSender<HomeSideEffect> = mock()
        genStateMachine(homeSideEffect = homeSideEffect, getTagUsageCount = getTagUsageCount)
            .controlIn(CoroutineScope(Dispatchers.Default), initState)
            .send(navigateEvent)
            .join()
        verify(homeSideEffect, once()).post(expectedSideEffectMessage)
    }

    @Test
    fun `modify tags when tag rename confirmed`() = runTest {
        val renameText = genBothify()
        val testTag: Tag = genTag()
        val modifyTagNameUseCase: ModifyTagNameUseCase = mock()
        val stateMachine =
            genStateMachine(modifyTagName = modifyTagNameUseCase)
                .controlIn(CoroutineScope(Dispatchers.Default), HomeState.Loaded(genHomeSnapshot()))

        stateMachine
            .send(HomeEvent.OnTagRenameConfirmClicked(testTag, renameText))
            .join()
        verify(modifyTagNameUseCase, once())(testTag, renameText)
    }

    @Test
    fun `delete tag when delete confirm invoked`() = runTest {
        val deleteTagUseCase: DeleteTagUseCase = mock()
        val testTag: Tag = genTag()
        val stateMachine =
            genStateMachine(deleteTag = deleteTagUseCase)
                .controlIn(CoroutineScope(Dispatchers.Default), HomeState.Loaded(genHomeSnapshot()))
        stateMachine
            .send(HomeEvent.OnTagDeleteConfirmClicked(testTag))
            .join()
        verify(deleteTagUseCase, once())(testTag)
    }
}