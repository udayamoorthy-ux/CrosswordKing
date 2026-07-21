package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val word: String, // Uppercase word
    val meaning: String,
    val partOfSpeech: String,
    val example: String,
    val isDifficult: Boolean = false,
    val isLearned: Boolean = false,
    val levelId: Int
)

@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelId: Int,
    val isCrosswordCompleted: Boolean = false,
    val isVocabCompleted: Boolean = false,
    val unlocked: Boolean = false,
    val score: Int = 0
)

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY word ASC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE levelId = :levelId")
    fun getWordsByLevel(levelId: Int): Flow<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): WordEntity?

    @Query("DELETE FROM words")
    suspend fun clearAll()
}

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllProgress(): Flow<List<LevelProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LevelProgress)

    @Update
    suspend fun updateProgress(progress: LevelProgress)

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    suspend fun getProgressById(levelId: Int): LevelProgress?

    @Query("DELETE FROM level_progress")
    suspend fun clearAll()
}

@Database(entities = [WordEntity::class, LevelProgress::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun levelProgressDao(): LevelProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crossword_vocab_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class GameRepository(private val db: AppDatabase) {
    val allWords: Flow<List<WordEntity>> = db.wordDao().getAllWords()
    val allProgress: Flow<List<LevelProgress>> = db.levelProgressDao().getAllProgress()

    fun getWordsByLevel(levelId: Int): Flow<List<WordEntity>> {
        return db.wordDao().getWordsByLevel(levelId)
    }

    suspend fun getWord(word: String): WordEntity? {
        return db.wordDao().getWord(word.uppercase())
    }

    suspend fun insertWord(word: WordEntity) {
        db.wordDao().insertWord(word)
    }

    suspend fun updateWord(word: WordEntity) {
        db.wordDao().updateWord(word)
    }

    suspend fun insertProgress(progress: LevelProgress) {
        db.levelProgressDao().insertProgress(progress)
    }

    suspend fun updateProgress(progress: LevelProgress) {
        db.levelProgressDao().updateProgress(progress)
    }

    suspend fun getProgressById(levelId: Int): LevelProgress? {
        return db.levelProgressDao().getProgressById(levelId)
    }
}
