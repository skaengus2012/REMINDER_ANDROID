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

package com.nlab.statekit.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import kotlin.metadata.KmClassifier
import kotlin.metadata.KmType

/**
 * @author thalys
 */
internal fun KmType.toClassName(): String {
    return (classifier as KmClassifier.Class).name
}

internal fun KmType.toTypeName(): TypeName {
    val classTokens: List<String> =
        (classifier as KmClassifier.Class)
            .name
            .split("/")
    return ClassName(
        classTokens.subList(0, classTokens.size - 1).joinToString("."),
        classTokens.last()
    )
}