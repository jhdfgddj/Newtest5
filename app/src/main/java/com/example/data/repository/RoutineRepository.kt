package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.NoteEntity
import com.example.data.local.RoutineDatabase
import com.example.data.local.RoutineEntity
import com.example.data.local.RoutineEntryEntity
import com.example.data.local.TaskEntity
import com.example.data.local.UserPreferenceEntity
import com.example.data.model.NoteItem
import com.example.data.model.Routine
import com.example.data.model.RoutineEntry
import com.example.data.model.TaskItem
import com.example.data.model.UserPreferences
import com.example.data.parser.BTEBSampleData
import com.example.data.parser.GeminiRoutineParser
import com.example.data.parser.PdfTextExtractor
import com.example.data.parser.RegexRoutineParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class ParseProgressState(
    val step: Int = 0, // 0: Idle, 1: Reading PDF, 2: Extracting Text, 3: Detecting Tables, 4: Normalizing, 5: Validating, 6: Completed, -1: Error
    val message: String = "",
    val error: String? = null
)

class RoutineRepository(private val context: Context) {

    private val db = RoutineDatabase.getDatabase(context)
    private val routineDao = db.routineDao()
    private val entryDao = db.routineEntryDao()
    private val taskDao = db.taskDao()
    private val noteDao = db.noteDao()
    private val prefDao = db.userPreferenceDao()

    private val pdfTextExtractor = PdfTextExtractor(context)
    private val regexParser = RegexRoutineParser()
    private val geminiParser = GeminiRoutineParser()

    val allRoutines: Flow<List<Routine>> = routineDao.getAllRoutines().map { list ->
        list.map { it.toModel() }
    }.flowOn(Dispatchers.IO)

    val userPreferences: Flow<UserPreferences> = prefDao.getUserPreferencesFlow().map {
        it?.toModel() ?: UserPreferences()
    }.flowOn(Dispatchers.IO)

