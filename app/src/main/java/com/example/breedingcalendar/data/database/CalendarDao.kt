
package com.example.breedingcalendar.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.breedingcalendar.data.entity.CalendarEvent
import com.example.breedingcalendar.data.entity.CustomEventType
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {

    // ─────────────────────────────────────
    // CalendarEvent : 일정 관리
    // ─────────────────────────────────────

    // 모든 일정 조회
    @Query(
        """
        SELECT * FROM calendar_events
        ORDER BY eventDate ASC, id ASC
        """
    )
    fun getAllEvents(): Flow<List<CalendarEvent>>

    // 특정 날짜의 일정 조회
    // date는 LocalDate.toEpochDay() 기준
    @Query(
        """
        SELECT * FROM calendar_events
        WHERE eventDate = :date
        ORDER BY id ASC
        """
    )
    fun getEventsByDate(date: Long): Flow<List<CalendarEvent>>

    // 특정 날짜 범위의 일정 조회
    @Query(
        """
        SELECT * FROM calendar_events
        WHERE eventDate BETWEEN :startDate AND :endDate
        ORDER BY eventDate ASC, id ASC
        """
    )
    fun getEventsByDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<CalendarEvent>>

    // 일정 ID로 조회
    @Query(
        """
        SELECT * FROM calendar_events
        WHERE id = :eventId
        LIMIT 1
        """
    )
    suspend fun getEventById(eventId: Long): CalendarEvent?

    // 원본 기록으로 생성된 예정 일정 조회
    @Query(
        """
        SELECT * FROM calendar_events
        WHERE sourceEventId = :sourceEventId
        AND isScheduled = 1
        """
    )
    suspend fun getScheduledEventBySourceId(
        sourceEventId: Long
    ): CalendarEvent?

    // 일정 추가
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvent(event: CalendarEvent): Long

    // 여러 일정 추가
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvents(events: List<CalendarEvent>)

    // 일정 수정
    @Update
    suspend fun updateEvent(event: CalendarEvent)

    // 일정 삭제
    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    // 일정 ID로 삭제
    @Query(
        """
        DELETE FROM calendar_events
        WHERE id = :eventId
        """
    )
    suspend fun deleteEventById(eventId: Long)

    // 원본 기록에 연결된 예정 일정 삭제
    @Query(
        """
        DELETE FROM calendar_events
        WHERE sourceEventId = :sourceEventId
        AND isScheduled = 1
        """
    )
    suspend fun deleteScheduledEventBySourceId(
        sourceEventId: Long
    )

    // 전체 일정 삭제
    @Query("DELETE FROM calendar_events")
    suspend fun deleteAllEvents()


    // ─────────────────────────────────────
    // CustomEventType : 커스텀 일정 유형 관리
    // ─────────────────────────────────────

    // 모든 커스텀 일정 유형 조회
    @Query(
        """
        SELECT * FROM custom_event_types
        ORDER BY id ASC
        """
    )
    fun getAllCustomEventTypes(): Flow<List<CustomEventType>>

    // 활성화된 커스텀 일정 유형 조회
    @Query(
        """
        SELECT * FROM custom_event_types
        WHERE isActive = 1
        ORDER BY id ASC
        """
    )
    fun getActiveCustomEventTypes(): Flow<List<CustomEventType>>

    // 커스텀 일정 유형 ID로 조회
    @Query(
        """
        SELECT * FROM custom_event_types
        WHERE id = :typeId
        LIMIT 1
        """
    )
    suspend fun getCustomEventTypeById(
        typeId: Long
    ): CustomEventType?

    // 커스텀 일정 유형 추가
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCustomEventType(
        type: CustomEventType
    ): Long

    // 여러 커스텀 일정 유형 추가
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCustomEventTypes(
        types: List<CustomEventType>
    )

    // 커스텀 일정 유형 수정
    @Update
    suspend fun updateCustomEventType(
        type: CustomEventType
    )

    // 커스텀 일정 유형 삭제
    @Delete
    suspend fun deleteCustomEventType(
        type: CustomEventType
    )

    // 커스텀 일정 유형 ID로 삭제
    @Query(
        """
        DELETE FROM custom_event_types
        WHERE id = :typeId
        """
    )
    suspend fun deleteCustomEventTypeById(
        typeId: Long
    )

    // 전체 커스텀 일정 유형 삭제
    @Query("DELETE FROM custom_event_types")
    suspend fun deleteAllCustomEventTypes()
}
