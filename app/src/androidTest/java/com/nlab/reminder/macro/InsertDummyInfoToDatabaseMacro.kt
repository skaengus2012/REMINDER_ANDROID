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
import com.nlab.reminder.internal.common.android.database.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class InsertDummyInfoToDatabaseMacro {
    private val faker: Faker = Faker(Locale("ko"))
    private val inputTagEntities: List<TagEntity> = listOf(
        TagEntity(name = "돈내는거"),
        TagEntity(name = "약속"),
        TagEntity(name = "건강"),
        TagEntity(name = "공과금 내는 날~!!"),
        TagEntity(name = "장보러 가는 날"),
        TagEntity(name = "핸드폰")
    )
    private val inputScheduleEntities: List<ScheduleEntity> = buildList {
        this += List(300) {
            ScheduleEntity(
                title = "Programming STUDY!",
                description = "Good to know about [${faker.programmingLanguage().name()}] with ${faker.name().fullName()}",
                url = "https://github.com/skaengus2012/REMINDER_ANDROID",
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
    fun input() = runTest {
        resetTagEntities()
        resetScheduleEntities()
        resetScheduleTagList()
    }

    private suspend fun resetTagEntities() {
        tagDao.find().first().forEach { tagDao.delete(it) }
        inputTagEntities.forEach { tagDao.insert(it) }
    }

    private suspend fun resetScheduleEntities() {
        scheduleDao.findByComplete(isComplete = true).first().forEach { scheduleDao.delete(it.scheduleEntity) }
        scheduleDao.findByComplete(isComplete = false).first().forEach { scheduleDao.delete(it.scheduleEntity) }

        inputScheduleEntities.shuffled().forEachIndexed { index, scheduleEntity ->
            scheduleDao.insert(scheduleEntity.copy(visiblePriority = index))
        }
    }

    private suspend fun resetScheduleTagList() {
        val tagEntities = tagDao.find().first()
        scheduleDao.findByComplete(isComplete = false)
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