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

import com.nlab.statekit.annotation.UiAction
import com.nlab.statekit.annotation.internal.StateKitGenerated
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.Element
import kotlin.metadata.ClassKind
import kotlin.metadata.KmType
import kotlin.metadata.KmValueParameter
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.kind

/**
 * @author Doohyun
 */
internal fun generateFuncSpecBuilder(element: Element): Result<FunSpec.Builder> = runCatching {
    val clazzMetadata =
        (KotlinClassMetadata.readStrict(element.getAnnotation(Metadata::class.java)) as KotlinClassMetadata.Class)
            .kmClass
    val constructors = clazzMetadata.constructors
    require(constructors.size == 1) {
        """There are invalid constructor annotated clazz.
           Clazz should be have only one constructor. -> [${clazzMetadata.name}]
        """.trimMargin()
    }

    val valueParameters = constructors.first().valueParameters
    val statementBuilder: StringBuilder =
        StringBuilder(clazzMetadata.name.replace("/", "."))
    if (clazzMetadata.kind != ClassKind.OBJECT) {
        statementBuilder.append("(")
        valueParameters.forEachIndexed { index, parameter ->
            statementBuilder.append(
                "${parameter.name}${if (index + 1 == valueParameters.size) "" else ", "}"
            )
        }
        statementBuilder.append(")")
    }

    val funcSpecBuilder = FunSpec
        .builder(name = element.simpleName.let { name ->
            val str = name.toString()
            "${str[0].lowercaseChar()}${str.substring(1, str.length)}"
        })
        .addAnnotation(StateKitGenerated::class)
        .addStatement("return dispatch($statementBuilder)")
        .returns(ClassName("kotlinx.coroutines", "Job"))

    constructors.first().valueParameters.map { parameter ->
        funcSpecBuilder.addParameter(
            name = parameter.name,
            type = convertTypeName(parameter)
        )
    }

    if (element.isPublicFunction()) funcSpecBuilder
    else funcSpecBuilder.addModifiers(KModifier.INTERNAL)
}

private fun convertTypeName(valueParameter: KmValueParameter): TypeName {
    val type = valueParameter.type
    val relations = linkedMapOf<KmType, List<KmType>>()
    setParameterRelations(kmTypes = listOf(type), relations)

    val typeToTypeNames = hashMapOf<KmType, TypeName>()
    registerTypeName(
        typeToTypeNames,
        relations.entries.map { (k, v) -> k to v }.toMutableList(),
        relationCursor = 0
    )

    return typeToTypeNames[type]!!
}

private tailrec fun setParameterRelations(
    kmTypes: List<KmType>,
    relations: LinkedHashMap<KmType, List<KmType>>
) {
    if (kmTypes.isEmpty()) return
    val curType: KmType = kmTypes.first()
    val curArguments = curType.arguments
    val childTypes = if (curArguments.isEmpty()) {
        emptyList()
    } else {
        check(CollectionTypeUtils.isSupportType(curType)) {
            "Unsupported type -> ${curType.toTypeName()}"
        }
        curArguments.map { argument ->
            checkNotNull(argument.type) { "star protection not supported." }
        }
    }

    relations[curType] = childTypes
    setParameterRelations(
        kmTypes = kmTypes.subList(1, kmTypes.size) + childTypes,
        relations
    )
}

private tailrec fun registerTypeName(
    typeToTypeNames: MutableMap<KmType, TypeName>,
    relations: MutableList<Pair<KmType, List<KmType>>>,
    relationCursor: Int,
) {
    if (relations.isEmpty()) return
    val (curType, childTypes) = relations[relationCursor]

    val typeName: TypeName? = if (childTypes.isEmpty()) {
        curType.toTypeName()
    } else {
        val hasTypeNamesAll = childTypes.all { typeToTypeNames.containsKey(it) }
        if (hasTypeNamesAll) {
            CollectionTypeUtils.createCollectionTypeName(
                curType,
                parameters = childTypes.mapNotNull { typeToTypeNames[it] }
            )
        } else {
            null
        }
    }

    if (typeName == null) {
        registerTypeName(
            typeToTypeNames,
            relations,
            relationCursor = relationCursor + 1,
        )
    } else {
        typeToTypeNames[curType] = typeName
        relations.removeAt(relationCursor)
        registerTypeName(
            typeToTypeNames,
            relations,
            relationCursor = 0,
        )
    }
}

private fun Element.isPublicFunction(): Boolean {
    return getAnnotation(UiAction::class.java)
        ?.isPublic
        ?: throw IllegalArgumentException("Cannot recognize any actions annotation")
}