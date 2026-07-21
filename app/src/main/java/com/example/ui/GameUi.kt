package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameScreen(viewModel: GameViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentLevelId by viewModel.currentLevelId.collectAsState()
    val allProgress by viewModel.allProgress.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }
    
    val currentLevelProgress = allProgress.find { it.levelId == currentLevelId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CrossKing",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.testTag("help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "How to Play Guide",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(
                        onClick = { viewModel.selectLevel(currentLevelId) },
                        modifier = Modifier.testTag("reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Current Level"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Extension, contentDescription = null) },
                    label = { Text("Crossword") },
                    modifier = Modifier.testTag("nav_crossword")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    label = { Text("Vocab Game") },
                    modifier = Modifier.testTag("nav_vocab")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Book, contentDescription = null) },
                    label = { Text("Word Bank") },
                    modifier = Modifier.testTag("nav_word_bank")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Level Selector Bar (shared at the top of game tabs)
            if (selectedTab != 2) {
                LevelSelector(
                    allProgress = allProgress,
                    currentLevelId = currentLevelId,
                    onLevelSelected = { viewModel.selectLevel(it) }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> CrosswordTab(viewModel = viewModel, onOpenHelp = { showHelpDialog = true })
                    1 -> VocabTab(viewModel = viewModel, onOpenHelp = { showHelpDialog = true })
                    2 -> WordBankTab(viewModel = viewModel)
                }
            }
        }
    }

    // Beautiful step-by-step onboarding and help dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "How to Play CrossKing",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            text = {
                var helpTab by remember { mutableIntStateOf(selectedTab.coerceIn(0, 1)) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Help Sub-tabs for Crossword vs Vocab Game
                    TabRow(
                        selectedTabIndex = helpTab,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = helpTab == 0,
                            onClick = { helpTab = 0 },
                            text = { Text("Crossword", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = helpTab == 1,
                            onClick = { helpTab = 1 },
                            text = { Text("Vocabulary", fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (helpTab == 0) {
                        // Crossword guide
                        Text(
                            text = "Solve the crossword grid using the clues provided.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        HelpStepItem(
                            number = "1",
                            icon = Icons.Default.TouchApp,
                            title = "Select a Cell",
                            description = "Tap any playable white box in the crossword grid. This highlights the word (Across or Down) and displays its clue."
                        )

                        HelpStepItem(
                            number = "2",
                            icon = Icons.Default.Keyboard,
                            title = "Type Your Answer",
                            description = "Use our custom keyboard at the bottom of the screen to enter letters. The highlighted active cell moves forward automatically."
                        )

                        HelpStepItem(
                            number = "3",
                            icon = Icons.Default.Lightbulb,
                            title = "Need Help? Use AI",
                            description = "Stuck? Tap the 'Gemini AI Definition' button on the clue box to get an instant description from Gemini AI."
                        )

                        HelpStepItem(
                            number = "4",
                            icon = Icons.Default.Check,
                            title = "Verify & Progress",
                            description = "Once filled, tap 'Verify Solution'. Correct solutions unlock level progression when both crossword and quiz tabs are complete!"
                        )
                    } else {
                        // Vocab game guide
                        Text(
                            text = "Spell the secret word that matches the definition using scrambled letters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HelpStepItem(
                            number = "1",
                            icon = Icons.Default.MenuBook,
                            title = "Read Definition",
                            description = "Look at the definition displayed inside the top card to understand what word you are spelling."
                        )

                        HelpStepItem(
                            number = "2",
                            icon = Icons.Default.Shuffle,
                            title = "Spell with Buttons",
                            description = "Tap on the scrambled letter buttons to enter them. Tap 'Delete Last' if you make a spelling mistake."
                        )

                        HelpStepItem(
                            number = "3",
                            icon = Icons.Default.Verified,
                            title = "Submit Your Answer",
                            description = "Click 'Submit Spelling' to check if your spelling is correct. If it is, advance to the next word to boost your score!"
                        )

                        HelpStepItem(
                            number = "4",
                            icon = Icons.Default.LockOpen,
                            title = "Level Up Progress",
                            description = "Achieve the target score to finish the vocabulary builder and complete the overall level!"
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false }
                ) {
                    Text("Got it!")
                }
            }
        )
    }
}

@Composable
fun HelpStepItem(
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun LevelSelector(
    allProgress: List<LevelProgress>,
    currentLevelId: Int,
    onLevelSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "Select Level",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(LevelsData.levels) { level ->
                val progress = allProgress.find { it.levelId == level.id }
                val unlocked = progress?.unlocked ?: (level.id == 1)
                val completed = (progress?.isCrosswordCompleted == true) && (progress.isVocabCompleted)
                val isSelected = level.id == currentLevelId

                val containerColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    completed -> MaterialTheme.colorScheme.tertiaryContainer
                    unlocked -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val contentColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    completed -> MaterialTheme.colorScheme.onTertiaryContainer
                    unlocked -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }

                Card(
                    modifier = Modifier
                        .width(145.dp)
                        .testTag("level_card_${level.id}")
                        .clickable(enabled = unlocked) { onLevelSelected(level.id) },
                    colors = CardDefaults.cardColors(containerColor = containerColor)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Lvl ${level.id}",
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                fontSize = 18.sp
                            )
                            if (!unlocked) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = contentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (completed) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = contentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = level.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = contentColor.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CrosswordTab(viewModel: GameViewModel, onOpenHelp: () -> Unit) {
    val currentLevelId by viewModel.currentLevelId.collectAsState()
    val grid by viewModel.crosswordGrid.collectAsState()
    val selectedCell by viewModel.selectedCell.collectAsState()
    val activeWord by viewModel.activeWord.collectAsState()
    val isCorrect by viewModel.isCrosswordCorrect.collectAsState()

    val level = LevelsData.levels.find { it.id == currentLevelId } ?: return

    // Precalculate cells starting points for indicators
    val startCellNumbers = remember(level) {
        val startCoords = level.words.map { Pair(it.row, it.col) }.distinct()
        level.words.associate { word ->
            val index = startCoords.indexOf(Pair(word.row, word.col))
            Pair(word.row, word.col) to (index + 1)
        }
    }

    // Modal state for showing word definitions generated by Gemini
    var showDefinitionDialog by remember { mutableStateOf(false) }
    var activeDefinitionWord by remember { mutableStateOf("") }
    val aiState by viewModel.aiMeaningState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 180.dp), // Clear space for the docked keyboard
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dismissible Help & Tutorial Banner
            var dismissedHelpBanner by remember { mutableStateOf(false) }
            if (!dismissedHelpBanner) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onOpenHelp() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Help Guide",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "How to Play Crossword?",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "New to Crossword or need a quick refresher? Tap here for our easy 1-minute visual tutorial!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(
                            onClick = { dismissedHelpBanner = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close help banner",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Validation Status Alert
            AnimatedVisibility(visible = isCorrect != null) {
                val containerColor = if (isCorrect == true) Color(0xFFE2F9E9) else Color(0xFFFCE8E6)
                val textColor = if (isCorrect == true) Color(0xFF1E7E34) else Color(0xFFC71C1C)
                val icon = if (isCorrect == true) Icons.Default.CheckCircle else Icons.Default.Info
                val message = if (isCorrect == true) 
                    "Amazing! The crossword is perfectly complete! Level progression unlocked." 
                else 
                    "Some letters are incorrect. Review the words and try again!"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = textColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = message, color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }

            // Draw Crossword Grid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (r in 0 until level.gridHeight) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            for (c in 0 until level.gridWidth) {
                                val coord = Pair(r, c)
                                val matchingWords = level.words.filter { word ->
                                    if (word.isAcross) {
                                        r == word.row && c >= word.col && c < word.col + word.length
                                    } else {
                                        c == word.col && r == word.row && r < word.row + word.length
                                    }
                                }
                                val isPlayableCell = matchingWords.isNotEmpty()
                                val enteredLetter = grid[coord] ?: ' '
                                val numberIndicator = startCellNumbers[coord]

                                if (isPlayableCell) {
                                    val isSelected = selectedCell == coord
                                    val isActiveWordCell = activeWord?.let { w ->
                                        if (w.isAcross) {
                                            r == w.row && c >= w.col && c < w.col + w.length
                                        } else {
                                            c == w.col && r >= w.row && r < w.row + w.length
                                        }
                                    } ?: false

                                    val cellColor = when {
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        isActiveWordCell -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }

                                    val borderColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .testTag("cell_${r}_${c}")
                                            .background(cellColor, RoundedCornerShape(4.dp))
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.2.dp,
                                                color = borderColor,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable { viewModel.selectCell(r, c) }
                                    ) {
                                        // Word Number Indicator
                                        if (numberIndicator != null) {
                                            Text(
                                                text = numberIndicator.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .padding(start = 4.dp, top = 2.dp)
                                                    .align(Alignment.TopStart)
                                            )
                                        }

                                        // Letter Text
                                        Text(
                                            text = enteredLetter.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                } else {
                                    // Blocked Cell (Black square)
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(Color.DarkGray.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Active Word Clue Box
            activeWord?.let { word ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (word.isAcross) "ACROSS CLUE" else "DOWN CLUE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )

                            // "Ask Gemini" button for instant definition
                            Button(
                                onClick = {
                                    activeDefinitionWord = word.word
                                    viewModel.lookupCustomWord(word.word)
                                    showDefinitionDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("ai_clue_button")
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gemini AI Definition", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = word.clue,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            } ?: run {
                Text(
                    text = "Tap any grid cell to focus a word & read its clue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Action Buttons: Verify & Help
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.validateCrossword() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("validate_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify Solution", fontWeight = FontWeight.Bold)
                }
            }

            // Across & Down List
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Level Clues",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Across
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ACROSS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    level.words.filter { it.isAcross }.forEach { word ->
                        val isWordActive = activeWord?.id == word.id && activeWord?.isAcross == true
                        Text(
                            text = "${word.col + 1}. ${word.clue}",
                            fontSize = 12.sp,
                            fontWeight = if (isWordActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isWordActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectClue(word) }
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                // Down
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DOWN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.secondary, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    level.words.filter { !it.isAcross }.forEach { word ->
                        val isWordActive = activeWord?.id == word.id && activeWord?.isAcross == false
                        Text(
                            text = "${word.row + 1}. ${word.clue}",
                            fontSize = 12.sp,
                            fontWeight = if (isWordActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isWordActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectClue(word) }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Docked Keyboard for Crossword Entries
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
        ) {
            CrosswordKeyboard(
                onKeyTyped = { viewModel.enterLetter(it) },
                onBackspace = { viewModel.deleteLetter() }
            )
        }
    }

    // AI Meaning bottom dialog
    if (showDefinitionDialog) {
        AlertDialog(
            onDismissRequest = {
                showDefinitionDialog = false
                viewModel.clearAiMeaningState()
            },
            title = {
                Text(
                    text = "Gemini AI: $activeDefinitionWord",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = aiState) {
                        is AiMeaningUiState.Loading -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Generating AI definition...")
                            }
                        }
                        is AiMeaningUiState.Success -> {
                            Column {
                                Text(
                                    text = state.definition.partOfSpeech,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.definition.meaning,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (state.definition.example.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Example:",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = "\"${state.definition.example}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Source: ${state.definition.source}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        is AiMeaningUiState.Error -> {
                            Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        }
                        else -> {
                            Text("No request sent.")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDefinitionDialog = false
                        viewModel.clearAiMeaningState()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun CrosswordKeyboard(onKeyTyped: (Char) -> Unit, onBackspace: () -> Unit) {
    val keys = listOf(
        listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'),
        listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'),
        listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keys.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { char ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("key_$char")
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .clickable { onKeyTyped(char) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Add Delete button in the last row
                if (rowIndex == 2) {
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("key_delete")
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(4.dp))
                            .clickable { onBackspace() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Backspace",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VocabTab(viewModel: GameViewModel, onOpenHelp: () -> Unit) {
    val quizState by viewModel.vocabQuizState.collectAsState()
    val currentLevelId by viewModel.currentLevelId.collectAsState()

    if (quizState.questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No quiz available for this level.")
        }
        return
    }

    val currentQuestion = quizState.questions[quizState.currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dismissible Help & Tutorial Banner
        var dismissedHelpBanner by remember { mutableStateOf(false) }
        if (!dismissedHelpBanner) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { onOpenHelp() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Help Guide",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "How to Play Vocabulary Game?",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Need a hand or want to see how to build your score? Tap here for our easy 1-minute visual tutorial!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(
                        onClick = { dismissedHelpBanner = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close help banner",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Quiz Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vocabulary Builder",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Spell the word based on the definition!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Score: ${quizState.score}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (quizState.isFinished) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Level Complete!",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = quizState.feedbackMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.selectLevel(currentLevelId) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Replay Quiz")
                    }
                }
            }
        } else {
            // Display Question Card
            Text(
                text = "Question ${quizState.currentIndex + 1} of ${quizState.questions.size}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            LinearProgressIndicator(
                progress = (quizState.currentIndex.toFloat() + 1) / quizState.questions.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DEFINITION:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentQuestion.clue,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SCRAMBLED LETTERS:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentQuestion.scrambled,
                        letterSpacing = 6.sp,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text entered box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = quizState.userInput.ifEmpty { "ENTER SPELLING..." },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    color = if (quizState.userInput.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of scrambled letter buttons to help type easily!
            val availableLetters = currentQuestion.scrambled.map { it.toString() }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 54.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableLetters.size) { index ->
                    val letter = availableLetters[index]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("vocab_letter_$index")
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                            .clickable { viewModel.enterVocabLetter(letter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Controls: Delete Letter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { viewModel.deleteVocabLetter() },
                    modifier = Modifier.testTag("vocab_delete")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Last")
                }
            }

            // Feedback and Submit Actions
            if (quizState.isCorrect != null) {
                val bannerColor = if (quizState.isCorrect == true) Color(0xFFE2F9E9) else Color(0xFFFCE8E6)
                val textColor = if (quizState.isCorrect == true) Color(0xFF1E7E34) else Color(0xFFC71C1C)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = bannerColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = quizState.feedbackMessage,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.nextVocabQuestion() },
                            colors = ButtonDefaults.buttonColors(containerColor = textColor),
                            modifier = Modifier.align(Alignment.End).testTag("vocab_next_button")
                        ) {
                            Text("Next Question")
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.submitVocabAnswer() },
                        enabled = quizState.userInput.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("vocab_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Submit Answer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WordBankTab(viewModel: GameViewModel) {
    val savedWords by viewModel.savedWords.collectAsState()
    val aiState by viewModel.aiMeaningState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableIntStateOf(0) } // 0 = All, 1 = Difficult, 2 = Learned
    var customWordInput by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    val filteredWords = savedWords.filter { wordEntity ->
        val matchesQuery = wordEntity.word.contains(searchQuery, ignoreCase = true) ||
                wordEntity.meaning.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filterType) {
            0 -> true
            1 -> wordEntity.isDifficult
            2 -> wordEntity.isLearned
            else -> true
        }
        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AI Generator Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dynamic AI Dictionary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Enter any word below to dynamically trigger Gemini to fetch definitions, parts of speech, and usage sentences!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customWordInput,
                            onValueChange = { customWordInput = it },
                            placeholder = { Text("E.g., Serendipity, Ephemeral", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("ai_lookup_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Button(
                            onClick = {
                                if (customWordInput.isNotEmpty()) {
                                    viewModel.lookupCustomWord(customWordInput)
                                    focusManager.clearFocus()
                                }
                            },
                            enabled = customWordInput.isNotEmpty() && aiState !is AiMeaningUiState.Loading,
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("ai_lookup_button")
                        ) {
                            if (aiState is AiMeaningUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                        }
                    }

                    // Display AI Lookup Result Card inline!
                    AnimatedVisibility(visible = aiState !is AiMeaningUiState.Idle) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "AI Lookup Result",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearAiMeaningState() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                when (val state = aiState) {
                                    is AiMeaningUiState.Loading -> {
                                        Text("Consulting Gemini AI...", fontSize = 13.sp)
                                    }
                                    is AiMeaningUiState.Success -> {
                                        Text(
                                            text = "${state.definition.word} (${state.definition.partOfSpeech})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = state.definition.meaning,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                        if (state.definition.example.isNotEmpty()) {
                                            Text(
                                                text = "\"${state.definition.example}\"",
                                                fontSize = 12.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = "Saved automatically to word bank!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E7E34),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    is AiMeaningUiState.Error -> {
                                        Text(
                                            text = "Error: ${state.message}",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 13.sp
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search & Filters Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Words & Definitions") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_bar"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable category tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Starred (Study)", "Mastered").forEachIndexed { index, label ->
                        val isSelected = filterType == index
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(containerColor)
                                .clickable { filterType = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = label, fontWeight = FontWeight.Bold, color = contentColor, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Saved Words list count
        item {
            Text(
                text = "${filteredWords.size} words available",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Words list items
        if (filteredWords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "No words match your filters. Complete crosswords, play vocab quiz, or lookup words above to add them!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredWords, key = { it.word }) { word ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("word_card_${word.word}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = word.word,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = word.partOfSpeech,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            // Interactive star & learn flags (Touch targets 48x48)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleWordDifficulty(word) },
                                    modifier = Modifier.size(48.dp).testTag("star_btn_${word.word}")
                                ) {
                                    Icon(
                                        imageVector = if (word.isDifficult) Icons.Default.Star else Icons.Filled.Star,
                                        tint = if (word.isDifficult) Color(0xFFFFC107) else Color.LightGray,
                                        contentDescription = "Toggle Star Study"
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleWordLearned(word) },
                                    modifier = Modifier.size(48.dp).testTag("check_btn_${word.word}")
                                ) {
                                    Icon(
                                        imageVector = if (word.isLearned) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                        tint = if (word.isLearned) Color(0xFF1E7E34) else Color.LightGray,
                                        contentDescription = "Toggle Mastered"
                                    )
                                }
                            }
                        }

                        Text(
                            text = word.meaning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        if (word.example.isNotEmpty()) {
                            Text(
                                text = "\"${word.example}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (word.levelId > 0) {
                            Text(
                                text = "Discovered in Level ${word.levelId}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
