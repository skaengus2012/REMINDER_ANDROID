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

package com.nlab.reminder.macro

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import com.nlab.reminder.core.local.database.configuration.ReminderDatabase
import com.nlab.reminder.core.local.database.dao.ScheduleContentDTO
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.model.TagEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * @author Doohyun
 */
@RunWith(AndroidJUnit4::class)
class InsertDummyInfoToDatabaseMacro {
    private val faker: Faker = Faker(Locale("ko"))
    private val inputTagTexts: List<String> = listOf(
        "집안일",
        "약속",
        "건강",
        "공과금 내는 날~!!",
        "장보러 가는 날",
        "결혼준비",
        "경제",
        "핸드폰",
        "개발",
        "스터디",
        "데이트 장소 알아보기",
        "뭔가 엄청엄청엄청 긴 태그~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
    ) + List(20) { "테스트용 태그 #$it" }
    private val inputScheduleContents: List<ScheduleContentDTO> = buildList {
        this += List(300) {
            ScheduleContentDTO(
                title = "Programming STUDY!",
                description = "Good to know about [${faker.programmingLanguage().name()}] with ${faker.name().fullName()}",
                link = "https://github.com/skaengus2012/REMINDER_ANDROID",
            )
        }

        this += List(300) {
            ScheduleContentDTO(
                title = "Travel ✈️",
                description = "Go to [${faker.nation().capitalCity()}] with ${faker.name().fullName()}",
                link = null
            )
        }

        this += List(300) {
            val book = faker.book()
            ScheduleContentDTO(
                title = "Book club",
                description = "About [${book.title()} of ${book.author()}]",
                link = null
            )
        }
    }

    private lateinit var database: ReminderDatabase
    private lateinit var scheduleDao: ScheduleDAO
    private lateinit var tagDao: TagDAO
    private lateinit var scheduleTagListDao: ScheduleTagListDAO

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = ReminderDatabase.getDatabase(context)
        scheduleDao = database.scheduleDAO()
        tagDao = database.tagDAO()
        scheduleTagListDao = database.scheduleTagListDAO()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun input() = runBlocking {
        val tagEntities = resetTagEntities()
        val scheduleEntities = resetScheduleEntities()
        resetScheduleTagList(scheduleEntities, tagEntities)
    }

    private suspend fun resetTagEntities(): List<TagEntity> {
        tagDao.getAsStream().first().forEach { tagDao.deleteById(it.tagId) }
        return inputTagTexts.map { tagDao.insertAndGet(it) }
    }

    private suspend fun resetScheduleEntities(): List<ScheduleEntity> {
        scheduleDao.deleteByScheduleIds(
            scheduleIds = scheduleDao.getAsStream().first().map { it.scheduleId }.toSet()
        )

        return inputScheduleContents.shuffled().mapIndexed() { index, scheduleContent ->
            scheduleDao.insertAndGet(
                scheduleContent.copy(title = "#$index ${scheduleContent.title}")
            )
        }
    }

    private suspend fun resetScheduleTagList(
        scheduleEntities: List<ScheduleEntity>,
        tagEntities: List<TagEntity>
    ) {
        scheduleEntities
            .map { scheduleEntity ->
                List(faker.number().numberBetween(0, tagEntities.size)) { index ->
                    ScheduleTagListEntity(
                        scheduleEntity.scheduleId,
                        tagEntities[index].tagId
                    )
                }
            }
            .flatten()
            .forEach { inputEntities -> scheduleTagListDao.insert(inputEntities) }
    }
}