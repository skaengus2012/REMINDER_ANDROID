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

package com.nlab.statekit.compiler

import com.nlab.statekit.annotation.UiActionMapping
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror

/**
 * @author Thalys
 */
internal data class UiActionMappingResult(
    val ownerName: String,
    val ownerPackageMetadata: String,
    val ownerClazzMetadata: String,
    val mappingActionNames: Set<String>
)

internal val UiActionMappingResult.filePath: String
    get() = ownerPackageMetadata.replace(".", "/")

internal val UiActionMappingResult.fileName: String
    get() = ownerClazzMetadata
        .split(".")
        .joinToString(separator = "", transform = ::capitalizeFirstLetter)
        .let { text -> "${text}_GeneratedUiActions" }

private fun capitalizeFirstLetter(text: String): String {
    return if (text[0].isUpperCase()) text else text.replaceFirstChar { it.uppercase() }
}

@OptIn(DelicateKotlinPoetApi::class)
internal fun createUiActionMappingResult(typeElement: TypeElement): UiActionMappingResult {
    val className = typeElement.asClassName()
    val packageName = className.packageName
    val canonicalName = className.canonicalName
    val typeMirrors: List<TypeMirror> = buildList {
        try {
            typeElement.getAnnotation(UiActionMapping::class.java).actions
        } catch (e: MirroredTypesException) {
            addAll(e.typeMirrors)
        }
    }
    return UiActionMappingResult(
        ownerName = canonicalName,
        ownerPackageMetadata = packageName,
        ownerClazzMetadata = canonicalName.replaceFirst("${packageName}.", ""),
        mappingActionNames = typeMirrors.map { it.asTypeName().toString() }.toSet()
    )
}