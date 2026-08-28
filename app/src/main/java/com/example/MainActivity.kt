package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.PreferencesManager
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.TtsManager

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var ttsManager: TtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(applicationContext)
        ttsManager = TtsManager(applicationContext)

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState()
            val lastReadChapterId by preferencesManager.lastReadChapterId.collectAsState()
            val fontSizeSp by preferencesManager.fontSizeSp.collectAsState()
            val bookmarkedChapterIds by preferencesManager.bookmarkedChapters.collectAsState()
            val notes by preferencesManager.notes.collectAsState()
            val savedPlan by preferencesManager.savedPlan.collectAsState()

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

            MyApplicationTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = currentScreen,
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    lastReadChapterId = lastReadChapterId,
                                    bookmarkedChapterIds = bookmarkedChapterIds,
                                    onSelectChapter = { chapterId ->
                                        preferencesManager.setLastReadChapter(chapterId)
                                        currentScreen = Screen.Reader(chapterId)
                                    },
                                    onOpenSearch = { currentScreen = Screen.Search },
                                    onOpenWorksheet = { currentScreen = Screen.Worksheet },
                                    onOpenBookmarks = { currentScreen = Screen.BookmarksAndNotes },
                                    onOpenAuthorInfo = { currentScreen = Screen.AuthorInfo }
                                )
                            }

                            is Screen.Reader -> {
                                BackHandler {
                                    ttsManager.stop()
                                    currentScreen = Screen.Home
                                }

                                ReaderScreen(
                                    chapterId = screen.chapterId,
                                    isBookmarked = bookmarkedChapterIds.contains(screen.chapterId),
                                    fontSizeSp = fontSizeSp,
                                    themeMode = themeMode,
                                    ttsManager = ttsManager,
                                    onNavigateBack = {
                                        ttsManager.stop()
                                        currentScreen = Screen.Home
                                    },
                                    onNavigateToChapter = { newChapterId ->
                                        ttsManager.stop()
                                        preferencesManager.setLastReadChapter(newChapterId)
                                        currentScreen = Screen.Reader(newChapterId)
                                    },
                                    onToggleBookmark = {
                                        preferencesManager.toggleBookmark(screen.chapterId)
                                    },
                                    onFontSizeChange = { newSize ->
                                        preferencesManager.setFontSize(newSize)
                                    },
                                    onThemeModeChange = { newTheme ->
                                        preferencesManager.setThemeMode(newTheme)
                                    },
                                    onSaveNote = { noteText ->
                                        val chapterTitle = "فصل ${screen.chapterId}"
                                        preferencesManager.addNote(screen.chapterId, chapterTitle, noteText)
                                    }
                                )
                            }

                            is Screen.Search -> {
                                BackHandler { currentScreen = Screen.Home }
                                SearchScreen(
                                    onNavigateBack = { currentScreen = Screen.Home },
                                    onSelectChapter = { chapterId ->
                                        preferencesManager.setLastReadChapter(chapterId)
                                        currentScreen = Screen.Reader(chapterId)
                                    }
                                )
                            }

                            is Screen.BookmarksAndNotes -> {
                                BackHandler { currentScreen = Screen.Home }
                                BookmarksAndNotesScreen(
                                    bookmarkedChapterIds = bookmarkedChapterIds,
                                    notes = notes,
                                    onSelectChapter = { chapterId ->
                                        preferencesManager.setLastReadChapter(chapterId)
                                        currentScreen = Screen.Reader(chapterId)
                                    },
                                    onDeleteNote = { noteId ->
                                        preferencesManager.deleteNote(noteId)
                                    },
                                    onNavigateBack = { currentScreen = Screen.Home }
                                )
                            }

                            is Screen.Worksheet -> {
                                BackHandler { currentScreen = Screen.Home }
                                WorksheetScreen(
                                    currentPlan = savedPlan,
                                    onSavePlan = { newPlan ->
                                        preferencesManager.savePlan(newPlan)
                                    },
                                    onNavigateBack = { currentScreen = Screen.Home }
                                )
                            }

                            is Screen.AuthorInfo -> {
                                BackHandler { currentScreen = Screen.Home }
                                AuthorScreen(
                                    onNavigateBack = { currentScreen = Screen.Home }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
