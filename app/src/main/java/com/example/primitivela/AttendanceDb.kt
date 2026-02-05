package com.example.primitivela // 1. CRITICAL: Ensure this matches your package name

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. The "Note" or Event table
@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val hasCsvData: Boolean = false
)

// 2. New entity for CSV student data
@Entity(tableName = "students")
data class Student(
    @PrimaryKey val roll: String,
    val eventId: Int,
    val name: String,
    val morningPresent: Boolean = false,
    val afternoonPresent: Boolean = false,
    val eveningPresent: Boolean = false,
    val morningTime: Long? = null,
    val afternoonTime: Long? = null,
    val eveningTime: Long? = null
)

// 3. The Scanned Barcodes table
@Entity(tableName = "attendance")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val barcodeValue: String,
    val studentName: String? = null,
    val session: String = "morning", // morning, afternoon, evening
    val timestamp: Long = System.currentTimeMillis()
)

// 4. The Data Access Object (Commands)
@Dao
interface AttendanceDao {
    @Insert
    suspend fun insertEvent(event: Event): Long

    @Insert
    suspend fun insertRecord(record: AttendanceRecord)

    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM attendance WHERE eventId = :eventId")
    suspend fun getRecordsForEvent(eventId: Int): List<AttendanceRecord>

    @Delete
    suspend fun deleteEvent(event: Event)

    // New methods for CSV functionality
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Query("SELECT * FROM students WHERE eventId = :eventId AND roll = UPPER(TRIM(:roll))")
    suspend fun getStudent(eventId: Int, roll: String): Student?

    @Update
    suspend fun updateStudent(student: Student)

    @Query("SELECT * FROM students WHERE eventId = :eventId ORDER BY name")
    suspend fun getStudentsForEvent(eventId: Int): List<Student>

    @Query("UPDATE events SET hasCsvData = :hasCsvData WHERE id = :eventId")
    suspend fun updateEventCsvStatus(eventId: Int, hasCsvData: Boolean)

    @Query("SELECT * FROM attendance WHERE eventId = :eventId AND session = :session")
    suspend fun getRecordsForSession(eventId: Int, session: String): List<AttendanceRecord>
}

// 5. The Database Holder
@Database(entities = [Event::class, AttendanceRecord::class, Student::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao
}