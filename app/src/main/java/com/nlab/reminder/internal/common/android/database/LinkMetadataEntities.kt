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

package com.nlab.reminder.internal.common.android.database

import com.nlab.reminder.domain.common.util.link.LinkMetadata

/**
 * @author thalys
 */
fun LinkMetadataEntity.toLinkMetadata(): LinkMetadata =
    LinkMetadata(title = title ?: "", imageUrl = imageUrl ?: "")

fun LinkMetadata.toEntity(link: String, timestamp: Long): LinkMetadataEntity =
    LinkMetadataEntity(
        link,
        title,
        imageUrl,
        timestamp
    )