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

package com.nlab.reminder.domain.feature.home.view

import android.content.res.Configuration.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlab.reminder.R
import com.nlab.reminder.core.android.designsystem.component.ReminderThemeBottomSheetLayout
import com.nlab.reminder.core.android.designsystem.component.ThemeLoadingIndicator
import com.nlab.reminder.core.android.designsystem.theme.ReminderTheme
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.view.TagDeleteBottomSheetContent
import com.nlab.reminder.domain.common.tag.view.TagRenameDialog
import kotlinx.collections.immutable.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID


/**
 * @author Doohyun
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var count: Long by remember { mutableLongStateOf(0L) }
    var tags: PersistentList<Tag> by remember { mutableStateOf(persistentListOf()) }
    var isPushOn: Boolean by remember { mutableStateOf(false) }
    var configTag: Tag? by remember { mutableStateOf(null) }
    var renameTag: Tag? by remember { mutableStateOf(null) }
    var deleteTag: DeleteTagEvent? by remember { mutableStateOf(null) }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    ReminderThemeBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            if (deleteTag != null) {
                TagDeleteBottomSheetContent(
                    tagName = "Hello",
                    usageCount = 1,
                    modifier = Modifier.navigationBarsPadding(),
                    onConfirmClicked = {
                        coroutineScope.launch {
                            Timber.d("Tag Delete ok.")
                            sheetState.hide()
                        }
                    },
                    onCancelClicked = {
                        coroutineScope.launch {
                            Timber.d("Tag Delete cancel.")
                            sheetState.hide()
                        }
                    }
                )
            }
        },
        onHide = {
            if (deleteTag != null) {
                deleteTag = null
            }
        }
    ) {
        Box(
            modifier = modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .fillMaxSize()
        ) {
            val contentBottomPadding = 76.dp
            val bottomContainerHeight = 56.dp
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(37.dp))
                Logo()
                Spacer(modifier = Modifier.height(42.5.dp))
                CategoryCardSection(
                    todayCount = count,
                    timetableCount = 0,
                    allCount = 0,
                    onTodayCategoryClicked = { count++ },
                    onTimetableCategoryClicked = {
                        val id = tags.size.toLong()
                        tags += Tag(tagId = id, name = "Tag${id}")
                    },
                    onAllCategoryClicked = {
                        tags.firstOrNull()
                            ?.let { first -> tags -= first }
                    }
                )
                Spacer(modifier = Modifier.height(59.dp))
                TagCardSection(
                    modifier = Modifier.padding(bottom = 10.dp),
                    tags = tags,
                    onTagClicked = { tag ->
                        renameTag = tag
                    },
                    onTagLongClicked = { tag -> Timber.d("onLongClick Tag ${tag.tagId}") }
                )
                Spacer(modifier = Modifier.height(contentBottomPadding))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomContainerHeight)
                    .align(Alignment.BottomCenter)
            ) {
                BottomContainer(
                    containerHeight = bottomContainerHeight,
                    contentBottomPadding = contentBottomPadding,
                    contentScrollState = scrollState,
                )

                NewPlanButton(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 10.dp),
                    onClick = {
                        configTag = Tag(tagId = 1, name = "Config시도중..")
                    }
                )

                TimePushSwitchButton(
                    isPushOn = isPushOn,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp),
                    onClick = {
                        isPushOn = isPushOn.not()
                        deleteTag = DeleteTagEvent(Tag(tagId = 1, name = "삭제시도중.."))
                    },
                )
            }

            if (count > 0) {
                ThemeLoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    displayDelayTime = 2_000
                )
            }
        }
    }

    BackHandler(enabled = sheetState.isVisible) {
        coroutineScope.launch {
            sheetState.hide()
        }
    }

    if (deleteTag != null) {
        LaunchedEffect(deleteTag) {
            sheetState.show()
        }
    }

    configTag?.let { tag ->
        HomeTagConfigDialog(
            tagName = tag.name,
            usageCount = 1,
            onDismiss = { configTag = null }
        )
    }

    renameTag?.let { tag ->
        TagRenameDialog(
            initText = "Modify..",
            tagName = tag.name,
            usageCount = 5,
            onTextChanged = { text -> Timber.d("Rename Tag $text") },
            onCancel = { renameTag = null },
            onConfirm = {
                Timber.d("Rename Tag[${tag.name}]")
                renameTag = null
            },
            shouldKeyboardShown = true
        )
    }
}

@Composable
private fun Logo() {
    Image(
        modifier = Modifier
            .width(126.dp)
            .height(25.dp),
        painter = painterResource(id = R.drawable.ic_logo),
        contentDescription = null,
        colorFilter = ColorFilter.tint(ReminderTheme.colors.font1),
    )
}

@Composable
private fun HomeTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = ReminderTheme.colors.font1,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun CategoryCardSection(
    todayCount: Long,
    timetableCount: Long,
    allCount: Long,
    onTodayCategoryClicked: () -> Unit = {},
    onTimetableCategoryClicked: () -> Unit = {},
    onAllCategoryClicked: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HomeTitle(
            text = LocalContext.current.getString(R.string.home_category_header),
            modifier = Modifier.padding(bottom = 14.dp)
        )
        Row {
            TodayCategoryCard(
                modifier = Modifier.weight(1f),
                remainCount = todayCount,
                onClick = onTodayCategoryClicked
            )

            Spacer(modifier = Modifier.width(14.dp))

            TimetableCategoryCard(
                modifier = Modifier.weight(1f),
                remainCount = timetableCount,
                onClick = onTimetableCategoryClicked
            )

            Spacer(modifier = Modifier.width(14.dp))

            AllCategoryCard(
                modifier = Modifier.weight(1f),
                remainCount = allCount,
                onClick = onAllCategoryClicked
            )
        }
    }
}

@Composable
private fun TagCardSection(
    tags: ImmutableList<Tag>,
    modifier: Modifier = Modifier,
    onTagClicked: (Tag) -> Unit = {},
    onTagLongClicked: (Tag) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        HomeTitle(
            text = LocalContext.current.getString(R.string.home_tag_header),
            modifier = Modifier.padding(bottom = 14.dp)
        )
        TagCard(
            tags = tags,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked
        )
    }
}

@Preview(
    name = "LightLogoPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkLogoPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun LogoPreview() {
    ReminderTheme {
        Logo()
    }
}

@Preview(
    name = "LightCategoryCardSectionPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkCategoryCardSectionPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun CategoryCardSectionPreview() {
    ReminderTheme {
        CategoryCardSection(
            todayCount = 10,
            timetableCount = 20,
            allCount = 30,
        )
    }
}

@Preview(
    name = "LightTagCardSectionPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkTagCardSectionPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun TagCardSectionPreview() {
    ReminderTheme {
        TagCardSection(
            tags = persistentListOf(
                Tag(tagId = 1, "My Tag"),
                Tag(tagId = 2, "Your Tag"),
                Tag(tagId = 3, "Our Tag")
            )
        )
    }
}

// TODO check when make stateMachine.
private data class DeleteTagEvent(
    val tag: Tag,
    val id: String = UUID.randomUUID().toString()
)