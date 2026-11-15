
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

    @Query("""
        SELECT * FROM calendar_events
        ORDER BY eventDate ASC, id ASC
    """)
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("""
        SELECT * FROM calendar_events
        WHERE eventDate = :date
        ORDER BY id ASC
    """)
    fun getEventsByDate(date: Long): Flow<List<CalendarEvent>>

    @Query("""
        SELECT * FROM calendar_events
        WHERE eventDate BETWEEN :startDate AND :endDate
        ORDER BY eventDate ASC, id ASC
    """)
    fun getEventsByDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<CalendarEvent>>

    @Query("""
        SELECT * FROM calendar_events
        WHERE id = :eventId
        LIMIT 1
    """)
    suspend fun getEventById(eventId: Long): CalendarEvent?

    @Query("""
        SELECT * FROM calendar_events
        WHERE sourceEventId = :sourceEventId
        AND isScheduled = 1
    """)
    suspend fun getScheduledEventsBySourceId(
        sourceEventId: Long
    ): List<CalendarEvent>

    @Query("""
        SELECT * FROM calendar_events
        WHERE sourceEventId = :sourceEventId
        AND isScheduled = 1
        LIMIT 1
    """)
    suspend fun getScheduledEventBySourceId(
        sourceEventId: Long
    ): CalendarEvent?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvents(events: List<CalendarEvent>)

    @Update
    suspend fun updateEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("""
        DELETE FROM calendar_events
        WHERE id = :eventId
    """)
    suspend fun deleteEventById(eventId: Long)

    @Query("""
        DELETE FROM calendar_events
        WHERE sourceEventId = :sourceEventId
        AND isScheduled = 1
    """)
    suspend fun deleteScheduledEventBySourceId(
        sourceEventId: Long
    )

    // 특정 커스텀 일정에 연결된 기록 ID 조회
    @Query("""
        SELECT DISTINCT sourceEventId
        FROM calendar_events
        WHERE customEventTypeId = :typeId
        AND isScheduled = 1
        AND sourceEventId IS NOT NULL
    """)
    suspend fun getSourceEventIdsByCustomEventTypeId(
        typeId: Long
    ): List<Long>

    // 선택한 커스텀 일정으로 생성된 예정 일정만 삭제
    @Query("""
        DELETE FROM calendar_events
        WHERE customEventTypeId = :typeId
        AND isScheduled = 1
    """)
    suspend fun deleteScheduledEventsByCustomEventTypeId(
        typeId: Long
    )

    // 해당 커스텀 일정과 연결된 실제 기록 삭제
    @Query("""
        DELETE FROM calendar_events
        WHERE id IN (:eventIds)
        AND isScheduled = 0
    """)
    suspend fun deleteActualEventsByIds(
        eventIds: List<Long>
    )

    @Query("DELETE FROM calendar_events")
    suspend fun deleteAllEvents()

    @Query("""
        SELECT * FROM custom_event_types
        ORDER BY id ASC
    """)
    fun getAllCustomEventTypes(): Flow<List<CustomEventType>>

    @Query("""
        SELECT * FROM custom_event_types
        WHERE isActive = 1
        ORDER BY id ASC
    """)
    fun getActiveCustomEventTypes(): Flow<List<CustomEventType>>

    @Query("""
        SELECT * FROM custom_event_types
        WHERE id = :typeId
        LIMIT 1
    """)
    suspend fun getCustomEventTypeById(
        typeId: Long
    ): CustomEventType?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCustomEventType(
        type: CustomEventType
    ): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCustomEventTypes(
        types: List<CustomEventType>
    )

    @Update
    suspend fun updateCustomEventType(type: CustomEventType)

    @Delete
    suspend fun deleteCustomEventType(type: CustomEventType)

    @Query("""
        DELETE FROM custom_event_types
        WHERE id = :typeId
    """)
    suspend fun deleteCustomEventTypeById(typeId: Long)

    @Query("DELETE FROM custom_event_types")
    suspend fun deleteAllCustomEventTypes()
}
