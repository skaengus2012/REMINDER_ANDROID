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

import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.asClassName
import javax.lang.model.element.TypeElement

/**
 * Parse the parent Nested structure class name for [TypeElement].
 *
 * example)
 * The nested structure below was parsed for target B.
 * ```
 * class A {
 *    class B {
 *       class C
 *    }
 * }
 *
 * // result
 * // ["A", "A.B"]
 * ```
 * @author Thalys
 */
@OptIn(DelicateKotlinPoetApi::class)
internal fun TypeElement.parseNestedClassExcludeChildNames(): List<String> {
    val className = asClassName()
    val packageName = className.packageName
    return className.canonicalName
        .replaceFirst(oldValue = "${packageName}.", newValue = "")
        .split(".")
        .asSequence()
        .scan(StringBuilder(packageName)) { acc, name -> acc.append(".").append(name) }
        .drop(1)
        .map { it.toString() }
        .toList()
}