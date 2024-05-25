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
import com.nlab.reminder.core.local.database.ReminderDatabase
import com.nlab.reminder.core.local.database.ScheduleDao
import com.nlab.reminder.core.local.database.ScheduleEntity
import com.nlab.reminder.core.local.database.ScheduleTagListDao
import com.nlab.reminder.core.local.database.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.TagDao
import com.nlab.reminder.core.local.database.TagEntity
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
    private val inputTagEntities: List<TagEntity> = listOf(
        TagEntity(name = "집안일"),
        TagEntity(name = "약속"),
        TagEntity(name = "건강"),
        TagEntity(name = "공과금 내는 날~!!"),
        TagEntity(name = "장보러 가는 날"),
        TagEntity(name = "결혼준비"),
        TagEntity(name = "경제"),
        TagEntity(name = "핸드폰"),
        TagEntity(name = "개발"),
        TagEntity(name = "스터디"),
        TagEntity(name = "데이트 장소 알아보기"),
        TagEntity(name = "뭔가 엄청엄청엄청 긴 태그~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"),
    ) + List(20) { TagEntity(name = "테스트용 태그 #$it") }
    private val inputScheduleEntities: List<ScheduleEntity> = buildList {
        this += List(300) {
            ScheduleEntity(
                title = "Programming STUDY!",
                description = "Good to know about [${faker.programmingLanguage().name()}] with ${faker.name().fullName()}",
                link = "https://github.com/skaengus2012/REMINDER_ANDROID",
                visiblePriority = 0,
                isComplete = false
            )
        }

        this += List(300) {
            ScheduleEntity(
                title = "Travel ✈️",
                description = "Go to [${faker.nation().capitalCity()}] with ${faker.name().fullName()}",
                visiblePriority = 0,
                isComplete = false
            )
        }

        this += List(300) {
            val book = faker.book()
            ScheduleEntity(
                title = "Book club",
                description = "About [${book.title()} of ${book.author()}]",
                visiblePriority = 0,
                isComplete = false
            )
        }
    }

    private lateinit var database: ReminderDatabase
    private lateinit var scheduleDao: ScheduleDao
    private lateinit var tagDao: TagDao
    private lateinit var scheduleTagListDao: ScheduleTagListDao

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = ReminderDatabase.getDatabase(context)
        scheduleDao = database.scheduleDao()
        tagDao = database.tagDao()
        scheduleTagListDao = database.scheduleTagListDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun input() = runBlocking {
        resetTagEntities()
        resetScheduleEntities()
        resetScheduleTagList()
    }

    private suspend fun resetTagEntities() {
        tagDao.find().first().forEach { tagDao.delete(it) }
        inputTagEntities.forEach { tagDao.insert(it) }
    }

    private suspend fun resetScheduleEntities() {
        scheduleDao.findByCompleteAsStream(isComplete = true).first().forEach { scheduleDao.delete(it.scheduleEntity) }
        scheduleDao.findByCompleteAsStream(isComplete = false).first().forEach { scheduleDao.delete(it.scheduleEntity) }

        inputScheduleEntities.shuffled().forEachIndexed { index, scheduleEntity ->
            scheduleDao.insert(
                scheduleEntity.copy(
                    visiblePriority = index.toLong(),
                    title = "#$index ${scheduleEntity.title}"
                )
            )
        }
    }

    private suspend fun resetScheduleTagList() {
        val tagEntities = tagDao.find().first()
        scheduleDao.findByCompleteAsStream(isComplete = false)
            .first()
            .map { it.scheduleEntity }
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