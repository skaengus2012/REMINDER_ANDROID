/*
 * Copyright (C) 2018 The N's lab Open Source Project
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
 *
 */

package com.nlab.practice2021.core.effect.system.impl

import android.app.Activity
import android.os.Bundle
import com.nlab.practice2021.core.effect.system.Destination
import com.nlab.practice2021.core.effect.system.SystemEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
class AndroidSystemEffect(
    private val commandSharedFlow: MutableSharedFlow<Command>,
    private val destinationToSystemEffect: DestinationToSystemEffect
) : SystemEffect {

    override suspend fun navigateTo(destination: Destination) {
        commandSharedFlow.emit(destinationToSystemEffect(destination))
    }
    
    sealed class Command {
        class NavigateActivity(val clazz: KClass<out Activity>, val bundle: Bundle = Bundle()) : Command()
    }
}