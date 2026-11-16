package com.example.breedingcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.breedingcalendar.data.entity.CalendarEvent
import com.example.breedingcalendar.data.entity.CustomEventType
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.math.abs

private val CalendarBackground = Color(0xFF121212)
private val CalendarSurface = Color(0xFF1E1E1E)
private val CalendarSelected = Color(0xFF345B75)
private val CalendarAccent = Color(0xFF80CBC4)
private val CalendarText = Color(0xFFEAEAEA)
private val CalendarSubText = Color(0xFF999999)

private val EventRecordColor = Color(0xFF80DEEA)
private val EventScheduledColor = Color(0xFFFFB74D)

private val EventRecordBorder = Color(0xFF26A69A)
private val EventScheduledBorder = Color(0xFFFF9800)

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel
) {
    val state by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showCustomTypeDialog by remember { mutableStateOf(false) }
    var showDeleteCustomTypeDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteCustomTypesDialog by remember { mutableStateOf(false) }

    var showEditCustomTypeListDialog by remember { mutableStateOf(false) }
    var customEventTypeToEdit by remember { mutableStateOf<CustomEventType?>(null) }

    val selectedCustomTypeIds = remember {
        mutableStateListOf<Long>()
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<CalendarEvent?>(null) }
    var isCalendarExpanded by remember { mutableStateOf(true) }

    val selectedEvents = state.events.filter {
        it.eventDate == state.selectedDate
    }

    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = CalendarAccent,
            secondary = CalendarAccent,
            background = CalendarBackground,
            surface = CalendarSurface,
            onPrimary = Color.Black,
            onBackground = CalendarText,
            onSurface = CalendarText
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CalendarBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        TextButton(
                            onClick = {
                                menuExpanded = true
                            }
                        ) {
                            Text(
                                text = "⋮",
                                color = CalendarText,
                                fontSize = 28.sp
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = {
                                menuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text("커스텀 일정 추가") },
                                onClick = {
                                    menuExpanded = false
                                    showCustomTypeDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("커스텀 일정 수정") },
                                onClick = {
                                    menuExpanded = false
                                    showEditCustomTypeListDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("커스텀 일정 제거") },
                                onClick = {
                                    menuExpanded = false
                                    selectedCustomTypeIds.clear()
                                    showDeleteCustomTypeDialog = true
                                }
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            viewModel.goToToday()
                        }
                    ) {
                        Text(
                            text = "오늘",
                            color = CalendarAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.currentYear}년 ${state.currentMonth + 1}월",
                        color = CalendarText,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "‹",
                            modifier = Modifier
                                .clickable { viewModel.moveMonth(-1) }
                                .padding(horizontal = 14.dp),
                            color = CalendarText,
                            fontSize = 32.sp
                        )

                        Text(
                            text = "›",
                            modifier = Modifier
                                .clickable { viewModel.moveMonth(1) }
                                .padding(horizontal = 14.dp),
                            color = CalendarText,
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val weekdays = listOf("일", "월", "화", "수", "목", "금", "토")

                    weekdays.forEachIndexed { index, day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = when (index) {
                                0 -> Color(0xFFE57373)
                                6 -> Color(0xFF64B5F6)
                                else -> CalendarSubText
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val firstDay = CalendarViewModel.dateToEpochDay(
                    state.currentYear,
                    state.currentMonth,
                    1
                )

                val firstCalendar =
                    CalendarViewModel.epochDayToCalendar(firstDay)

                val firstWeekday =
                    firstCalendar.get(Calendar.DAY_OF_WEEK) - 1

                val daysInMonth = GregorianCalendar(
                    state.currentYear,
                    state.currentMonth,
                    1
                ).getActualMaximum(Calendar.DAY_OF_MONTH)

                val calendarCells = (0 until 42).map { index ->
                    val day = index - firstWeekday + 1

                    if (day in 1..daysInMonth) {
                        CalendarViewModel.dateToEpochDay(
                            state.currentYear,
                            state.currentMonth,
                            day
                        )
                    } else {
                        null
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(
                            isCalendarExpanded,
                            state.currentYear,
                            state.currentMonth
                        ) {
                            var dragX = 0f
                            var dragY = 0f

                            detectDragGestures(
                                onDragStart = {
                                    dragX = 0f
                                    dragY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    dragX += dragAmount.x
                                    dragY += dragAmount.y
                                    change.consume()
                                },
                                onDragEnd = {
                                    val threshold = 70f

                                    if (
                                        abs(dragX) > abs(dragY) &&
                                        abs(dragX) > threshold
                                    ) {
                                        if (dragX < 0) {
                                            viewModel.moveMonth(1)
                                        } else {
                                            viewModel.moveMonth(-1)
                                        }
                                    } else if (
                                        abs(dragY) > abs(dragX) &&
                                        abs(dragY) > threshold
                                    ) {
                                        isCalendarExpanded = dragY > 0
                                    }
                                }
                            )
                        }
                ) {
                    calendarCells.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            week.forEachIndexed { index, date ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (isCalendarExpanded) {
                                                Modifier.height(72.dp)
                                            } else {
                                                Modifier.aspectRatio(0.78f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (date != null) {
                                        val calendar =
                                            CalendarViewModel.epochDayToCalendar(date)

                                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                                        val isSelected = date == state.selectedDate

                                        val today = Calendar.getInstance()

                                        val isToday =
                                            date == CalendarViewModel.dateToEpochDay(
                                                today.get(Calendar.YEAR),
                                                today.get(Calendar.MONTH),
                                                today.get(Calendar.DAY_OF_MONTH)
                                            )

                                        val dateEvents = state.events.filter {
                                            it.eventDate == date
                                        }

                                        val hasEvents = dateEvents.isNotEmpty()

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    viewModel.selectDate(date)
                                                    showAddDialog = true
                                                }
                                                .background(
                                                    color = if (isSelected) {
                                                        CalendarSelected
                                                    } else {
                                                        Color.Transparent
                                                    }
                                                )
                                                .padding(
                                                    horizontal = 2.dp,
                                                    vertical = 7.dp
                                                ),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .background(
                                                        color = if (isToday) {
                                                            CalendarAccent
                                                        } else {
                                                            Color.Transparent
                                                        },
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = day.toString(),
                                                    color = if (isToday) {
                                                        Color.Black
                                                    } else if (index == 0) {
                                                        Color(0xFFE57373)
                                                    } else if (index == 6) {
                                                        Color(0xFF64B5F6)
                                                    } else {
                                                        CalendarText
                                                    },
                                                    fontSize = 14.sp,
                                                    fontWeight = if (
                                                        isSelected || isToday
                                                    ) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Normal
                                                    }
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            if (isCalendarExpanded) {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    dateEvents.take(2).forEach { event ->
                                                        val eventTextColor =
                                                            if (event.isScheduled) {
                                                                EventScheduledColor
                                                            } else {
                                                                EventRecordColor
                                                            }

                                                        val eventBorderColor =
                                                            if (event.isScheduled) {
                                                                EventScheduledBorder
                                                            } else {
                                                                EventRecordBorder
                                                            }

                                                        Text(
                                                            text = calendarDayEventDisplayText(event),
                                                            color = eventTextColor,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(5.dp))
                                                                .border(
                                                                    width = 0.7.dp,
                                                                    color = eventBorderColor,
                                                                    shape = RoundedCornerShape(5.dp)
                                                                )
                                                                .background(
                                                                    eventBorderColor.copy(alpha = 0.10f),
                                                                    RoundedCornerShape(5.dp)
                                                                )
                                                                .padding(
                                                                    horizontal = 2.dp,
                                                                    vertical = 2.dp
                                                                )
                                                        )
                                                    }

                                                    if (dateEvents.size > 2) {
                                                        Text(
                                                            text = "...",
                                                            color = CalendarSubText,
                                                            fontSize = 8.sp,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.fillMaxWidth()
                                                        )
                                                    }
                                                }
                                            } else if (hasEvents) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(CalendarAccent, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDate(state.selectedDate),
                        color = CalendarText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "+ 일정 추가",
                        modifier = Modifier
                            .clickable { showAddDialog = true }
                            .padding(8.dp),
                        color = CalendarAccent,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (selectedEvents.isEmpty()) {
                        Text(
                            text = "등록된 일정이 없습니다.",
                            color = CalendarSubText,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(selectedEvents) { event ->
                                EventItem(
                                    event = event,
                                    onLongClick = {
                                        eventToDelete = event
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            customEventTypes = state.customEventTypes,
            onDismiss = {
                showAddDialog = false
            },
            onSaveCustom = { types, group, number, memo ->
                viewModel.addEvent(
                    types = types,
                    individualGroup = group,
                    individualNumber = number,
                    memo = memo
                )
                showAddDialog = false
            },
            onSaveMemo = { memo ->
                viewModel.addMemoEvent(memo)
                showAddDialog = false
            }
        )
    }

    if (showCustomTypeDialog) {
        AddCustomEventTypeDialog(
            onDismiss = {
                showCustomTypeDialog = false
            },
            onSave = { name, intervalDays, incrementNumber ->
                viewModel.addCustomEventType(
                    name,
                    intervalDays,
                    incrementNumber
                )
                showCustomTypeDialog = false
            }
        )
    }

    if (showEditCustomTypeListDialog) {
        SelectCustomEventTypeDialog(
            customEventTypes = state.customEventTypes,
            onDismiss = {
                showEditCustomTypeListDialog = false
            },
            onSelect = { type ->
                customEventTypeToEdit = type
                showEditCustomTypeListDialog = false
            }
        )
    }

    customEventTypeToEdit?.let { type ->
        EditCustomEventTypeDialog(
            customEventType = type,
            onDismiss = {
                customEventTypeToEdit = null
            },
            onSave = { name, intervalDays, incrementNumber ->
                viewModel.updateCustomEventType(
                    type.copy(
                        name = name,
                        repeatIntervalDays = intervalDays,
                        incrementNumber = incrementNumber
                    )
                )
                customEventTypeToEdit = null
            }
        )
    }

    if (showDeleteCustomTypeDialog) {
        DeleteCustomEventTypeDialog(
            customEventTypes = state.customEventTypes,
            selectedTypeIds = selectedCustomTypeIds,
            onDismiss = {
                showDeleteCustomTypeDialog = false
                selectedCustomTypeIds.clear()
            },
            onConfirm = {
                showDeleteCustomTypeDialog = false
                showConfirmDeleteCustomTypesDialog = true
            }
        )
    }

    if (showConfirmDeleteCustomTypesDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDeleteCustomTypesDialog = false
                selectedCustomTypeIds.clear()
            },
            containerColor = CalendarSurface,
            title = {
                Text(
                    text = "커스텀 일정 삭제",
                    color = CalendarText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "해당 커스텀 일정을 제거하면 등록되어 있는 모든 일정이 캘린더에서 제거됩니다. 정말 삭제하시겠습니까?",
                    color = CalendarText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomEventTypes(
                            selectedCustomTypeIds.toList()
                        )
                        selectedCustomTypeIds.clear()
                        showConfirmDeleteCustomTypesDialog = false
                    }
                ) {
                    Text("확인", color = CalendarAccent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDeleteCustomTypesDialog = false
                        selectedCustomTypeIds.clear()
                    }
                ) {
                    Text("취소", color = CalendarSubText)
                }
            }
        )
    }

    eventToDelete?.let { event ->
        AlertDialog(
            onDismissRequest = {
                eventToDelete = null
            },
            containerColor = CalendarSurface,
            title = {
                Text(
                    text = "일정 삭제",
                    color = CalendarText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "삭제하시겠습니까?",
                    color = CalendarText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvent(event)
                        eventToDelete = null
                    }
                ) {
                    Text("확인", color = CalendarAccent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        eventToDelete = null
                    }
                ) {
                    Text("취소", color = CalendarSubText)
                }
            }
        )
    }
}

@Composable
private fun SelectCustomEventTypeDialog(
    customEventTypes: List<CustomEventType>,
    onDismiss: () -> Unit,
    onSelect: (CustomEventType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CalendarSurface,
        title = {
            Text(
                text = "수정할 커스텀 일정 선택",
                color = CalendarText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (customEventTypes.isEmpty()) {
                Text(
                    text = "등록된 커스텀 일정이 없습니다.",
                    color = CalendarSubText
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(customEventTypes) { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(type) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = type.name,
                                    color = CalendarText,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${type.repeatIntervalDays}일 간격",
                                    color = CalendarSubText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = CalendarSubText)
            }
        }
    )
}

@Composable
private fun EditCustomEventTypeDialog(
    customEventType: CustomEventType,
    onDismiss: () -> Unit,
    onSave: (String, Int, Boolean) -> Unit
) {
    var name by remember(customEventType.id) {
        mutableStateOf(customEventType.name)
    }

    var intervalText by remember(customEventType.id) {
        mutableStateOf(customEventType.repeatIntervalDays.toString())
    }

    var incrementNumber by remember(customEventType.id) {
        mutableStateOf(customEventType.incrementNumber)
    }

    val interval = intervalText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CalendarSurface,
        title = {
            Text(
                text = "커스텀 일정 수정",
                color = CalendarText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("일정 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.all { it.isDigit() }) {
                            intervalText = value
                        }
                    },
                    label = { Text("일정 생성 간격 (Day)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = incrementNumber,
                        onCheckedChange = { incrementNumber = it }
                    )

                    Text(
                        text = "자동 생성 시 번호 +1",
                        color = CalendarText
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name.trim(),
                        interval!!,
                        incrementNumber
                    )
                },
                enabled = name.isNotBlank() &&
                        interval != null &&
                        interval >= 0
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun DeleteCustomEventTypeDialog(
    customEventTypes: List<CustomEventType>,
    selectedTypeIds: SnapshotStateList<Long>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CalendarSurface,
        title = {
            Text(
                text = "커스텀 일정 제거",
                color = CalendarText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (customEventTypes.isEmpty()) {
                Text(
                    text = "등록된 커스텀 일정이 없습니다.",
                    color = CalendarSubText
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(customEventTypes) { type ->
                        val isSelected = type.id in selectedTypeIds

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedTypeIds.remove(type.id)
                                    } else {
                                        selectedTypeIds.add(type.id)
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (type.id !in selectedTypeIds) {
                                            selectedTypeIds.add(type.id)
                                        }
                                    } else {
                                        selectedTypeIds.remove(type.id)
                                    }
                                }
                            )

                            Text(
                                text = type.name,
                                color = CalendarText
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = selectedTypeIds.isNotEmpty()
            ) {
                Text("확인", color = CalendarAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = CalendarSubText)
            }
        }
    )
}

// 캘린더 날짜 칸
private fun calendarDayEventDisplayText(event: CalendarEvent): String {
    val prefix = listOf(
        event.individualGroup,
        event.individualNumber?.toString().orEmpty()
    )
        .filter { it.isNotBlank() }
        .joinToString(" ")

    val suffix = if (event.isScheduled) {
        event.eventName
    } else {
        event.memo
    }

    return listOf(prefix, suffix)
        .filter { it.isNotBlank() }
        .joinToString(" ")
}

// Select Day
private fun customDayDisplayText(event: CalendarEvent): String {
    val prefix = listOf(
        event.individualGroup,
        event.individualNumber?.toString().orEmpty()
    )
        .filter { it.isNotBlank() }
        .joinToString(" ")

    val suffix = if (event.isScheduled) {
        event.eventName
    } else {
        event.memo
    }

    return listOf(prefix, suffix)
        .filter { it.isNotBlank() }
        .joinToString(" ")
}

@Composable
private fun EventItem(
    event: CalendarEvent,
    onLongClick: () -> Unit
) {
    val displayName = customDayDisplayText(event)

    val textColor = if (event.isScheduled) {
        EventScheduledColor
    } else {
        EventRecordColor
    }

    val borderColor = if (event.isScheduled) {
        EventScheduledBorder
    } else {
        EventRecordBorder
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CalendarSurface)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (event.isScheduled) {
                        EventScheduledColor
                    } else {
                        CalendarAccent
                    },
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = displayName,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = if (event.isScheduled) "" else "",
            color = textColor,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun AddEventDialog(
    customEventTypes: List<CustomEventType>,
    onDismiss: () -> Unit,
    onSaveCustom: (List<CustomEventType>, String, Int, String) -> Unit,
    onSaveMemo: (String) -> Unit
) {
    val selectedTypeIds = remember {
        mutableStateListOf<Long>()
    }

    var individualGroup by remember { mutableStateOf("") }
    var individualNumberText by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    val individualNumber = individualNumberText.toIntOrNull()

    val selectedTypes = customEventTypes.filter {
        it.id in selectedTypeIds
    }

    val hasAnyRecordInput =
        individualGroup.isNotBlank() || individualNumberText.isNotBlank()

    val hasActualRecord =
        individualGroup.isNotBlank() && individualNumber != null

    val canSave = hasActualRecord ||
            (!hasAnyRecordInput && memo.isNotBlank())

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(
                    top = 80.dp,
                    bottom = 16.dp
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .heightIn(max = maxHeight - 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = CalendarSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "일정 추가",
                        color = CalendarText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = "실제 기록",
                            color = CalendarAccent,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = individualGroup,
                            onValueChange = { individualGroup = it },
                            label = { Text("개체 구분") },
                            placeholder = { Text("예: 아잔틱1") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = individualNumberText,
                            onValueChange = { value ->
                                if (
                                    value.isEmpty() ||
                                    value.all { it.isDigit() }
                                ) {
                                    individualNumberText = value
                                }
                            },
                            label = { Text("번호") },
                            placeholder = { Text("예: 1") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "자동 생성할 커스텀 일정",
                            color = CalendarAccent,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (customEventTypes.isEmpty()) {
                            Text(
                                text = "등록된 커스텀 일정이 없습니다.",
                                color = CalendarSubText,
                                fontSize = 12.sp
                            )
                        } else {
                            customEventTypes.forEach { type ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (type.id in selectedTypeIds) {
                                                selectedTypeIds.remove(type.id)
                                            } else {
                                                selectedTypeIds.add(type.id)
                                            }
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = type.id in selectedTypeIds,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                if (type.id !in selectedTypeIds) {
                                                    selectedTypeIds.add(type.id)
                                                }
                                            } else {
                                                selectedTypeIds.remove(type.id)
                                            }
                                        }
                                    )

                                    Column {
                                        Text(
                                            text = type.name,
                                            color = CalendarText
                                        )

                                        Text(
                                            text = if (type.repeatIntervalDays > 0) {
                                                "${type.repeatIntervalDays}일 후 일정 생성"
                                            } else {
                                                "자동 일정 생성 안 함"
                                            },
                                            color = CalendarSubText,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            label = { Text("메모") },
                            placeholder = { Text("예: 산란") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "선택한 커스텀 일정은 각각 설정된 주기로 생성됩니다.",
                            color = CalendarSubText,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text(
                                "취소",
                                color = CalendarSubText
                            )
                        }

                        Button(
                            onClick = {
                                if (hasActualRecord) {
                                    onSaveCustom(
                                        selectedTypes,
                                        individualGroup,
                                        individualNumber,
                                        memo
                                    )
                                } else if (
                                    !hasAnyRecordInput &&
                                    memo.isNotBlank()
                                ) {
                                    onSaveMemo(memo)
                                }
                            },
                            enabled = canSave
                        ) {
                            Text("저장")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCustomEventTypeDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var intervalText by remember { mutableStateOf("") }
    var incrementNumber by remember { mutableStateOf(false) }

    val interval = intervalText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CalendarSurface,
        title = {
            Text(
                text = "커스텀 일정 추가",
                color = CalendarText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("일정 이름") },
                    placeholder = { Text("예: 산란") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.all { it.isDigit() }) {
                            intervalText = value
                        }
                    },
                    label = { Text("일정 생성 간격 (Day)") },
                    placeholder = { Text("예: 30") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = incrementNumber,
                        onCheckedChange = { incrementNumber = it }
                    )

                    Text(
                        text = "자동 생성 시 번호 +1",
                        color = CalendarText
                    )
                }

                Text(
                    text = if (incrementNumber) {
                        "예: 아잔틱1 1 → 아잔틱1 2"
                    } else {
                        "기존 번호를 그대로 사용합니다."
                    },
                    color = CalendarSubText,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name.trim(),
                        interval!!,
                        incrementNumber
                    )
                },
                enabled = name.isNotBlank() &&
                        interval != null &&
                        interval >= 0
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

private fun formatDate(epochDay: Long): String {
    val calendar = CalendarViewModel.epochDayToCalendar(epochDay)

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return "${year}년 ${month}월 ${day}일"
}