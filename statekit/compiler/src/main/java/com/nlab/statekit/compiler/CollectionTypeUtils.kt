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
    private const val iterableTypeName = "kotlin/collections/Iterable"
    private const val collectionTypeName = "kotlin/collections/Collection"
    private const val listTypeName = "kotlin/collections/List"
    private const val setTypeName = "kotlin/collections/Set"
    private const val mapTypeName = "kotlin/collections/Map"

    private val supportedCollections = setOf(
        iterableTypeName,
        collectionTypeName,
        listTypeName,
        setTypeName,
        mapTypeName
    )

    fun isSupportType(type: KmType): Boolean {
        return type.toClassName() in supportedCollections
    }

    fun createCollectionTypeName(type: KmType, parameters: List<TypeName>): TypeName =
        when (type.toClassName()) {
            collectionTypeName -> COLLECTION.parameterizedBy(parameters)
            iterableTypeName -> ITERABLE.parameterizedBy(parameters)
            listTypeName -> LIST.parameterizedBy(parameters)
            setTypeName -> SET.parameterizedBy(parameters)
            mapTypeName -> MAP.parameterizedBy(parameters)
            else -> throw IllegalArgumentException("Unsupported collection type -> [${type}]")
        }
}