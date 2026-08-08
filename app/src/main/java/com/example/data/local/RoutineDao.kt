package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY updatedAt DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    fun getRoutineByIdFlow(id: String): Flow<RoutineEntity?>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: String): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: String)

    @Query("DELETE FROM routines")
    suspend fun deleteAllRoutines()
}

@Dao
interface RoutineEntryDao {
    @Query("SELECT * FROM routine_entries WHERE routineId = :routineId ORDER BY date ASC, time ASC")
    fun getEntriesForRoutine(routineId: String): Flow<List<RoutineEntryEntity>>

    @Query("SELECT * FROM routine_entries WHERE routineId = :routineId ORDER BY date ASC, time ASC")
    suspend fun getEntriesForRoutineSync(routineId: String): List<RoutineEntryEntity>

    @Query("SELECT * FROM routine_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: String): RoutineEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<RoutineEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: RoutineEntryEntity)

    @Query("DELETE FROM routine_entries WHERE routineId = :routineId")
    suspend fun deleteEntriesForRoutine(routineId: String)

    @Query("DELETE FROM routine_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("DELETE FROM routine_entries")
    suspend fun deleteAllEntries()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE routineId = :routineId ORDER BY createdAt DESC")
    fun getTasksForRoutine(routineId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks WHERE routineId = :routineId")
    suspend fun deleteTasksForRoutine(routineId: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE routineId = :routineId ORDER BY createdAt DESC")
    fun getNotesForRoutine(routineId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("DELETE FROM notes WHERE routineId = :routineId")
    suspend fun deleteNotesForRoutine(routineId: String)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferencesFlow(): Flow<UserPreferenceEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreferences(): UserPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferenceEntity)

    @Query("DELETE FROM user_preferences")
    suspend fun deleteAllPreferences()
}
