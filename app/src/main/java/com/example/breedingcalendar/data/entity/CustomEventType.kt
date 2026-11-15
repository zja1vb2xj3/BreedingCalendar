package com.example.breedingcalendar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_event_types"
)
data class CustomEventType(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 사용자가 지정한 알림 이름
    val name: String,

    // 알림 주기 (Day)
    // 0이면 예정 일정 생성 안 함
    val repeatIntervalDays: Int = 0,

    // 일정 유형 사용 여부
    val isActive: Boolean = true,

    // 생성 시각
    val createdAt: Long = System.currentTimeMillis()
)