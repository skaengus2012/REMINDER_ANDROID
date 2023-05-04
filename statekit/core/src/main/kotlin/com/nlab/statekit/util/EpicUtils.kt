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

package com.nlab.statekit.util

import com.nlab.statekit.Action
import com.nlab.statekit.middleware.epic.DefaultEpic
import com.nlab.statekit.middleware.epic.Epic
import com.nlab.statekit.middleware.epic.EpicSource
import com.nlab.statekit.middleware.epic.dsl.DslEpic
import com.nlab.statekit.middleware.epic.dsl.DslEpicBuilder

/**
 * @author thalys
 */
fun <A : Action> buildEpic(vararg epicSources: EpicSource<A>): Epic<A> = DefaultEpic(epicSources.toList())
fun <A : Action> buildDslEpic(block: DslEpicBuilder<A>.() -> Unit): Epic<A> = DslEpic(block)