
package com.example.breedingcalendar.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_events",
    indices = [
        Index(value = ["eventDate"]),
        Index(value = ["customEventTypeId"]),
        Index(value = ["sourceEventId"])
    ]
)
data class CalendarEvent(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 일정 이름 (유형 삭제 후에도 기존 이름 유지)
    val eventName: String,

    // 일정 유형 ID
    val customEventTypeId: Long? = null,

    // 일정 날짜 (LocalDate.toEpochDay() 기준)
    val eventDate: Long,

    // false: 실제 기록, true: 예정 일정
    val isScheduled: Boolean = false,

    // 예정 일정을 생성한 원본 기록 ID
    val sourceEventId: Long? = null,

    // 메모
    val memo: String = "",

    // 생성 시각 (Unix Timestamp 밀리초)
    val createdAt: Long = System.currentTimeMillis(),

    // 수정 시각 (Unix Timestamp 밀리초)
    val updatedAt: Long = System.currentTimeMillis()
)
