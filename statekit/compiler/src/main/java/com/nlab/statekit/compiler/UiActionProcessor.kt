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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isObject
import com.squareup.kotlinpoet.metadata.toKmClass
import kotlinx.metadata.KmType
import kotlinx.metadata.KmValueParameter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("com.nlab.statekit.lifecycle.UiAction")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions(UiActionProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@OptIn(KotlinPoetMetadataPreview::class)
class UiActionProcessor : AbstractProcessor() {
    private val functionEntries: MutableMap<Destination, List<FunSpec.Builder>> = hashMapOf()

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(UiAction::class.java)
        val hasInvalidElement: Boolean = elements.any { it.kind != ElementKind.CLASS }
        if (hasInvalidElement) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "AutoEvent can only use for class"
            )
            return false
        }

        val destinationClazzToElements: MutableMap<String, MutableSet<Element>> = hashMapOf()
        elements.forEach { element ->
            val destinationClazz = parseDestinationClazzString(element)
            if (destinationClazzToElements.containsKey(destinationClazz).not()) {
                destinationClazzToElements[destinationClazz] = hashSetOf()
            }

            destinationClazzToElements[destinationClazz]?.let { it += element }
        }

        val destinationToFunctions =
            runCatching { generationDestinationToFuncSpecBuilders(destinationClazzToElements) }
        destinationToFunctions
            .onSuccess { functionEntries += it  }
            .onFailure {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, it.message)
            }
        if (destinationToFunctions.isFailure) {
            return false
        }

        if (roundEnv.processingOver()) {
            functionEntries.forEach { (destination, funcSpecBuilders) ->
                generateFiles(
                    destination,
                    funcSpecBuilders
                )
            }
        }
        return true
    }

    private fun parseDestinationClazzString(element: Element): String {
        val typeElement = element as TypeElement
        return typeElement.annotationMirrors
            .first()  // Annotation information was introduced in version 1.8.20 and appears last.
            .elementValues.entries
            .first()
            .value
            .toString()
    }

    private fun generationDestinationToFuncSpecBuilders(
        destinationClazzToElements: MutableMap<String, MutableSet<Element>>
    ): List<Pair<Destination, List<FunSpec.Builder>>> =
        destinationClazzToElements.entries.map { (destinationClazz, elements) ->
            val destination: Destination = generateDestination(destinationClazz)
            val funcSpecBuilders: List<FunSpec.Builder> =
                elements.map { element -> generateFuncSpecBuilder(element) }

            destination to funcSpecBuilders
        }

    private fun generateDestination(destinationClazz: String): Destination {
        val tokens = destinationClazz.split(".").let { it.subList(0, it.size - 1) }
        val firstClassTokenIndex: Int =
            tokens.indexOfFirst { token -> token.first().isUpperCase() }
        val packageTokens = tokens.subList(0, firstClassTokenIndex)
        return Destination(
            packageInfo = packageTokens.joinToString("."),
            clazzName = tokens.joinToString("."),
            filePath = "/${packageTokens.joinToString("/")}",
            fileName = "${tokens[firstClassTokenIndex]}_UiActions"
        )
    }

    private fun generateFuncSpecBuilder(element: Element): FunSpec.Builder {
        val metadata = element.getAnnotation(Metadata::class.java).toKmClass()
        val constructors = metadata.constructors
        check(constructors.isNotEmpty()) {
            "There are no constructor annotated clazz. [${metadata.name}]"
        }

        val valueParameters = constructors.first().valueParameters
        val statementBuilder: StringBuilder =
            StringBuilder(metadata.name.replace("/", "."))
        if (metadata.isObject.not()) {
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
            .addStatement("return dispatch($statementBuilder)")
            .returns(jobType)

        constructors.first().valueParameters.map { parameter ->
            funcSpecBuilder.addParameter(parameter.name,
                convertTypeName(parameter)
            )
        }

        return funcSpecBuilder
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
                "Unsupported collection type -> ${curType.toTypeName()}"
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

    private fun generateFiles(destination: Destination, funcSpecBuilders: List<FunSpec.Builder>) {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val receiverInfo = ClassName(destination.packageInfo, destination.clazzName)
        val fileSpec = FileSpec.builder(destination.packageInfo, destination.fileName)

        funcSpecBuilders
            .map { it.receiver(receiverInfo).build() }
            .forEach { fileSpec.addFunction(it) }

        fileSpec
            .build()
            .writeTo(processingEnv.filer)

        processingEnv.messager.printMessage(
            Diagnostic.Kind.NOTE,
            "generate UiAction. [${kaptKotlinGeneratedDir}${destination.filePath}/${destination.fileName}.kt]"
        )
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        private val jobType: TypeName = ClassName("kotlinx.coroutines", "Job")
    }

    private data class Destination(
        val packageInfo: String,
        val clazzName: String,
        val filePath: String,
        val fileName: String
    )
}