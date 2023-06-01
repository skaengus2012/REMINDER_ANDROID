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

import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.effect.SideEffectHandle
import com.nlab.reminder.core.state.asContainer
import com.nlab.reminder.domain.common.data.model.Tag
import com.nlab.reminder.domain.common.data.repository.TagRepository
import com.nlab.reminder.domain.common.data.model.genTag
import com.nlab.reminder.test.genStateContainerScope
import com.nlab.testkit.genBothify
import com.nlab.testkit.genLong
import com.nlab.testkit.instanceOf
import com.nlab.testkit.once
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
            genHomeStateMachine().asContainer(genStateContainerScope(), initState)
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
                .asContainer(genStateContainerScope(), initState)
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
        val stateContainer =
            genHomeStateMachine(getHomeSnapshot = mock { whenever(mock()) doReturn flow { throw throwable } })
                .asContainer(genStateContainerScope(), HomeState.Init)
        val errorDeferred = async {
            stateContainer.stateFlow
                .filterIsInstance<HomeState.Error>()
                .first()
        }
        stateContainer.send(HomeEvent.Fetch).join()
        assertThat(errorDeferred.await(), instanceOf(HomeState.Error::class))
    }

    @Test
    fun `throw exception when getHomeSnapshot occurred error`() = runTest {
        val catchUseCase: (Throwable) -> Unit = mock()
        val stateContainer =
            genHomeStateMachine(getHomeSnapshot = mock { whenever(mock()) doReturn flow { throw Throwable() } })
                .apply { catch { catchUseCase(it) } }
                .asContainer(genStateContainerScope(), HomeState.Init)
        stateContainer
            .send(HomeEvent.Fetch)
            .join()
        verify(catchUseCase, once())(any())
    }

    @Test
    fun `navigate today end when today category clicked`() = runTest {
        testNavigationEnd(
            event = HomeEvent.OnTodayCategoryClicked,
            expectedSideEffect = HomeSideEffect.NavigateToday
        )
    }

    @Test
    fun `navigate timetable end when timetable category clicked`() = runTest {
        testNavigationEnd(
            event = HomeEvent.OnTimetableCategoryClicked,
            expectedSideEffect = HomeSideEffect.NavigateTimetable
        )
    }

    @Test
    fun `navigate all end when all category clicked`() = runTest {
        testNavigationEnd(
            event = HomeEvent.OnAllCategoryClicked,
            expectedSideEffect = HomeSideEffect.NavigateAllSchedule
        )
    }

    @Test
    fun `navigate tag end when tag element clicked`() = runTest {
        val testTag: Tag = genTag()
        testNavigationEnd(
            initState = HomeState.Loaded(genHomeSnapshot()),
            event = HomeEvent.OnTagClicked(testTag),
            expectedSideEffect = HomeSideEffect.NavigateTag(testTag)
        )
    }

    @Test
    fun `show tag config popup end when tag element long clicked`() = runTest {
        val testTag: Tag = genTag()
        testNavigationEnd(
            initState = HomeState.Loaded(genHomeSnapshot()),
            event = HomeEvent.OnTagLongClicked(testTag),
            expectedSideEffect = HomeSideEffect.ShowTagConfigPopup(testTag)
        )
    }

    @Test
    fun `show tag rename popup when tag rename request invoked`() = runTest {
        val testTag: Tag = genTag()
        val testUsageCount = genLong()
        testNavigationEnd(
            tagRepository = mock { whenever(mock.getUsageCount(testTag)) doReturn Result.Success(testUsageCount) },
            initState = HomeState.Loaded(genHomeSnapshot()),
            event = HomeEvent.OnTagRenameRequestClicked(testTag),
            expectedSideEffect = HomeSideEffect.ShowTagRenamePopup(testTag, testUsageCount)
        )
    }

    @Test
    fun `show error popup when tag rename request failed`() = runTest {
        testShowErrorPopupByTagRepository(
            tagRepository = mock {
                whenever(mock.getUsageCount(any())) doReturn Result.Failure(Throwable())
            },
            event = HomeEvent.OnTagRenameRequestClicked(genTag())
        )
    }

    @Test
    fun `show tag delete popup when tag delete request invoked`() = runTest {
        val testTag: Tag = genTag()
        val testUsageCount = genLong()
        testNavigationEnd(
            tagRepository = mock { whenever(mock.getUsageCount(testTag)) doReturn Result.Success(testUsageCount) },
            initState = HomeState.Loaded(genHomeSnapshot()),
            event = HomeEvent.OnTagDeleteRequestClicked(testTag),
            expectedSideEffect = HomeSideEffect.ShowTagDeletePopup(testTag, testUsageCount)
        )
    }

    @Test
    fun `show error popup when tag delete request failed`() = runTest {
        testShowErrorPopupByTagRepository(
            tagRepository = mock {
                whenever(mock.getUsageCount(any())) doReturn Result.Failure(Throwable())
            },
            event = HomeEvent.OnTagDeleteRequestClicked(genTag())
        )
    }

    private suspend fun testNavigationEnd(
        tagRepository: TagRepository = mock(),
        initState: HomeState = HomeState.Loaded(genHomeSnapshot()),
        event: HomeEvent,
        expectedSideEffect: HomeSideEffect,
    ) {
        val homeSideEffectHandle: SideEffectHandle<HomeSideEffect> = mock()
        genHomeStateMachine(sideEffectHandle = homeSideEffectHandle, tagRepository = tagRepository)
            .asContainer(genStateContainerScope(), initState)
            .send(event)
            .join()
        verify(homeSideEffectHandle, once()).post(expectedSideEffect)
    }

    private suspend fun testShowErrorPopupByTagRepository(
        tagRepository: TagRepository,
        event: HomeEvent
    ) {
        val sideEffectHandle: SideEffectHandle<HomeSideEffect> = mock()

        genHomeStateMachine(sideEffectHandle = sideEffectHandle, tagRepository = tagRepository)
            .asContainer(genStateContainerScope(), HomeState.Loaded(genHomeSnapshot()))
            .send(event)
            .join()
        verify(sideEffectHandle, once()).post(HomeSideEffect.ShowErrorPopup)
    }

    @Test
    fun `update tag name when rename confirm invoked`() = runTest {
        val testTag: Tag = genTag()
        val rename: String = genBothify()
        val tagRepository: TagRepository = mock()
        val stateContainer =
            genHomeStateMachine(tagRepository = tagRepository)
                .asContainer(genStateContainerScope(), HomeState.Loaded(genHomeSnapshot()))
        stateContainer
            .send(HomeEvent.OnTagRenameConfirmClicked(testTag, rename))
            .join()
        verify(tagRepository, once()).updateName(testTag, rename)
    }

    @Test
    fun `show error popup when rename confirm invoked`() = runTest {
        testShowErrorPopupByTagRepository(
            tagRepository = mock {
                whenever(mock.updateName(any(), any())) doReturn Result.Failure(Throwable())
            },
            event = HomeEvent.OnTagRenameConfirmClicked(genTag(), genBothify())
        )
    }

    @Test
    fun `delete tag when delete confirm invoked`() = runTest {
        val testTag: Tag = genTag()
        val tagRepository: TagRepository = mock()
        val stateContainer =
            genHomeStateMachine(tagRepository = tagRepository)
                .asContainer(genStateContainerScope(), HomeState.Loaded(genHomeSnapshot()))
        stateContainer
            .send(HomeEvent.OnTagDeleteConfirmClicked(testTag))
            .join()
        verify(tagRepository, once()).delete(testTag)
    }

    @Test
    fun `show error popup when tag delete failed`() = runTest {
        testShowErrorPopupByTagRepository(
            tagRepository = mock {
                whenever(mock.delete(any())) doReturn Result.Failure(Throwable())
            },
            event = HomeEvent.OnTagDeleteConfirmClicked(genTag())
        )
    }
}