/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.test

import com.github.javafaker.Faker

/**
 * @author Doohyun
 */

private val f = Faker()

fun genNumerify(numericString: String = "###"): String = f.numerify(numericString)
fun genLetterify(letterString: String = "???", isUpper: Boolean = false): String = f.letterify(letterString, isUpper)
fun genBothify(string: String = "???###", isUpper: Boolean = false): String = f.bothify(string, isUpper)
fun genLong(numericString: String = "#######"): Long = genNumerify(numericString).toLong()
fun genInt(numericString: String = "####"): Int = genNumerify(numericString).toInt()
fun genBoolean(): Boolean = genInt("#") % 2 == 0