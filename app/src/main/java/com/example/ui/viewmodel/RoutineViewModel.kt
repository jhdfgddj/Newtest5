package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DataQualityStatus
import com.example.data.model.NoteItem
import com.example.data.model.Routine
import com.example.data.model.RoutineEntry
import com.example.data.model.TaskItem
import com.example.data.model.UserPreferences
import com.example.data.repository.ParseProgressState
import com.example.data.repository.RoutineRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import com.example.data.model.RoutineNotification

enum class RoutineTab {
    HOME,
    ROUTINE,
    CALENDAR,
    TASKS,
    SAVED,
    NOTIFICATIONS,
    SETTINGS
}

enum class RoutineViewType {
    DAY_TIMELINE,
    LIST_TABLE
}

data class ExamCountdown(
    val entry: RoutineEntry,
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isHappeningNow: Boolean,
    val isCompleted: Boolean,
    val formattedCountdownString: String
)

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RoutineRepository(application)

    val allRoutines: StateFlow<List<Routine>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    // Active routine
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeRoutine: StateFlow<Routine?> = combine(allRoutines, userPreferences) { routines, prefs ->
        if (routines.isEmpty()) null
        else routines.find { it.id == prefs.activeRoutineId } ?: routines.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val routineEntries: StateFlow<List<RoutineEntry>> = activeRoutine.flatMapLatest { routine ->
        if (routine == null) flowOf(emptyList())
        else repository.getEntriesForRoutine(routine.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TaskItem>> = activeRoutine.flatMapLatest { routine ->
        if (routine == null) flowOf(emptyList())
        else repository.getTasksForRoutine(routine.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<NoteItem>> = activeRoutine.flatMapLatest { routine ->
        if (routine == null) flowOf(emptyList())
        else repository.getNotesForRoutine(routine.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Parsing progress
    private val _parseProgress = MutableStateFlow(ParseProgressState())
    val parseProgress: StateFlow<ParseProgressState> = _parseProgress.asStateFlow()

    // Bookmarks / Saved Exams
    val bookmarkedEntryIds = MutableStateFlow<Set<String>>(emptySet())

    val savedEntries: StateFlow<List<RoutineEntry>> = combine(routineEntries, bookmarkedEntryIds) { entries, ids ->
        entries.filter { ids.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    private val _notifications = MutableStateFlow<List<RoutineNotification>>(
        listOf(
            RoutineNotification(
                id = "notif_1",
                title = "Upcoming Exam Reminder",
                message = "Your next scheduled exam is approaching soon. Review your preparation checklist.",
                timestamp = "Today, 08:00 AM",
                isRead = false,
                category = "Exam Alert"
            ),
            RoutineNotification(
                id = "notif_2",
                title = "Routine Successfully Loaded",
                message = "BTEB Semester Exam Routine parsed with 100% data verification.",
                timestamp = "Yesterday",
                isRead = true,
                category = "Import"
            ),
            RoutineNotification(
                id = "notif_3",
                title = "Data Quality Verification",
                message = "All subject codes, times, and semesters cross-verified with BTEB standards.",
                timestamp = "2 days ago",
                isRead = true,
                category = "System"
            )
        )
    )
    val notifications: StateFlow<List<RoutineNotification>> = _notifications.asStateFlow()

    val showSearchOverlay = MutableStateFlow(false)

    // Filters and Search
    val searchQuery = MutableStateFlow("")
    val selectedTechnologyFilter = MutableStateFlow<String?>(null)
    val selectedSemesterFilter = MutableStateFlow<String?>(null)
    val selectedDateFilter = MutableStateFlow<String?>(null)
    val selectedQualityFilter = MutableStateFlow<DataQualityStatus?>(null)
    val isMyRoutineMode = MutableStateFlow(false)

    val activeTab = MutableStateFlow(RoutineTab.HOME)
    val routineViewType = MutableStateFlow(RoutineViewType.DAY_TIMELINE)

    val selectedEntryForModal = MutableStateFlow<RoutineEntry?>(null)
    val showRoutineManagerModal = MutableStateFlow(false)
    val showExportModal = MutableStateFlow(false)

    // Ticker flow emitting current timestamp every second
    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    // Unique technologies extracted from current routine
    val uniqueTechnologies: StateFlow<List<String>> = routineEntries.map { list ->
        list.flatMap { it.technology }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique semesters
    val uniqueSemesters: StateFlow<List<String>> = routineEntries.map { list ->
        list.flatMap { it.semester }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique dates
    val uniqueDates: StateFlow<List<String>> = routineEntries.map { list ->
        list.map { it.date }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered routine entries
    val filteredEntries: StateFlow<List<RoutineEntry>> = combine(
        routineEntries,
        searchQuery,
        selectedTechnologyFilter,
        selectedSemesterFilter,
        selectedDateFilter,
        selectedQualityFilter,
        isMyRoutineMode,
        userPreferences
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val entries = args[0] as List<RoutineEntry>
        val query = args[1] as String
        val tech = args[2] as String?
        val sem = args[3] as String?
        val date = args[4] as String?
        val quality = args[5] as DataQualityStatus?
        val myRoutine = args[6] as Boolean
        val prefs = args[7] as UserPreferences

        entries.filter { entry ->
            // Search filter
            val matchesQuery = query.isBlank() ||
                    entry.subjectName.contains(query, ignoreCase = true) ||
                    entry.subjectCode.contains(query, ignoreCase = true) ||
                    entry.date.contains(query, ignoreCase = true) ||
                    entry.day.contains(query, ignoreCase = true) ||
                    entry.technology.any { it.contains(query, ignoreCase = true) }

            // Technology filter
            val matchesTech = if (myRoutine && !prefs.selectedTechnology.isNullOrBlank()) {
                entry.technology.any { it.equals(prefs.selectedTechnology, ignoreCase = true) }
            } else if (!tech.isNullOrBlank()) {
                entry.technology.any { it.equals(tech, ignoreCase = true) }
            } else true

            // Semester filter
            val matchesSem = if (myRoutine && !prefs.selectedSemester.isNullOrBlank()) {
                entry.semester.any { it.equals(prefs.selectedSemester, ignoreCase = true) }
            } else if (!sem.isNullOrBlank()) {
                entry.semester.any { it.equals(sem, ignoreCase = true) }
            } else true

            // Date filter
            val matchesDate = date == null || entry.date == date

            // Data quality filter
            val matchesQuality = quality == null || entry.status == quality

            matchesQuery && matchesTech && matchesSem && matchesDate && matchesQuality
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Next upcoming exam countdown
    val nextExamCountdown: StateFlow<ExamCountdown?> = combine(routineEntries, tickerFlow, userPreferences) { entries, currentTime, prefs ->
        val upcoming = entries.map { entry ->
            calculateCountdown(entry, currentTime, prefs.timeZone)
        }.filter { !it.isCompleted }
            .sortedBy { parseExamTimestamp(it.entry.date, it.entry.time, prefs.timeZone) }

        upcoming.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Today's schedule
    val todaysExams: StateFlow<List<RoutineEntry>> = combine(routineEntries, tickerFlow, userPreferences) { entries, currentTime, prefs ->
        val sdf = SimpleDateFormat("yyyy-MM-DD", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone(prefs.timeZone)
        val todayIso = sdf.format(Date(currentTime))
        entries.filter { it.date == todayIso }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Auto-load sample routine if DB is empty
        viewModelScope.launch {
            val routines = repository.allRoutines.first()
            if (routines.isEmpty()) {
                repository.loadSampleRoutine()
            }
        }
    }

    fun uploadPdf(uri: Uri, fileName: String) {
        viewModelScope.launch {
            repository.processPdfUpload(uri, fileName) { state ->
                _parseProgress.value = state
            }
        }
    }

    fun resetParseProgress() {
        _parseProgress.value = ParseProgressState()
    }

    fun loadSampleData() {
        viewModelScope.launch {
            _parseProgress.value = ParseProgressState(step = 1, message = "Loading BTEB Sample Routine...")
            delay(400)
            repository.loadSampleRoutine()
            _parseProgress.value = ParseProgressState(step = 6, message = "Completed!")
        }
    }

    fun selectRoutine(id: String) {
        viewModelScope.launch {
            repository.updateActiveRoutineId(id)
        }
    }

    fun deleteRoutine(id: String) {
        viewModelScope.launch {
            repository.deleteRoutine(id)
        }
    }

    fun duplicateRoutine(id: String) {
        viewModelScope.launch {
            repository.duplicateRoutine(id)
        }
    }

    fun renameRoutine(id: String, newName: String) {
        viewModelScope.launch {
            repository.renameRoutine(id, newName)
        }
    }

    fun updatePersonalizedPreferences(tech: String?, sem: String?, reg: String?) {
        viewModelScope.launch {
            val current = userPreferences.value
            repository.updatePreferences(
                current.copy(
                    selectedTechnology = tech,
                    selectedSemester = sem,
                    selectedRegulation = reg
                )
            )
        }
    }

    fun updateThemeMode(themeMode: String) {
        viewModelScope.launch {
            val current = userPreferences.value
            repository.updatePreferences(current.copy(themeMode = themeMode))
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val current = userPreferences.value
            repository.updatePreferences(current.copy(language = lang))
        }
    }

    fun updateTimeFormat(format: String) {
        viewModelScope.launch {
            val current = userPreferences.value
            repository.updatePreferences(current.copy(timeFormat = format))
        }
    }

    fun updateEntryProgress(entryId: String, progress: Int) {
        viewModelScope.launch {
            repository.updateEntryPrepProgress(entryId, progress)
        }
    }

    fun updateEntry(entry: RoutineEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry)
        }
    }

    fun addTask(title: String, entryId: String? = null) {
        val routineId = activeRoutine.value?.id ?: return
        viewModelScope.launch {
            repository.addTask(
                TaskItem(
                    id = java.util.UUID.randomUUID().toString(),
                    routineId = routineId,
                    entryId = entryId,
                    title = title
                )
            )
        }
    }

    fun toggleTask(taskId: String) {
        val routineId = activeRoutine.value?.id ?: return
        viewModelScope.launch {
            repository.toggleTaskCompleted(taskId, routineId)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun addNote(title: String, content: String, entryId: String? = null) {
        val routineId = activeRoutine.value?.id ?: return
        viewModelScope.launch {
            repository.addNote(
                NoteItem(
                    id = java.util.UUID.randomUUID().toString(),
                    routineId = routineId,
                    entryId = entryId,
                    title = title,
                    content = content
                )
            )
        }
    }

    fun toggleBookmark(entryId: String) {
        val current = bookmarkedEntryIds.value
        if (current.contains(entryId)) {
            bookmarkedEntryIds.value = current - entryId
        } else {
            bookmarkedEntryIds.value = current + entryId
        }
    }

    fun markNotificationRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    fun clearFilters() {
        searchQuery.value = ""
        selectedTechnologyFilter.value = null
        selectedSemesterFilter.value = null
        selectedDateFilter.value = null
        selectedQualityFilter.value = null
        isMyRoutineMode.value = false
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    suspend fun getExportJson(): String {
        val activeId = activeRoutine.value?.id ?: return "{}"
        return repository.exportRoutineToJson(activeId)
    }

    suspend fun getExportCsv(): String {
        val activeId = activeRoutine.value?.id ?: return ""
        return repository.exportRoutineToCsv(activeId)
    }

    private fun calculateCountdown(entry: RoutineEntry, currentTime: Long, timeZone: String): ExamCountdown {
        val examTime = parseExamTimestamp(entry.date, entry.time, timeZone)
        val diffMillis = examTime - currentTime

        val examDurationMillis = 3 * 3600 * 1000L // Assume 3 hours exam duration

        val isHappeningNow = diffMillis <= 0 && diffMillis >= -examDurationMillis
        val isCompleted = diffMillis < -examDurationMillis

        val totalSeconds = if (diffMillis > 0) diffMillis / 1000 else 0
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val formattedStr = when {
            isCompleted -> "Completed"
            isHappeningNow -> "Exam in progress"
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
            else -> "${minutes}m ${seconds}s"
        }

        return ExamCountdown(
            entry = entry,
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            isHappeningNow = isHappeningNow,
            isCompleted = isCompleted,
            formattedCountdownString = formattedStr
        )
    }

    private fun parseExamTimestamp(dateIso: String, timeStr: String, timeZone: String): Long {
        return try {
            val dateTimeStr = "$dateIso $timeStr"
            val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone(timeZone)
            val date = sdf.parse(dateTimeStr)
            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis() + 86400000L
        }
    }
}
