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

package com.nlab.reminder

/**
 * Parsed representation of the `compileSdk` value in the version catalog (e.g. "37" or "37.1").
 *
 * @author Doohyun
 */
internal data class CompileSdkVersion(val major: Int, val minor: Int?)

internal fun CompileSdkVersion(raw: String): CompileSdkVersion {
    val parts = raw.split(".")
    require(parts.size in 1..2 && parts.all { part -> part.isNotEmpty() && part.all(Char::isDigit) }) {
        "Invalid compileSdk version: '$raw'. Expected format is 'major' or 'major.minor'."
    }
    return CompileSdkVersion(major = parts[0].toInt(), minor = parts.getOrNull(1)?.toInt())
}