    fun getEntriesForRoutine(routineId: String): Flow<List<RoutineEntry>> =
        entryDao.getEntriesForRoutine(routineId).map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)

    fun getTasksForRoutine(routineId: String): Flow<List<TaskItem>> =
        taskDao.getTasksForRoutine(routineId).map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)

    fun getNotesForRoutine(routineId: String): Flow<List<NoteItem>> =
        noteDao.getNotesForRoutine(routineId).map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)

    suspend fun loadSampleRoutine(): Routine = withContext(Dispatchers.IO) {
        val (routine, entries) = BTEBSampleData.createSampleRoutine()
        saveRoutineWithEntries(routine, entries)
        updateActiveRoutineId(routine.id)
        routine
    }

    suspend fun saveRoutineWithEntries(routine: Routine, entries: List<RoutineEntry>) = withContext(Dispatchers.IO) {
        routineDao.insertRoutine(RoutineEntity.fromModel(routine))
        entryDao.insertEntries(entries.map { RoutineEntryEntity.fromModel(it) })
    }

    suspend fun updateEntry(entry: RoutineEntry) = withContext(Dispatchers.IO) {
        entryDao.insertEntry(RoutineEntryEntity.fromModel(entry))
    }

    suspend fun updateEntryPrepProgress(entryId: String, progress: Int) = withContext(Dispatchers.IO) {
        val entity = entryDao.getEntryById(entryId) ?: return@withContext
        val updated = entity.copy(prepProgress = progress.coerceIn(0, 100))
        entryDao.insertEntry(updated)
    }

    suspend fun deleteRoutine(routineId: String) = withContext(Dispatchers.IO) {
        routineDao.deleteRoutineById(routineId)
        entryDao.deleteEntriesForRoutine(routineId)
        taskDao.deleteTasksForRoutine(routineId)
        noteDao.deleteNotesForRoutine(routineId)

        val prefs = getUserPreferencesSync()
        if (prefs.activeRoutineId == routineId) {
            val remaining = allRoutines.first().filter { it.id != routineId }
            val nextActiveId = remaining.firstOrNull()?.id
            updateActiveRoutineId(nextActiveId)
        }
    }

    suspend fun duplicateRoutine(routineId: String): Routine? = withContext(Dispatchers.IO) {
        val currentEntity = routineDao.getRoutineById(routineId) ?: return@withContext null
        val newId = "routine_" + System.currentTimeMillis()
        val newRoutine = currentEntity.toModel().copy(
            id = newId,
            title = "${currentEntity.title} (Copy)",
            createdAt = System.currentTimeMillis()
        )

        val currentEntries = entryDao.getEntriesForRoutineSync(routineId)
        val newEntries = currentEntries.map {
            it.toModel().copy(
                id = java.util.UUID.randomUUID().toString(),
                routineId = newId
            )
        }

        saveRoutineWithEntries(newRoutine, newEntries)
        newRoutine
    }

    suspend fun renameRoutine(routineId: String, newTitle: String) = withContext(Dispatchers.IO) {
        val existing = routineDao.getRoutineById(routineId) ?: return@withContext
        routineDao.insertRoutine(existing.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
    }

    suspend fun addTask(task: TaskItem) = withContext(Dispatchers.IO) {
        taskDao.insertTask(TaskEntity.fromModel(task))
    }

    suspend fun toggleTaskCompleted(taskId: String, routineId: String) = withContext(Dispatchers.IO) {
        val tasks = taskDao.getTasksForRoutine(routineId).first()
        val target = tasks.find { it.id == taskId } ?: return@withContext
        val updated = target.copy(isCompleted = !target.isCompleted)
        taskDao.insertTask(updated)
    }

    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun addNote(note: NoteItem) = withContext(Dispatchers.IO) {
        noteDao.insertNote(NoteEntity.fromModel(note))
    }

    suspend fun deleteNote(noteId: String) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(noteId)
    }

    suspend fun updatePreferences(preferences: UserPreferences) = withContext(Dispatchers.IO) {
        prefDao.insertUserPreferences(UserPreferenceEntity.fromModel(preferences))
    }

    suspend fun updateActiveRoutineId(routineId: String?) = withContext(Dispatchers.IO) {
        val current = getUserPreferencesSync()
        prefDao.insertUserPreferences(UserPreferenceEntity.fromModel(current.copy(activeRoutineId = routineId)))
    }

    suspend fun getUserPreferencesSync(): UserPreferences = withContext(Dispatchers.IO) {
        prefDao.getUserPreferences()?.toModel() ?: UserPreferences()
    }

    suspend fun processPdfUpload(uri: Uri, fileName: String, onProgress: (ParseProgressState) -> Unit): Routine? = withContext(Dispatchers.IO) {
        try {
            onProgress(ParseProgressState(step = 1, message = "Reading PDF document..."))
            val extraction = pdfTextExtractor.extractFromUri(uri)

            if (extraction.errorMessage != null) {
                onProgress(ParseProgressState(step = -1, error = extraction.errorMessage))
                return@withContext null
            }

            if (extraction.isScanned) {
                onProgress(ParseProgressState(step = -1, error = "This PDF appears to be image-based/scanned without selectable text. You can load sample data or edit manually."))
                return@withContext null
            }

            onProgress(ParseProgressState(step = 2, message = "Extracting text and identifying structure..."))

            val cleanTitle = fileName.removeSuffix(".pdf").removeSuffix(".PDF").replace("_", " ")

            var parsed: Pair<Routine, List<RoutineEntry>>? = null

            if (geminiParser.isApiKeyAvailable()) {
                onProgress(ParseProgressState(step = 3, message = "AI-Assisted understanding routine tables..."))
                parsed = geminiParser.parseWithGemini(extraction.extractedText, cleanTitle)
            }

            if (parsed == null) {
                onProgress(ParseProgressState(step = 4, message = "Normalizing and parsing routine records..."))
                parsed = regexParser.parseRoutine(extraction.extractedText, cleanTitle)
            }

            onProgress(ParseProgressState(step = 5, message = "Validating data and generating application..."))

            val (routine, entries) = parsed
            saveRoutineWithEntries(routine, entries)
            updateActiveRoutineId(routine.id)

            onProgress(ParseProgressState(step = 6, message = "Completed!"))
            routine
        } catch (e: Exception) {
            onProgress(ParseProgressState(step = -1, error = "Error processing routine: ${e.localizedMessage}"))
            null
        }
    }

    suspend fun exportRoutineToJson(routineId: String): String = withContext(Dispatchers.IO) {
        val routine = routineDao.getRoutineById(routineId)?.toModel() ?: return@withContext "{}"
        val entries = entryDao.getEntriesForRoutineSync(routineId).map { it.toModel() }

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"organization\": \"${routine.organization}\",\n")
        sb.append("  \"title\": \"${routine.title}\",\n")
        sb.append("  \"noticeNumber\": \"${routine.noticeNumber}\",\n")
        sb.append("  \"publicationDate\": \"${routine.publicationDate}\",\n")
        sb.append("  \"examSession\": \"${routine.examSession}\",\n")
        sb.append("  \"regulation\": \"${routine.regulation}\",\n")
        sb.append("  \"entries\": [\n")
        entries.forEachIndexed { idx, e ->
            sb.append("    {\n")
            sb.append("      \"date\": \"${e.date}\",\n")
            sb.append("      \"day\": \"${e.day}\",\n")
            sb.append("      \"time\": \"${e.time}\",\n")
            sb.append("      \"session\": \"${e.session}\",\n")
            sb.append("      \"subjectCode\": \"${e.subjectCode}\",\n")
            sb.append("      \"subjectName\": \"${e.subjectName}\",\n")
            sb.append("      \"technology\": [${e.technology.joinToString { "\"$it\"" }}],\n")
            sb.append("      \"semester\": [${e.semester.joinToString { "\"$it\"" }}]\n")
            sb.append("    }${if (idx < entries.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        sb.toString()
    }

    suspend fun exportRoutineToCsv(routineId: String): String = withContext(Dispatchers.IO) {
        val entries = entryDao.getEntriesForRoutineSync(routineId).map { it.toModel() }
        val sb = StringBuilder()
        sb.append("Date,Day,Time,Session,Subject Code,Subject Name,Technologies,Semesters,Regulation\n")
        entries.forEach { e ->
            val techStr = e.technology.joinToString(";")
            val semStr = e.semester.joinToString(";")
            sb.append("\"${e.date}\",\"${e.day}\",\"${e.time}\",\"${e.session}\",\"${e.subjectCode}\",\"${e.subjectName}\",\"$techStr\",\"$semStr\",\"${e.regulation}\"\n")
        }
        sb.toString()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        routineDao.deleteAllRoutines()
        entryDao.deleteAllEntries()
        taskDao.deleteAllTasks()
        noteDao.deleteAllNotes()
        prefDao.deleteAllPreferences()
    }
}
