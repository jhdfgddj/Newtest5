package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.components.CalendarView
import com.example.ui.components.DayTimelineView
import com.example.ui.components.ExamDetailModal
import com.example.ui.components.FilterPanel
import com.example.ui.components.NextExamCard
import com.example.ui.components.NotesPanel
import com.example.ui.components.NotificationCenterScreen
import com.example.ui.components.PersonalizedFilterBar
import com.example.ui.components.QuickDateStrip
import com.example.ui.components.RoutineListView
import com.example.ui.components.RoutineManagerModal
import com.example.ui.components.SavedExamsScreen
import com.example.ui.components.SearchOverlay
import com.example.ui.components.StatsOverview
import com.example.ui.components.TaskListPanel
import com.example.ui.components.TodayScheduleSection
import com.example.ui.components.UpcomingExamsSection
import com.example.ui.components.UploadDropzoneScreen
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.RoutineTab
import com.example.ui.viewmodel.RoutineViewModel
import com.example.ui.viewmodel.RoutineViewType
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: RoutineViewModel) {
    val activeRoutine by viewModel.activeRoutine.collectAsState()
    val allRoutines by viewModel.allRoutines.collectAsState()
    val routineEntries by viewModel.routineEntries.collectAsState()
    val filteredEntries by viewModel.filteredEntries.collectAsState()
    val savedEntries by viewModel.savedEntries.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedEntryIds.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val nextExam by viewModel.nextExamCountdown.collectAsState()
    val todaysExams by viewModel.todaysExams.collectAsState()
    val userPrefs by viewModel.userPreferences.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val parseProgress by viewModel.parseProgress.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTech by viewModel.selectedTechnologyFilter.collectAsState()
    val selectedSem by viewModel.selectedSemesterFilter.collectAsState()
    val selectedDate by viewModel.selectedDateFilter.collectAsState()
    val selectedQuality by viewModel.selectedQualityFilter.collectAsState()
    val isMyRoutine by viewModel.isMyRoutineMode.collectAsState()

    val activeTab by viewModel.activeTab.collectAsState()
    val routineViewType by viewModel.routineViewType.collectAsState()

    val selectedEntryForModal by viewModel.selectedEntryForModal.collectAsState()
    val showRoutineManagerModal by viewModel.showRoutineManagerModal.collectAsState()
    val showExportModal by viewModel.showExportModal.collectAsState()
    val showSearchOverlay by viewModel.showSearchOverlay.collectAsState()

    val uniqueTechnologies by viewModel.uniqueTechnologies.collectAsState()
    val uniqueSemesters by viewModel.uniqueSemesters.collectAsState()
    val uniqueDates by viewModel.uniqueDates.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var showUploadModal by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppHeader(
                activeRoutine = activeRoutine,
                allRoutines = allRoutines,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.searchQuery.value = it },
                themeMode = userPrefs.themeMode,
                onThemeToggle = {
                    val nextMode = if (userPrefs.themeMode == "DARK") "LIGHT" else "DARK"
                    viewModel.updateThemeMode(nextMode)
                },
                onSelectRoutine = { viewModel.selectRoutine(it) },
                onOpenRoutineManager = { viewModel.showRoutineManagerModal.value = true },
                onUploadClick = { showUploadModal = true },
                onOpenSearch = { viewModel.showSearchOverlay.value = true },
                onOpenNotifications = { viewModel.activeTab.value = RoutineTab.NOTIFICATIONS },
                unreadNotificationCount = notifications.count { !it.isRead }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == RoutineTab.HOME,
                    onClick = { viewModel.activeTab.value = RoutineTab.HOME },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_home")
                )
                NavigationBarItem(
                    selected = activeTab == RoutineTab.ROUTINE,
                    onClick = { viewModel.activeTab.value = RoutineTab.ROUTINE },
                    icon = { Icon(imageVector = Icons.Default.ListAlt, contentDescription = "Schedule") },
                    label = { Text("Schedule", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_routine")
                )
                NavigationBarItem(
                    selected = activeTab == RoutineTab.CALENDAR,
                    onClick = { viewModel.activeTab.value = RoutineTab.CALENDAR },
                    icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("Calendar", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_calendar")
                )
                NavigationBarItem(
                    selected = activeTab == RoutineTab.SAVED,
                    onClick = { viewModel.activeTab.value = RoutineTab.SAVED },
                    icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Saved") },
                    label = { Text("Saved", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_saved")
                )
                NavigationBarItem(
                    selected = activeTab == RoutineTab.TASKS,
                    onClick = { viewModel.activeTab.value = RoutineTab.TASKS },
                    icon = { Icon(imageVector = Icons.Default.Task, contentDescription = "Tasks") },
                    label = { Text("Tasks", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_tasks")
                )
                NavigationBarItem(
                    selected = activeTab == RoutineTab.SETTINGS,
                    onClick = { viewModel.activeTab.value = RoutineTab.SETTINGS },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                RoutineTab.HOME -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
                    ) {
                        // Overview stats
                        StatsOverview(
                            totalExams = routineEntries.size,
                            totalTechnologies = uniqueTechnologies.size,
                            totalSemesters = uniqueSemesters.size,
                            completionPercentage = 0
                        )

                        // Countdown to next exam as featured post
                        NextExamCard(
                            countdown = nextExam,
                            onClickEntry = { entry ->
                                viewModel.selectedEntryForModal.value = entry
                            }
                        )

                        // Today's schedule feed
                        TodayScheduleSection(
                            todaysExams = todaysExams,
                            onClickEntry = { entry ->
                                viewModel.selectedEntryForModal.value = entry
                            }
                        )

                        // All upcoming exams list
                        UpcomingExamsSection(
                            entries = filteredEntries,
                            onClickEntry = { entry ->
                                viewModel.selectedEntryForModal.value = entry
                            },
                            onSeeAllClick = {
                                viewModel.activeTab.value = RoutineTab.ROUTINE
                            }
                        )
                    }
                }

                RoutineTab.ROUTINE -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Personalized profile filter bar
                        PersonalizedFilterBar(
                            userPreferences = userPrefs,
                            isMyRoutineMode = isMyRoutine,
                            onToggleMyRoutineMode = {
                                viewModel.isMyRoutineMode.value = !isMyRoutine
                            },
                            availableTechnologies = uniqueTechnologies,
                            availableSemesters = uniqueSemesters,
                            onUpdatePreferences = { tech, sem, reg ->
                                viewModel.updatePersonalizedPreferences(tech, sem, reg)
                            }
                        )

                        // Date Stories / Horizontal Date Strip
                        QuickDateStrip(
                            availableDates = uniqueDates,
                            selectedDate = selectedDate,
                            onSelectDate = { viewModel.selectedDateFilter.value = it }
                        )

                        // View Type Selector & Filters Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Showing ${filteredEntries.size} of ${routineEntries.size} subjects",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Row {
                                IconButton(
                                    onClick = { viewModel.routineViewType.value = RoutineViewType.DAY_TIMELINE },
                                    modifier = Modifier.testTag("view_type_timeline_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewDay,
                                        contentDescription = "Timeline View",
                                        tint = if (routineViewType == RoutineViewType.DAY_TIMELINE) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.routineViewType.value = RoutineViewType.LIST_TABLE },
                                    modifier = Modifier.testTag("view_type_table_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewList,
                                        contentDescription = "List Table View",
                                        tint = if (routineViewType == RoutineViewType.LIST_TABLE) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Filter Chip Panel
                        FilterPanel(
                            selectedTechnology = selectedTech,
                            onSelectTechnology = { viewModel.selectedTechnologyFilter.value = it },
                            selectedSemester = selectedSem,
                            onSelectSemester = { viewModel.selectedSemesterFilter.value = it },
                            selectedDate = selectedDate,
                            onSelectDate = { viewModel.selectedDateFilter.value = it },
                            selectedQuality = selectedQuality,
                            onSelectQuality = { viewModel.selectedQualityFilter.value = it },
                            availableTechnologies = uniqueTechnologies,
                            availableSemesters = uniqueSemesters,
                            availableDates = uniqueDates,
                            onClearFilters = { viewModel.clearFilters() }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Schedule Feed Content
                        if (routineViewType == RoutineViewType.DAY_TIMELINE) {
                            DayTimelineView(
                                entries = filteredEntries,
                                onClickEntry = { entry ->
                                    viewModel.selectedEntryForModal.value = entry
                                }
                            )
                        } else {
                            RoutineListView(
                                entries = filteredEntries,
                                bookmarkedIds = bookmarkedIds,
                                onToggleBookmark = { id -> viewModel.toggleBookmark(id) },
                                onClickEntry = { entry ->
                                    viewModel.selectedEntryForModal.value = entry
                                }
                            )
                        }
                    }
                }

                RoutineTab.CALENDAR -> {
                    CalendarView(
                        entries = filteredEntries,
                        selectedDate = selectedDate,
                        onSelectDate = { viewModel.selectedDateFilter.value = it },
                        onClickEntry = { entry ->
                            viewModel.selectedEntryForModal.value = entry
                        }
                    )
                }

                RoutineTab.SAVED -> {
                    SavedExamsScreen(
                        savedEntries = savedEntries,
                        bookmarkedIds = bookmarkedIds,
                        onToggleBookmark = { id -> viewModel.toggleBookmark(id) },
                        onClickEntry = { entry ->
                            viewModel.selectedEntryForModal.value = entry
                        }
                    )
                }

                RoutineTab.TASKS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        var subTab by remember { mutableStateOf(0) }
                        TabRow(selectedTabIndex = subTab) {
                            Tab(
                                selected = subTab == 0,
                                onClick = { subTab = 0 },
                                text = { Text("Checklist (${tasks.size})") }
                            )
                            Tab(
                                selected = subTab == 1,
                                onClick = { subTab = 1 },
                                text = { Text("Study Notes (${notes.size})") }
                            )
                        }

                        if (subTab == 0) {
                            TaskListPanel(
                                tasks = tasks,
                                onAddTask = { title -> viewModel.addTask(title) },
                                onToggleTask = { taskId -> viewModel.toggleTask(taskId) },
                                onDeleteTask = { taskId -> viewModel.deleteTask(taskId) }
                            )
                        } else {
                            NotesPanel(
                                notes = notes,
                                onAddNote = { title, content -> viewModel.addNote(title, content) },
                                onDeleteNote = { noteId -> viewModel.deleteNote(noteId) }
                            )
                        }
                    }
                }

                RoutineTab.NOTIFICATIONS -> {
                    NotificationCenterScreen(
                        notifications = notifications,
                        onMarkRead = { viewModel.markNotificationRead(it) },
                        onMarkAllRead = { viewModel.markAllNotificationsRead() },
                        onClearAll = { viewModel.clearNotifications() },
                        onClose = { viewModel.activeTab.value = RoutineTab.HOME }
                    )
                }

                RoutineTab.SETTINGS -> {
                    SettingsScreen(
                        userPreferences = userPrefs,
                        onUpdatePrefs = { tech, sem, reg ->
                            viewModel.updatePersonalizedPreferences(tech, sem, reg)
                            Toast.makeText(context, "Preferences saved!", Toast.LENGTH_SHORT).show()
                        },
                        onExportClick = { viewModel.showExportModal.value = true },
                        onUploadClick = { showUploadModal = true },
                        onLoadSampleClick = {
                            viewModel.loadSampleData()
                            Toast.makeText(context, "Loaded BTEB Routine Sample!", Toast.LENGTH_SHORT).show()
                        },
                        onClearDataClick = {
                            viewModel.clearAllData()
                            Toast.makeText(context, "Cleared database!", Toast.LENGTH_SHORT).show()
                        },
                        uniqueTechnologies = uniqueTechnologies,
                        uniqueSemesters = uniqueSemesters
                    )
                }
            }
        }
    }

    // Modal Overlay: Dedicated Search Experience
    if (showSearchOverlay) {
        SearchOverlay(
            searchQuery = searchQuery,
            onQueryChange = { viewModel.searchQuery.value = it },
            searchResults = filteredEntries,
            uniqueTechnologies = uniqueTechnologies,
            uniqueSemesters = uniqueSemesters,
            onClose = { viewModel.showSearchOverlay.value = false },
            onClickEntry = { entry ->
                viewModel.selectedEntryForModal.value = entry
                viewModel.showSearchOverlay.value = false
            }
        )
    }

    // Modal 1: Exam Detail Modal
    selectedEntryForModal?.let { entry ->
        ExamDetailModal(
            entry = entry,
            tasks = tasks,
            notes = notes,
            onDismiss = { viewModel.selectedEntryForModal.value = null },
            onUpdateProgress = { progress -> viewModel.updateEntryProgress(entry.id, progress) },
            onUpdateEntry = { updated -> viewModel.updateEntry(updated) },
            onAddTask = { title, entryId -> viewModel.addTask(title, entryId) },
            onToggleTask = { taskId -> viewModel.toggleTask(taskId) },
            onDeleteTask = { taskId -> viewModel.deleteTask(taskId) },
            onAddNote = { title, content, entryId -> viewModel.addNote(title, content, entryId) },
            onDeleteNote = { noteId -> viewModel.deleteNote(noteId) }
        )
    }

    // Modal 2: Routine Manager Modal
    if (showRoutineManagerModal) {
        RoutineManagerModal(
            routines = allRoutines,
            activeRoutine = activeRoutine,
            onDismiss = { viewModel.showRoutineManagerModal.value = false },
            onSelectRoutine = { viewModel.selectRoutine(it) },
            onDeleteRoutine = { viewModel.deleteRoutine(it) },
            onDuplicateRoutine = { viewModel.duplicateRoutine(it) },
            onRenameRoutine = { id, name -> viewModel.renameRoutine(id, name) },
            onOpenExportModal = { viewModel.showExportModal.value = true },
            onOpenUpload = { showUploadModal = true }
        )
    }

    // Modal 3: Upload Dropzone Modal
    if (showUploadModal) {
        AlertDialog(
            onDismissRequest = { showUploadModal = false },
            title = null,
            text = {
                UploadDropzoneScreen(
                    parseProgress = parseProgress,
                    onUploadPdf = { uri, fileName ->
                        viewModel.uploadPdf(uri, fileName)
                    },
                    onLoadSampleData = {
                        viewModel.loadSampleData()
                        showUploadModal = false
                    },
                    onResetProgress = { viewModel.resetParseProgress() }
                )
            },
            confirmButton = {
                Button(onClick = {
                    showUploadModal = false
                    viewModel.resetParseProgress()
                }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal 4: Export Routine Modal
    if (showExportModal) {
        AlertDialog(
            onDismissRequest = { viewModel.showExportModal.value = false },
            title = { Text("Export Routine Data", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Export or backup current active routine in various formats:")
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val json = viewModel.getExportJson()
                                clipboardManager.setText(AnnotatedString(json))
                                Toast.makeText(context, "Routine JSON copied to clipboard!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy JSON Format")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val csv = viewModel.getExportCsv()
                                clipboardManager.setText(AnnotatedString(csv))
                                Toast.makeText(context, "Routine CSV copied to clipboard!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy CSV Table Format")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.showExportModal.value = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun SettingsScreen(
    userPreferences: com.example.data.model.UserPreferences,
    onUpdatePrefs: (tech: String?, sem: String?, reg: String?) -> Unit,
    onExportClick: () -> Unit,
    onUploadClick: () -> Unit,
    onLoadSampleClick: () -> Unit,
    onClearDataClick: () -> Unit,
    uniqueTechnologies: List<String>,
    uniqueSemesters: List<String>
) {
    var techInput by remember(userPreferences) { mutableStateOf(userPreferences.selectedTechnology ?: "") }
    var semInput by remember(userPreferences) { mutableStateOf(userPreferences.selectedSemester ?: "") }
    var regInput by remember(userPreferences) { mutableStateOf(userPreferences.selectedRegulation ?: "2022 Regulation") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Profile & Personalization",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Personalization Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Department & Semester Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Filtering will automatically highlight exams relevant to your technology and semester.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                androidx.compose.material3.OutlinedTextField(
                    value = techInput,
                    onValueChange = { techInput = it },
                    label = { Text("Technology e.g. Computer, Electrical, Civil") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.material3.OutlinedTextField(
                    value = semInput,
                    onValueChange = { semInput = it },
                    label = { Text("Semester e.g. 4th, 6th") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.material3.OutlinedTextField(
                    value = regInput,
                    onValueChange = { regInput = it },
                    label = { Text("Regulation e.g. 2022 Regulation") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onUpdatePrefs(
                            techInput.ifBlank { null },
                            semInput.ifBlank { null },
                            regInput.ifBlank { null }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Profile")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Routine Operations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onUploadClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload PDF Routine")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onExportClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export / Copy Routine Data")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onLoadSampleClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reload BTEB Sample Routine")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClearDataClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear All Local Data")
                }
            }
        }
    }
}
