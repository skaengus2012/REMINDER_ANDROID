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

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.metadata.KmType

/**
 * @author thalys
 */
internal object CollectionTypeUtils {
    private const val ITERABLE_TYPE_NAME = "kotlin/collections/Iterable"
    private const val COLLECTION_TYPE_NAME = "kotlin/collections/Collection"
    private const val LIST_TYPE_NAME = "kotlin/collections/List"
    private const val SET_TYPE_NAME = "kotlin/collections/Set"
    private const val MAP_TYPE_NAME = "kotlin/collections/Map"

    private val supportedCollections = setOf(
        ITERABLE_TYPE_NAME,
        COLLECTION_TYPE_NAME,
        LIST_TYPE_NAME,
        SET_TYPE_NAME,
        MAP_TYPE_NAME
    )

    fun isSupportType(type: KmType): Boolean {
        return type.toClassName() in supportedCollections
    }

    fun createCollectionTypeName(type: KmType, parameters: List<TypeName>): TypeName =
        when (type.toClassName()) {
            COLLECTION_TYPE_NAME -> COLLECTION.parameterizedBy(parameters)
            ITERABLE_TYPE_NAME -> ITERABLE.parameterizedBy(parameters)
            LIST_TYPE_NAME -> LIST.parameterizedBy(parameters)
            SET_TYPE_NAME -> SET.parameterizedBy(parameters)
            MAP_TYPE_NAME -> MAP.parameterizedBy(parameters)
            else -> throw IllegalArgumentException("Unsupported collection type -> [${type}]")
        }
}