package com.example.vitalityapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pushup_sessions")
data class PushUpSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reps: Int,
    val durationSeconds: Int,
    val dateInMillis: Long = System.currentTimeMillis()
)

@Dao

interface PushUpDao {
    @Insert
    suspend fun insertSession(session: PushUpSession): Long

    @Query("SELECT * FROM pushup_sessions ORDER BY dateInMillis DESC")
    fun getAllSessions(): Flow<List<PushUpSession>>
}

@Database(entities = [PushUpSession::class], version = 1, exportSchema = false)
abstract class VitalityDatabase : RoomDatabase() {
    abstract fun pushUpDao(): PushUpDao
}
