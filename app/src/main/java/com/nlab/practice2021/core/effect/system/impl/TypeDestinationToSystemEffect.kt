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

package com.nlab.practice2021.core.effect.system.impl

import com.nlab.practice2021.core.effect.system.Destination
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
class TypeDestinationToSystemEffect private constructor(
    delegate: DestinationToSystemEffect
) : DestinationToSystemEffect by delegate {

    companion object {
        operator fun <T : Destination>invoke(
            destinationClazz: KClass<T>,
            mapper: (T) -> AndroidSystemEffect.Command
        ): TypeDestinationToSystemEffect = TypeDestinationToSystemEffect(
            delegate = object : DestinationToSystemEffect {
                override fun invoke(destination: Destination): AndroidSystemEffect.Command {
                    return mapper(checkNotNull(destinationClazz.java.cast(destination)))
                }
            }
        )
    }

}