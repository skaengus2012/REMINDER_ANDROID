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

import com.nlab.statekit.lifecycle.UiAction
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror

/**
 * @author Doohyun
 *
 * Generate receiver name from [UiAction.receiverTypes]
 */
@OptIn(DelicateKotlinPoetApi::class)
internal fun TypeElement.generateDestinationsFromUiAction(): List<Destination> {
    val typeMirrors: List<TypeMirror> = buildList {
        try {
            getAnnotation(UiAction::class.java).receiverTypes
        } catch (e: MirroredTypesException) {
            addAll(e.typeMirrors)
        }
    }

    return typeMirrors.map { typeMirror ->
        val lowerCaseToTokens: Map<Boolean, List<String>> = typeMirror
            .asTypeName()
            .toString()
            .split(".")
            .groupBy { it.first().isLowerCase() }
        Destination(
            packageMetadata = lowerCaseToTokens.getOrDefault(true, emptyList()),
            clazzMetadata = lowerCaseToTokens.getValue(false)
        )
    }
}

/**
 * @author Doohyun
 *
 * Generate fallback receiver name from [TypeElement.asClassName]
 * [TypeElement.asClassName] should be finished with [classSuffix].
 *
 * If [TypeElement.asClassName] is **com.nlab.sample.SimpleAction.SimpleActionImpl**,
 * And [classSuffix] is **Action**,
 * default receiver will be generated to **com.nlab.sample.Simple** with [decorateDestination].
 */
@OptIn(DelicateKotlinPoetApi::class)
internal fun TypeElement.generateDestinationFromContractUiState(
    classSuffix: String,
    decorateDestination: (String) -> String
): Destination {
    val className = asClassName()
    val packageName = className.packageName
    val canonicalName = className.canonicalName

    val firstClazzName: String =
        canonicalName
            .replaceFirst("${packageName}.", "")
            .split(".")
            .first()
    require(firstClazzName.endsWith(classSuffix)) {
        """Cannot generate fallback receiver because action name not finished with $classSuffix.
           Please check -> [${className.simpleName}]
           """.trimMargin()
    }
    return Destination(
        packageMetadata = packageName.split("."),
        clazzMetadata = listOf(
            decorateDestination(firstClazzName.substring(0, firstClazzName.length - classSuffix.length))
        )
    )
}