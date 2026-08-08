package com.example.data.model

enum class DataQualityStatus {
    VERIFIED,
    NEEDS_REVIEW
}

data class Routine(
    val id: String,
    val title: String,
    val organization: String = "Bangladesh Technical Education Board",
    val noticeNumber: String = "",
    val publicationDate: String = "",
    val examSession: String = "",
    val regulation: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class RoutineEntry(
    val id: String,
    val routineId: String,
    val date: String, // ISO YYYY-MM-DD
    val day: String, // e.g. Thursday / বৃহস্পতিবার
    val time: String, // e.g. 10:00 AM, 2:00 PM
    val session: String = "", // Morning / Afternoon
    val semester: List<String> = emptyList(),
    val subjectCode: String,
    val subjectName: String,
    val technology: List<String> = emptyList(),
    val regulation: String = "",
    val examSession: String = "",
    val rawText: String = "",
    val status: DataQualityStatus = DataQualityStatus.VERIFIED,
    val prepProgress: Int = 0 // 0 to 100%
)

data class TaskItem(
    val id: String,
    val routineId: String,
    val entryId: String? = null,
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class NoteItem(
    val id: String,
    val routineId: String,
    val entryId: String? = null,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class UserPreferences(
    val selectedTechnology: String? = null,
    val selectedSemester: String? = null,
    val selectedRegulation: String? = null,
    val activeRoutineId: String? = null,
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val language: String = "EN", // EN, BN
    val timeFormat: String = "12H", // 12H, 24H
    val timeZone: String = "Asia/Dhaka",
    val remindersEnabled: Boolean = true,
    val reminderLeadHours: List<Int> = listOf(24, 12, 3, 1)
)
