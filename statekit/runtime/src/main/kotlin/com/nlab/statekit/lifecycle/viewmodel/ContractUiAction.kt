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

package com.nlab.statekit.lifecycle.viewmodel

/**
 * Generate UiAction dispatch method without receiver type.
 * The first simple names of annotated class should suffixed with "Action".
 * **SampleUiAction.OnClick**, **SampleUiAction.DialogAction.OnClick**
 *
 * For example, if you have a action like this: **SimpleAction.OnClick**
 * Annotation Processor will be generate method like this:
 *
 * fun SampleViewModel.onClick() {
 * }
 * @author Doohyun
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ContractUiAction