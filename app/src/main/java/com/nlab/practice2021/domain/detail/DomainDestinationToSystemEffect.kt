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

package com.nlab.practice2021.domain.detail

import com.nlab.practice2021.core.effect.system.Destination
import com.nlab.practice2021.core.effect.system.impl.DestinationToSystemEffect
import com.nlab.practice2021.core.effect.system.impl.MappingDestinationToSystemEffect
import com.nlab.practice2021.domain.sample2.NavigateToSample2Activity
import com.nlab.practice2021.domain.sample2.Sample2Destination
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
class DomainDestinationToSystemEffect private constructor(
    mapper: Map<KClass<out Destination>, DestinationToSystemEffect>
) : DestinationToSystemEffect by MappingDestinationToSystemEffect(mapper) {

    companion object {
        @Deprecated("Converter config to be used until Hilt is applied")
        operator fun invoke(): DomainDestinationToSystemEffect = DomainDestinationToSystemEffect(
            mapOf(
                Sample2Destination::class to NavigateToSample2Activity()
            )
        )
    }

}