package com.example.breedingcalendar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_event_types"
)
data class CustomEventType(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 사용자가 지정한 일정 이름
    val name: String,

    // 일정 생성 간격 (Day)
    // 0이면 자동 일정 생성 안 함
    val repeatIntervalDays: Int = 0,

    // 자동 생성 일정의 메모 형식
    // %s = 개체 구분, %d = 번호
    @ColumnInfo(defaultValue = "'%s %d'")
    val memoFormat: String = "%s %d",

    // 번호 증가 여부
    @ColumnInfo(defaultValue = "0")
    val incrementNumber: Boolean = false,

    // 일정 유형 사용 여부
    val isActive: Boolean = true,

    // 생성 시각
    val createdAt: Long = System.currentTimeMillis()
)