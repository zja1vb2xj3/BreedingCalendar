
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
import kotlinx.coroutines.flow.collect
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

    // 일정 삭제
    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            database.withTransaction {
                if (event.isScheduled) {
                    // 예정 일정만 삭제
                    dao.deleteEventById(event.id)
                } else {
                    // 실제 기록과 연결된 예정 일정 전체 삭제
                    dao.deleteScheduledEventBySourceId(event.id)

                    // 원본 기록 삭제
                    dao.deleteEventById(event.id)
                }
            }
        }
    }

    // 월 이동
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

    // 오늘로 이동
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

    // 날짜 선택
    fun selectDate(date: Long) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date
        )
    }

    // 커스텀 일정 등록
    fun addCustomEventType(
        name: String,
        intervalDays: Int
    ) {
        val safeName = name.trim()

        if (safeName.isBlank() || intervalDays < 0) return

        viewModelScope.launch {
            dao.insertCustomEventType(
                CustomEventType(
                    name = safeName,
                    repeatIntervalDays = intervalDays
                )
            )
        }
    }

    // 개체 번호의 순번 증가
    // 예: 16 -> 17, 19 -> 110
    private fun incrementIndividualNumber(
        value: String
    ): String? {
        val safeValue = value.trim()

        // 앞부분은 개체 구분명, 뒷부분은 순번
        val match = Regex("^(.+?)(\\d+)$").matchEntire(safeValue)
            ?: return null

        val individualName = match.groupValues[1]
        val sequence = match.groupValues[2].toIntOrNull()
            ?: return null

        return individualName + (sequence + 1)
    }

    // 커스텀 일정 선택 시 기록 및 예정일 생성
    fun addEvent(
        type: CustomEventType,
        memo: String
    ) {
        val selectedDate = _uiState.value.selectedDate
        val safeMemo = memo.trim()

        viewModelScope.launch {
            database.withTransaction {

                // 원본 기록 저장
                val eventId = dao.insertEvent(
                    CalendarEvent(
                        eventName = type.name,
                        customEventTypeId = type.id,
                        eventDate = selectedDate,
                        isScheduled = false,
                        memo = safeMemo
                    )
                )

                if (type.name.trim() == "산란") {

                    val nextMemo = incrementIndividualNumber(safeMemo)

                    if (nextMemo != null) {

                        val scheduledEvents = mutableListOf<CalendarEvent>()

                        // 다음 산란 예정일
                        if (type.repeatIntervalDays > 0) {
                            scheduledEvents.add(
                                CalendarEvent(
                                    eventName = type.name,
                                    customEventTypeId = type.id,
                                    eventDate = selectedDate + type.repeatIntervalDays,
                                    isScheduled = true,
                                    sourceEventId = eventId,
                                    memo = nextMemo
                                )
                            )
                        }

                        // 해칭 예정일: 산란일 기준 80일
                        scheduledEvents.add(
                            CalendarEvent(
                                eventName = "해칭",
                                customEventTypeId = type.id,
                                eventDate = selectedDate + 80,
                                isScheduled = true,
                                sourceEventId = eventId,
                                memo = safeMemo
                            )
                        )

                        if (scheduledEvents.isNotEmpty()) {
                            dao.insertEvents(scheduledEvents)
                        }
                    }

                } else if (type.repeatIntervalDays > 0) {

                    // 일반 커스텀 일정은 기존 반복 주기 유지
                    dao.insertEvent(
                        CalendarEvent(
                            eventName = type.name,
                            customEventTypeId = type.id,
                            eventDate = selectedDate + type.repeatIntervalDays,
                            isScheduled = true,
                            sourceEventId = eventId,
                            memo = safeMemo
                        )
                    )
                }
            }
        }
    }

    // 커스텀 일정 미선택 시 메모만 기록
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
