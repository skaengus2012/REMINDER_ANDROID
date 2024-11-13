/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

/**
 * @author Doohyun
 */
/**
internal class HomeInterceptor @Inject constructor(
    tagRepository: TagRepository
) : Interceptor<HomeAction, HomeUiState> by buildDslInterceptor(defineDSL = {
    state<HomeUiState.Success> {
        action<HomeAction.OnTagLongClicked> { (action) ->
            tagRepository.getUsageCount(action.tag.id)
                .onSuccess { usageCount ->
                    // todo
                 //   dispatch(HomeAction.TagConfigMetadataLoaded(action.tag, usageCount))
                }
                .onFailure { e -> dispatch(HomeAction.ErrorOccurred(e)) }
                .getOrThrow()
        }
        action<HomeAction.OnTagRenameConfirmClicked> { (_, before) ->
            /** TODO
            catching { checkNotNull(before.workflow as? HomeWorkflow.TagRename) { "TagRename workflow was not set" } }
                .map { tagRename -> with(tagRename) { tag.copy(name = renameText) } }
                .flatMap { tag ->

                //    tagRepository.save(tag)
                }
                .getOrThrow()
            */
        }
        action<HomeAction.OnTagDeleteConfirmClicked> { (_, before) ->
            catching { checkNotNull(before.workflow as? HomeWorkflow.TagDelete) { "TagDelete workflow was not set" } }
                .flatMap { tagDelete -> tagRepository.delete(tagDelete.tag.id) }
                .getOrThrow()
        }
    }
})*/