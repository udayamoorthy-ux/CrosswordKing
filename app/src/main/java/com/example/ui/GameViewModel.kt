package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AiMeaningUiState {
    object Idle : AiMeaningUiState
    object Loading : AiMeaningUiState
    data class Success(val definition: WordDefinition) : AiMeaningUiState
    data class Error(val message: String) : AiMeaningUiState
}

data class VocabQuizQuestion(
    val originalWord: String,
    val scrambled: String,
    val clue: String
)

data class VocabQuizState(
    val questions: List<VocabQuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val userInput: String = "",
    val score: Int = 0,
    val isFinished: Boolean = false,
    val feedbackMessage: String = "",
    val isCorrect: Boolean? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database)
        initializeDatabase()
    }

    // --- State Observables ---
    val allProgress: StateFlow<List<LevelProgress>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedWords: StateFlow<List<WordEntity>> = repository.allWords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentLevelId = MutableStateFlow(1)
    val currentLevelId: StateFlow<Int> = _currentLevelId.asStateFlow()

    // Map of (row, col) to Char entered by the user
    private val _crosswordGrid = MutableStateFlow<Map<Pair<Int, Int>, Char>>(emptyMap())
    val crosswordGrid: StateFlow<Map<Pair<Int, Int>, Char>> = _crosswordGrid.asStateFlow()

    private val _selectedCell = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedCell: StateFlow<Pair<Int, Int>?> = _selectedCell.asStateFlow()

    private val _activeWord = MutableStateFlow<CrosswordWord?>(null)
    val activeWord: StateFlow<CrosswordWord?> = _activeWord.asStateFlow()

    private val _isCrosswordCorrect = MutableStateFlow<Boolean?>(null)
    val isCrosswordCorrect: StateFlow<Boolean?> = _isCrosswordCorrect.asStateFlow()

    // Vocabulary Quiz State
    private val _vocabQuizState = MutableStateFlow(VocabQuizState())
    val vocabQuizState: StateFlow<VocabQuizState> = _vocabQuizState.asStateFlow()

    // Gemini API State
    private val _aiMeaningState = MutableStateFlow<AiMeaningUiState>(AiMeaningUiState.Idle)
    val aiMeaningState: StateFlow<AiMeaningUiState> = _aiMeaningState.asStateFlow()

    // --- Initialization ---
    private fun initializeDatabase() {
        viewModelScope.launch {
            // Check if level progress exists; if not, create them
            repository.allProgress.first().let { progressList ->
                if (progressList.isEmpty()) {
                    for (i in 1..5) {
                        repository.insertProgress(
                            LevelProgress(
                                levelId = i,
                                isCrosswordCompleted = false,
                                isVocabCompleted = false,
                                unlocked = i == 1, // Level 1 is unlocked initially
                                score = 0
                            )
                        )
                    }
                }
            }

            // Check if words exist; if not, populate from game vocabulary
            repository.allWords.first().let { wordList ->
                if (wordList.isEmpty()) {
                    LevelsData.levels.forEach { level ->
                        level.words.forEach { cWord ->
                            val offlineInfo = LevelsData.offlineMeanings[cWord.word]
                            repository.insertWord(
                                WordEntity(
                                    word = cWord.word,
                                    meaning = offlineInfo?.first ?: cWord.clue,
                                    partOfSpeech = offlineInfo?.second ?: "Noun",
                                    example = offlineInfo?.third ?: "",
                                    isDifficult = false,
                                    isLearned = false,
                                    levelId = level.id
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Navigation / Level Control ---
    fun selectLevel(levelId: Int) {
        _currentLevelId.value = levelId
        resetLevelState(levelId)
    }

    private fun resetLevelState(levelId: Int) {
        _crosswordGrid.value = emptyMap()
        _selectedCell.value = null
        _activeWord.value = null
        _isCrosswordCorrect.value = null
        _aiMeaningState.value = AiMeaningUiState.Idle
        initVocabQuiz(levelId)
    }

    // --- Crossword Mechanics ---
    fun selectCell(row: Int, col: Int) {
        _selectedCell.value = Pair(row, col)
        
        // Find if this cell belongs to any crossword word in the current level
        val currentLevel = LevelsData.levels.find { it.id == _currentLevelId.value } ?: return
        val matchingWord = currentLevel.words.find { cWord ->
            if (cWord.isAcross) {
                row == cWord.row && col >= cWord.col && col < cWord.col + cWord.length
            } else {
                col == cWord.col && row >= cWord.row && row < cWord.row + cWord.length
            }
        }
        _activeWord.value = matchingWord
    }

    fun selectClue(word: CrosswordWord) {
        _activeWord.value = word
        _selectedCell.value = Pair(word.row, word.col)
    }

    fun enterLetter(char: Char) {
        val cell = _selectedCell.value ?: return
        val upperChar = char.uppercaseChar()

        // Verify cell is valid for the current level
        val currentLevel = LevelsData.levels.find { it.id == _currentLevelId.value } ?: return
        val isCellValid = currentLevel.words.any { cWord ->
            if (cWord.isAcross) {
                cell.first == cWord.row && cell.second >= cWord.col && cell.second < cWord.col + cWord.length
            } else {
                cell.second == cWord.col && cell.first >= cWord.row && cell.first < cWord.row + cWord.length
            }
        }

        if (!isCellValid) return

        // Update grid
        val updatedGrid = _crosswordGrid.value.toMutableMap()
        updatedGrid[cell] = upperChar
        _crosswordGrid.value = updatedGrid

        // Move to the next cell automatically if typing in a word
        val activeW = _activeWord.value
        if (activeW != null) {
            val nextCell = if (activeW.isAcross) {
                Pair(cell.first, cell.second + 1)
            } else {
                Pair(cell.first + 1, cell.second)
            }
            // Check if next cell is still in the active word bounds
            val inBounds = if (activeW.isAcross) {
                nextCell.second < activeW.col + activeW.length
            } else {
                nextCell.first < activeW.row + activeW.length
            }
            if (inBounds) {
                _selectedCell.value = nextCell
            }
        }
    }

    fun deleteLetter() {
        val cell = _selectedCell.value ?: return
        val updatedGrid = _crosswordGrid.value.toMutableMap()
        if (updatedGrid.containsKey(cell)) {
            updatedGrid.remove(cell)
            _crosswordGrid.value = updatedGrid
        } else {
            // Move back one cell if current is empty
            val activeW = _activeWord.value
            if (activeW != null) {
                val prevCell = if (activeW.isAcross) {
                    Pair(cell.first, cell.second - 1)
                } else {
                    Pair(cell.first - 1, cell.second)
                }
                val inBounds = if (activeW.isAcross) {
                    prevCell.second >= activeW.col
                } else {
                    prevCell.first >= activeW.row
                }
                if (inBounds) {
                    _selectedCell.value = prevCell
                    val gridWithPrevRemoved = _crosswordGrid.value.toMutableMap()
                    gridWithPrevRemoved.remove(prevCell)
                    _crosswordGrid.value = gridWithPrevRemoved
                }
            }
        }
    }

    fun validateCrossword() {
        val levelId = _currentLevelId.value
        val currentLevel = LevelsData.levels.find { it.id == levelId } ?: return
        val grid = _crosswordGrid.value

        var allCorrect = true
        for (cWord in currentLevel.words) {
            for (i in 0 until cWord.length) {
                val row = if (cWord.isAcross) cWord.row else cWord.row + i
                val col = if (cWord.isAcross) cWord.col + i else cWord.col
                val expectedChar = cWord.word[i]
                val enteredChar = grid[Pair(row, col)]

                if (enteredChar != expectedChar) {
                    allCorrect = false
                    break
                }
            }
            if (!allCorrect) break
        }

        _isCrosswordCorrect.value = allCorrect

        if (allCorrect) {
            viewModelScope.launch {
                val progress = repository.getProgressById(levelId)
                if (progress != null) {
                    val updatedProgress = progress.copy(isCrosswordCompleted = true)
                    repository.updateProgress(updatedProgress)
                    
                    // If both crossword and vocabulary are completed, unlock the next level
                    checkAndUnlockNextLevel(levelId, updatedProgress.isVocabCompleted)
                }
            }
        }
    }

    // --- Vocabulary Game Mechanics ---
    private fun initVocabQuiz(levelId: Int) {
        val currentLevel = LevelsData.levels.find { it.id == levelId } ?: return
        val questions = currentLevel.words.map { cWord ->
            // Scramble word letters
            var scrambled = cWord.word.toCharArray().toList().shuffled().joinToString("")
            while (scrambled == cWord.word && cWord.word.length > 1) {
                scrambled = cWord.word.toCharArray().toList().shuffled().joinToString("")
            }
            val offlineInfo = LevelsData.offlineMeanings[cWord.word]
            VocabQuizQuestion(
                originalWord = cWord.word,
                scrambled = scrambled,
                clue = offlineInfo?.first ?: cWord.clue
            )
        }

        _vocabQuizState.value = VocabQuizState(
            questions = questions,
            currentIndex = 0,
            userInput = "",
            score = 0,
            isFinished = false,
            feedbackMessage = "",
            isCorrect = null
        )
    }

    fun enterVocabLetter(letter: String) {
        val current = _vocabQuizState.value
        if (current.isFinished) return
        _vocabQuizState.value = current.copy(
            userInput = current.userInput + letter.uppercase(),
            isCorrect = null,
            feedbackMessage = ""
        )
    }

    fun deleteVocabLetter() {
        val current = _vocabQuizState.value
        if (current.isFinished || current.userInput.isEmpty()) return
        _vocabQuizState.value = current.copy(
            userInput = current.userInput.dropLast(1),
            isCorrect = null,
            feedbackMessage = ""
        )
    }

    fun submitVocabAnswer() {
        val current = _vocabQuizState.value
        if (current.isFinished || current.userInput.isEmpty()) return

        val currentQuestion = current.questions[current.currentIndex]
        val isCorrect = current.userInput.trim().uppercase() == currentQuestion.originalWord

        val newScore = if (isCorrect) current.score + 20 else current.score
        val feedback = if (isCorrect) "Correct! Excellent job!" else "Oops! The correct spelling is '${currentQuestion.originalWord}'"

        _vocabQuizState.value = current.copy(
            isCorrect = isCorrect,
            feedbackMessage = feedback,
            score = newScore
        )
    }

    fun nextVocabQuestion() {
        val current = _vocabQuizState.value
        val nextIndex = current.currentIndex + 1
        
        if (nextIndex >= current.questions.size) {
            // Finished! Update DB
            val levelId = _currentLevelId.value
            _vocabQuizState.value = current.copy(
                isFinished = true,
                feedbackMessage = "Quiz Complete! You scored ${current.score}/${current.questions.size * 20} points!"
            )
            viewModelScope.launch {
                val progress = repository.getProgressById(levelId)
                if (progress != null) {
                    val updatedProgress = progress.copy(
                        isVocabCompleted = true,
                        score = current.score
                    )
                    repository.updateProgress(updatedProgress)
                    checkAndUnlockNextLevel(levelId, crosswordCompleted = updatedProgress.isCrosswordCompleted)
                }
            }
        } else {
            _vocabQuizState.value = current.copy(
                currentIndex = nextIndex,
                userInput = "",
                isCorrect = null,
                feedbackMessage = ""
            )
        }
    }

    private suspend fun checkAndUnlockNextLevel(completedLevelId: Int, crosswordCompleted: Boolean) {
        val progress = repository.getProgressById(completedLevelId) ?: return
        val vocabularyCompleted = progress.isVocabCompleted
        
        // If both crossword and vocabulary are completed for this level, unlock next level
        if (progress.isCrosswordCompleted && vocabularyCompleted) {
            val nextLevelId = completedLevelId + 1
            if (nextLevelId <= 5) {
                val nextProgress = repository.getProgressById(nextLevelId)
                if (nextProgress != null && !nextProgress.unlocked) {
                    repository.updateProgress(nextProgress.copy(unlocked = true))
                }
            }
        }
    }

    // --- Word Bank / Saved Dictionary Actions ---
    fun toggleWordDifficulty(word: WordEntity) {
        viewModelScope.launch {
            repository.updateWord(word.copy(isDifficult = !word.isDifficult))
        }
    }

    fun toggleWordLearned(word: WordEntity) {
        viewModelScope.launch {
            repository.updateWord(word.copy(isLearned = !word.isLearned))
        }
    }

    fun lookupCustomWord(word: String) {
        val cleanWord = word.trim().uppercase()
        if (cleanWord.isEmpty()) return

        _aiMeaningState.value = AiMeaningUiState.Loading

        viewModelScope.launch {
            // First check if already in DB
            val existing = repository.getWord(cleanWord)
            if (existing != null && existing.meaning.isNotEmpty() && existing.partOfSpeech != "Configure API Key") {
                _aiMeaningState.value = AiMeaningUiState.Success(
                    WordDefinition(
                        word = existing.word,
                        meaning = existing.meaning,
                        partOfSpeech = existing.partOfSpeech,
                        example = existing.example,
                        source = "Saved Word Bank"
                    )
                )
                return@launch
            }

            // Otherwise call Gemini API
            val definition = GeminiClient.fetchDefinition(cleanWord)
            if (definition.partOfSpeech == "Error") {
                _aiMeaningState.value = AiMeaningUiState.Error(definition.meaning)
            } else {
                _aiMeaningState.value = AiMeaningUiState.Success(definition)
                
                // If it is a valid definition (not a guides/error), save to local database
                if (definition.partOfSpeech != "Configure API Key" && definition.partOfSpeech != "Setup Guide") {
                    repository.insertWord(
                        WordEntity(
                            word = definition.word,
                            meaning = definition.meaning,
                            partOfSpeech = definition.partOfSpeech,
                            example = definition.example,
                            isDifficult = true, // Auto-mark custom lookups as difficult/study words
                            isLearned = false,
                            levelId = 0 // Level 0 represents custom searched dictionary words
                        )
                    )
                }
            }
        }
    }

    fun clearAiMeaningState() {
        _aiMeaningState.value = AiMeaningUiState.Idle
    }
}
