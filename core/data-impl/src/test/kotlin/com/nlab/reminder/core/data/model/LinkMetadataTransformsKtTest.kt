package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.local.database.dao.LinkMetadataDTO
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class LinkMetadataTransformsKtTest {
    @Test
    fun testLinkMetadataEntityToLinkMetadata() {
        val (_, expectedLinkMetadata, entity) = genLinkAndMetadataAndEntity()
        val actualLinkMetadata = LinkMetadata(entity)

        assertThat(actualLinkMetadata, equalTo(expectedLinkMetadata))
    }

    @Test
    fun testLinkMetadataToDTO() {
        val (link, linkMetadata) = genLinkAndMetadataAndEntity()
        val expectedDTO = LinkMetadataDTO(
            link = link.rawLink,
            title = linkMetadata.title,
            imageUrl = linkMetadata.imageUrl
        )
        val actualDTO = linkMetadata.toLocalDTO(link)

        assertThat(actualDTO, equalTo(expectedDTO))
    }
}