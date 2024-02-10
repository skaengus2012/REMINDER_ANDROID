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
import com.nlab.statekit.lifecycle.viewmodel.ContractUiAction
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
    value = [
        "com.nlab.statekit.lifecycle.UiAction",
        "com.nlab.statekit.lifecycle.viewmodel.ContractUiAction",
    ]
)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions(UiActionProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class UiActionProcessor : AbstractProcessor() {
    private val destinationClazzToElementTable = mutableMapOf<Destination, MutableMap<String, FunSpec.Builder>>()

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val uiActionElements: List<TypeElement> =
            requiredTypeElements<UiAction>(roundEnv)
                .onFailure { e -> processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.message) }
                .getOrNull() ?: return false
        val contractUiActionElements: List<TypeElement> =
            requiredTypeElements<ContractUiAction>(roundEnv)
                .onFailure { e -> processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.message) }
                .getOrNull() ?: return false

        try {
            putElement(uiActionElements, TypeElement::generateDestinationsFromUiAction)

            val suffix = "Action"
            val decorateDestination: (String) -> String = { "${it}ViewModel" }
            putElement(contractUiActionElements) { typeElement ->
                listOf(typeElement.generateDestinationFromContractUiState(suffix, decorateDestination))
            }
        } catch (e: Exception) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.message)
            return false
        }

        if (roundEnv.processingOver()) {
            destinationClazzToElementTable.forEach { (destination, table) ->
                generateFiles(destination, table.values.toList())
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

    private fun putElement(
        elements: List<TypeElement>,
        getDestination: (TypeElement) -> List<Destination>,
    ) {
        elements.forEach { element ->
            val funSpec by lazy(LazyThreadSafetyMode.NONE) { generateFuncSpecBuilder(element).getOrThrow() }
            getDestination(element).forEach { destination ->
                val destinationTable = destinationClazzToElementTable.getOrPut(destination, ::mutableMapOf)
                val elementName = element.qualifiedName.toString()
                if (elementName !in destinationTable) {
                    destinationTable[elementName] = funSpec
                }
            }
        }
    }

    private fun generateFiles(destination: Destination, funcSpecBuilders: List<FunSpec.Builder>) {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val receiverInfo = ClassName(destination.packageInfo, destination.clazzMetadata)
        val fileSpec = FileSpec.builder(destination.packageInfo, destination.fileName)

        funcSpecBuilders
            .map { it.receiver(receiverInfo).build() }
            .forEach { fileSpec.addFunction(it) }

        fileSpec
            .build()
            .writeTo(processingEnv.filer)

        processingEnv.messager.printMessage(
            Diagnostic.Kind.NOTE,
            "generating UiAction -> [${kaptKotlinGeneratedDir}/${destination.filePath}/${destination.fileName}.kt]"
        )
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}