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

package com.nlab.reminder.core.androidx.transition

import androidx.transition.Transition

/**
 * @author thalys
 */
inline fun transitionListenerOf(
    crossinline onStart: (Transition) -> Unit = {},
    crossinline onEnd: (Transition) -> Unit = {},
    crossinline onCancel: (Transition) -> Unit = {},
    crossinline onPause: (Transition) -> Unit = {},
    crossinline onResume: (Transition) -> Unit = {}
): Transition.TransitionListener = object : Transition.TransitionListener {
    override fun onTransitionStart(transition: Transition) {
        onStart(transition)
    }

    override fun onTransitionEnd(transition: Transition) {
        onEnd(transition)
    }

    override fun onTransitionCancel(transition: Transition) {
        onCancel(transition)
    }

    override fun onTransitionPause(transition: Transition) {
       onPause(transition)
    }

    override fun onTransitionResume(transition: Transition) {
        onResume(transition)
    }
}