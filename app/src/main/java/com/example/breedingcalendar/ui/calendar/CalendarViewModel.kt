
package com.example.breedingcalendar.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.breedingcalendar.data.database.AppDatabase
import com.example.breedingcalendar.data.entity.CalendarEvent
import com.example.breedingcalendar.data.entity.CustomEventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

data class CalendarUiState(
    val currentYear: Int,
    val currentMonth: Int,
    val selectedDate: Long,
    val events: List<CalendarEvent> = emptyList(),
    val customEventTypes: List<CustomEventType> = emptyList()
)

class CalendarViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val dao = database.calendarDao()
    private val today = Calendar.getInstance()

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            currentYear = today.get(Calendar.YEAR),
            currentMonth = today.get(Calendar.MONTH),
            selectedDate = dateToEpochDay(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            )
        )
    )

    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        observeEvents()
        observeCustomEventTypes()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            dao.getAllEvents().collect { events ->
                _uiState.value = _uiState.value.copy(
                    events = events
                )
            }
        }
    }

    private fun observeCustomEventTypes() {
        viewModelScope.launch {
            dao.getActiveCustomEventTypes().collect { types ->
                _uiState.value = _uiState.value.copy(
                    customEventTypes = types
                )
            }
        }
    }

    // 개별 일정 삭제
    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            database.withTransaction {
                if (event.isScheduled) {
                    dao.deleteEventById(event.id)
                } else {
                    dao.deleteScheduledEventBySourceId(event.id)
                    dao.deleteEventById(event.id)
                }
            }
        }
    }

    // 커스텀 일정 종류 삭제
    fun deleteCustomEventTypes(typeIds: List<Long>) {
        if (typeIds.isEmpty()) return

        viewModelScope.launch {
            database.withTransaction {
                typeIds.distinct().forEach { typeId ->
                    val sourceEventIds =
                        dao.getSourceEventIdsByCustomEventTypeId(typeId)

                    dao.deleteScheduledEventsByCustomEventTypeId(typeId)

                    if (sourceEventIds.isNotEmpty()) {
                        dao.deleteActualEventsByIds(sourceEventIds)
                    }

                    dao.deleteCustomEventTypeById(typeId)
                }
            }
        }
    }

    fun moveMonth(amount: Int) {
        val state = _uiState.value

        val calendar = GregorianCalendar(
            state.currentYear,
            state.currentMonth,
            1
        )

        calendar.add(Calendar.MONTH, amount)

        _uiState.value = state.copy(
            currentYear = calendar.get(Calendar.YEAR),
            currentMonth = calendar.get(Calendar.MONTH)
        )
    }

    fun goToToday() {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        _uiState.value = _uiState.value.copy(
            currentYear = year,
            currentMonth = month,
            selectedDate = dateToEpochDay(year, month, day)
        )
    }

    fun selectDate(date: Long) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date
        )
    }

    fun addCustomEventType(
        name: String,
        intervalDays: Int,
        incrementNumber: Boolean
    ) {
        val safeName = name.trim()

        if (safeName.isBlank() || intervalDays < 0) return

        viewModelScope.launch {
            dao.insertCustomEventType(
                CustomEventType(
                    name = safeName,
                    repeatIntervalDays = intervalDays,
                    memoFormat = "%s %d",
                    incrementNumber = incrementNumber
                )
            )
        }
    }

    // 커스텀 일정 설정 수정
    fun updateCustomEventType(type: CustomEventType) {
        val safeName = type.name.trim()

        if (safeName.isBlank() || type.repeatIntervalDays < 0) return

        viewModelScope.launch {
            dao.updateCustomEventType(
                type.copy(
                    name = safeName
                )
            )
        }
    }

    private fun createScheduledMemo(
        individualGroup: String,
        individualNumber: Int
    ): String {
        return "$individualGroup $individualNumber"
    }

    fun addEvent(
        types: List<CustomEventType>,
        individualGroup: String,
        individualNumber: Int,
        memo: String
    ) {
        val selectedDate = _uiState.value.selectedDate
        val safeGroup = individualGroup.trim()
        val safeMemo = memo.trim()

        if (safeGroup.isBlank()) return

        // 실제 등록일에는 첫 번째 커스텀 일정 이름만 사용
        val firstSelectedTypeName = types
            .map { it.name }
            .distinct()
            .firstOrNull()

        val recordMemoParts = mutableListOf<String>()

        if (!firstSelectedTypeName.isNullOrBlank()) {
            recordMemoParts.add(firstSelectedTypeName)
        }

        if (
            safeMemo.isNotBlank() &&
            safeMemo !in recordMemoParts
        ) {
            recordMemoParts.add(safeMemo)
        }

        val recordMemo = recordMemoParts.joinToString(" ")

        viewModelScope.launch {
            database.withTransaction {

                // 실제 기록은 한 번만 생성
                val eventId = dao.insertEvent(
                    CalendarEvent(
                        eventName = "메모",
                        customEventTypeId = null,
                        eventDate = selectedDate,
                        isScheduled = false,
                        individualGroup = safeGroup,
                        individualNumber = individualNumber,
                        memo = recordMemo
                    )
                )

                // 예정 일정은 선택한 커스텀 일정 전체를 대상으로 생성
                val scheduledEvents = types
                    .filter { it.repeatIntervalDays > 0 }
                    .map { type ->

                        val scheduledNumber =
                            individualNumber +
                                    if (type.incrementNumber) 1 else 0

                        val scheduledMemo = createScheduledMemo(
                            individualGroup = safeGroup,
                            individualNumber = scheduledNumber
                        )

                        CalendarEvent(
                            eventName = type.name,
                            customEventTypeId = type.id,
                            eventDate = selectedDate +
                                    type.repeatIntervalDays,
                            isScheduled = true,
                            sourceEventId = eventId,
                            individualGroup = safeGroup,
                            individualNumber = scheduledNumber,
                            memo = scheduledMemo
                        )
                    }

                if (scheduledEvents.isNotEmpty()) {
                    dao.insertEvents(scheduledEvents)
                }
            }
        }
    }

    fun addMemoEvent(memo: String) {
        val safeMemo = memo.trim()

        if (safeMemo.isBlank()) return

        val selectedDate = _uiState.value.selectedDate

        viewModelScope.launch {
            dao.insertEvent(
                CalendarEvent(
                    eventName = "메모",
                    customEventTypeId = null,
                    eventDate = selectedDate,
                    isScheduled = false,
                    memo = safeMemo
                )
            )
        }
    }

    companion object {

        fun dateToEpochDay(
            year: Int,
            month: Int,
            day: Int
        ): Long {
            val calendar = GregorianCalendar(
                TimeZone.getTimeZone("UTC")
            )

            calendar.clear()
            calendar.set(year, month, day)

            return calendar.timeInMillis / 86_400_000L
        }

        fun epochDayToCalendar(epochDay: Long): Calendar {
            val calendar = GregorianCalendar(
                TimeZone.getTimeZone("UTC")
            )

            calendar.timeInMillis = epochDay * 86_400_000L

            return calendar
        }
    }
}
