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

package com.nlab.reminder.core.domain

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.isNotEmpty
import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import com.nlab.reminder.core.schedule.model.ScheduleElement
import com.nlab.reminder.core.annotation.inject.kotlin.coroutine.DefaultDispatcher
import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * @author thalys
 */
@Reusable
class FetchLinkMetadataUseCase @Inject constructor(
    private val linkMetadataTableRepository: LinkMetadataTableRepository,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    // Return the Unit, so Jacoco didn't have a problem with Context.
    suspend operator fun invoke(schedules: List<ScheduleElement>): Unit = withContext(dispatcher) {
        val links: Set<Link> =
            schedules.map(ScheduleElement::link)
                .filter(Link::isNotEmpty)
                .toSet()
        if (links.isNotEmpty()) {
            linkMetadataTableRepository.fetch(links)
        }
    }
}