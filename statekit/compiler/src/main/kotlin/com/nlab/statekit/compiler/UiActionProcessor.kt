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
import com.nlab.statekit.annotation.UiActionMapping
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes(
    "com.nlab.statekit.annotation.UiAction",
    "com.nlab.statekit.annotation.UiActionMapping",
)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions(UiActionProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class UiActionProcessor : AbstractProcessor() {
    /**
     * The structure is as follows:
     * ```
     * // Class hierarchy
     * class Feature {
     *   class Action {
     *      @UiAction
     *      data object Foo : Action()
     *   }
     * }
     * ```
     * structure of table:
     * ```
     * Feature -> [FuncSpec(Foo)]
     * Feature.Action -> [FuncSpec(Foo)]
     * Feature.Action.Foo -> [FuncSpec(Foo)]
     * ```
     */
    private val actionToFuncSpecsTable = mutableMapOf<String, MutableList<Lazy<FunSpec.Builder>>>()
    private val uiActionMappingResults = mutableListOf<UiActionMappingResult>()

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        requiredTypeElements<UiAction>(roundEnv)
            .mapCatching(::putUiActionElement)
            .onFailure { e -> processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.stackTraceToString()) }
            .getOrNull() ?: return false

        requiredTypeElements<UiActionMapping>(roundEnv)
            .mapCatching(::putUiActionMappingElement)
            .onFailure { e -> processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.stackTraceToString()) }
            .getOrNull() ?: return false

        if (roundEnv.processingOver()) {
            uiActionMappingResults.forEach { result ->
                val actionFuncSpecs = result.mappingActionNames.mapNotNull(actionToFuncSpecsTable::get)
                if (actionFuncSpecs.isNotEmpty()) {
                    val packageName = result.ownerPackageMetadata
                    val ownerClazzSimpleNames = listOf(result.ownerClazzMetadata)
                    val filePath = result.filePath
                    val fileName = result.fileName
                    actionFuncSpecs.forEach { funcSpec ->
                        try {
                            generateFiles(
                                packageName = packageName,
                                ownerClazzSimpleNames = ownerClazzSimpleNames,
                                funcSpecBuilders = funcSpec,
                                filePath = filePath,
                                fileName = fileName
                            )
                        } catch (e: Exception) {
                            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.stackTraceToString())
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    private inline fun <reified T : Annotation> requiredTypeElements(
        roundEnv: RoundEnvironment
    ): Result<List<TypeElement>> = runCatching {
        roundEnv.getElementsAnnotatedWith(T::class.java)
            .also { elements ->
                require(elements.all { it.kind == ElementKind.CLASS }) {
                    "${T::class.java.simpleName} can only use for class"
                }
            }
            .map { element -> element as TypeElement }
    }

    private fun putUiActionElement(elements: List<TypeElement>) {
        elements.forEach { element ->
            val lazyFuncSpec = lazy(LazyThreadSafetyMode.NONE) { generateFuncSpecBuilder(element).getOrThrow() }
            element.parseNestedClassExcludeChildNames().forEach { className ->
                val funcSpecs = actionToFuncSpecsTable.getOrPut(className) { mutableListOf() }
                funcSpecs += lazyFuncSpec
            }
        }
    }

    private fun putUiActionMappingElement(elements: List<TypeElement>) {
        elements.forEach { uiActionMappingResults += createUiActionMappingResult(it) }
    }

    private fun generateFiles(
        packageName: String,
        ownerClazzSimpleNames: List<String>,
        funcSpecBuilders: List<Lazy<FunSpec.Builder>>,
        filePath: String,
        fileName: String
    ) {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val receiverInfo = ClassName(packageName, ownerClazzSimpleNames)
        val fileSpec = FileSpec.builder(packageName, fileName)

        funcSpecBuilders
            .map { it.value.receiver(receiverInfo).build() }
            .forEach { fileSpec.addFunction(it) }

        fileSpec
            .build()
            .writeTo(processingEnv.filer)

        processingEnv.messager.printMessage(
            Diagnostic.Kind.NOTE,
            "generating UiAction -> [${kaptKotlinGeneratedDir}/${filePath}/${fileName}.kt]"
        )
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}