package com.nlab.reminder.core.local.database.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * @author Doohyun
 */
data class ScheduleCompositeEntity(
    @Embedded val scheduleEntity: ScheduleEntity,
    @Relation(
        parentColumn = "schedule_id",
        entityColumn = "schedule_id"
    )
    val scheduleTagListEntities: List<ScheduleTagListEntity>,
    @Relation(
        parentColumn = "schedule_id",
        entityColumn = "schedule_id"
    )
    val repeatDetailEntities: List<RepeatDetailEntity>,
)