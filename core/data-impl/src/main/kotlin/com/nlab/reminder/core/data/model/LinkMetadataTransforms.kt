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

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import com.nlab.reminder.core.local.database.dao.LinkMetadataDTO
import com.nlab.reminder.core.local.database.model.LinkMetadataEntity
import com.nlab.reminder.core.network.datasource.LinkThumbnailResponse

/**
 * @author Doohyun
 */
internal fun LinkMetadata(entity: LinkMetadataEntity): LinkMetadata = LinkMetadata(
    title = entity.title.tryToNonBlankStringOrNull(),
    imageUrl = entity.imageUrl.tryToNonBlankStringOrNull()
)

internal fun LinkMetadata(response: LinkThumbnailResponse): LinkMetadata = LinkMetadata(
    title = response.title.tryToNonBlankStringOrNull(),
    imageUrl = response.image.tryToNonBlankStringOrNull()
)

internal fun LinkMetadata.toLocalDTO(link: Link): LinkMetadataDTO = LinkMetadataDTO(
    link = link.rawLink,
    title = title,
    imageUrl = imageUrl
)