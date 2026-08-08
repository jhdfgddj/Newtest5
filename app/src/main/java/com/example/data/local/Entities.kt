package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DataQualityStatus
import com.example.data.model.NoteItem
import com.example.data.model.Routine
import com.example.data.model.RoutineEntry
import com.example.data.model.TaskItem
import com.example.data.model.UserPreferences

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val title: String,
    val organization: String,
    val noticeNumber: String,
    val publicationDate: String,
    val examSession: String,
    val regulation: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toModel(): Routine = Routine(
        id = id,
        title = title,
        organization = organization,
        noticeNumber = noticeNumber,
        publicationDate = publicationDate,
        examSession = examSession,
        regulation = regulation,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: Routine): RoutineEntity = RoutineEntity(
            id = model.id,
            title = model.title,
            organization = model.organization,
            noticeNumber = model.noticeNumber,
            publicationDate = model.publicationDate,
            examSession = model.examSession,
            regulation = model.regulation,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}

@Entity(tableName = "routine_entries")
data class RoutineEntryEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val date: String,
    val day: String,
    val time: String,
    val session: String,
    val semester: List<String>,
    val subjectCode: String,
    val subjectName: String,
    val technology: List<String>,
    val regulation: String,
    val examSession: String,
    val rawText: String,
    val status: DataQualityStatus,
    val prepProgress: Int
) {
    fun toModel(): RoutineEntry = RoutineEntry(
        id = id,
        routineId = routineId,
        date = date,
        day = day,
        time = time,
        session = session,
        semester = semester,
        subjectCode = subjectCode,
        subjectName = subjectName,
        technology = technology,
        regulation = regulation,
        examSession = examSession,
        rawText = rawText,
        status = status,
        prepProgress = prepProgress
    )

    companion object {
        fun fromModel(model: RoutineEntry): RoutineEntryEntity = RoutineEntryEntity(
            id = model.id,
            routineId = model.routineId,
            date = model.date,
            day = model.day,
            time = model.time,
            session = model.session,
            semester = model.semester,
            subjectCode = model.subjectCode,
            subjectName = model.subjectName,
            technology = model.technology,
            regulation = model.regulation,
            examSession = model.examSession,
            rawText = model.rawText,
            status = model.status,
            prepProgress = model.prepProgress
        )
    }
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val entryId: String?,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: Long
) {
    fun toModel(): TaskItem = TaskItem(
        id = id,
        routineId = routineId,
        entryId = entryId,
        title = title,
        isCompleted = isCompleted,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(model: TaskItem): TaskEntity = TaskEntity(
            id = model.id,
            routineId = model.routineId,
            entryId = model.entryId,
            title = model.title,
            isCompleted = model.isCompleted,
            createdAt = model.createdAt
        )
    }
}

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val entryId: String?,
    val title: String,
    val content: String,
    val createdAt: Long
) {
    fun toModel(): NoteItem = NoteItem(
        id = id,
        routineId = routineId,
        entryId = entryId,
        title = title,
        content = content,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(model: NoteItem): NoteEntity = NoteEntity(
            id = model.id,
            routineId = model.routineId,
            entryId = model.entryId,
            title = model.title,
            content = model.content,
            createdAt = model.createdAt
        )
    }
}

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val id: Int = 1,
    val selectedTechnology: String?,
    val selectedSemester: String?,
    val selectedRegulation: String?,
    val activeRoutineId: String?,
    val themeMode: String,
    val language: String,
    val timeFormat: String,
    val timeZone: String,
    val remindersEnabled: Boolean,
    val reminderLeadHours: List<Int>
) {
    fun toModel(): UserPreferences = UserPreferences(
        selectedTechnology = selectedTechnology,
        selectedSemester = selectedSemester,
        selectedRegulation = selectedRegulation,
        activeRoutineId = activeRoutineId,
        themeMode = themeMode,
        language = language,
        timeFormat = timeFormat,
        timeZone = timeZone,
        remindersEnabled = remindersEnabled,
        reminderLeadHours = reminderLeadHours
    )

    companion object {
        fun fromModel(model: UserPreferences): UserPreferenceEntity = UserPreferenceEntity(
            id = 1,
            selectedTechnology = model.selectedTechnology,
            selectedSemester = model.selectedSemester,
            selectedRegulation = model.selectedRegulation,
            activeRoutineId = model.activeRoutineId,
            themeMode = model.themeMode,
            language = model.language,
            timeFormat = model.timeFormat,
            timeZone = model.timeZone,
            remindersEnabled = model.remindersEnabled,
            reminderLeadHours = model.reminderLeadHours
        )
    }
}
