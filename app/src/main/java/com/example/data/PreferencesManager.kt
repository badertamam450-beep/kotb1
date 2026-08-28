package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class ReaderThemeMode {
    PARCHMENT, // Warm book page style (Office / Classic Book design)
    LIGHT,     // Crisp Modern Light
    DARK,      // Deep Slate Night
    EMERALD    // Luxury Emerald Gold
}

data class UserNote(
    val id: String,
    val chapterId: Int,
    val chapterTitle: String,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class FeasibilityPlan(
    val projectName: String = "",
    val startupCapital: String = "",
    val monthlyCosts: String = "",
    val expectedRevenue: String = "",
    val skillsRequired: String = "",
    val executionSteps: String = "",
    val timelineYears: String = "4"
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("book_reader_prefs", Context.MODE_PRIVATE)

    private val _lastReadChapterId = MutableStateFlow(prefs.getInt(KEY_LAST_CHAPTER, 1))
    val lastReadChapterId: StateFlow<Int> = _lastReadChapterId.asStateFlow()

    private val _fontSizeSp = MutableStateFlow(prefs.getInt(KEY_FONT_SIZE, 18))
    val fontSizeSp: StateFlow<Int> = _fontSizeSp.asStateFlow()

    private val _themeMode = MutableStateFlow(
        try {
            ReaderThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ReaderThemeMode.PARCHMENT.name) ?: ReaderThemeMode.PARCHMENT.name)
        } catch (e: Exception) {
            ReaderThemeMode.PARCHMENT
        }
    )
    val themeMode: StateFlow<ReaderThemeMode> = _themeMode.asStateFlow()

    private val _bookmarkedChapters = MutableStateFlow(loadBookmarks())
    val bookmarkedChapters: StateFlow<Set<Int>> = _bookmarkedChapters.asStateFlow()

    private val _notes = MutableStateFlow(loadNotes())
    val notes: StateFlow<List<UserNote>> = _notes.asStateFlow()

    private val _savedPlan = MutableStateFlow(loadPlan())
    val savedPlan: StateFlow<FeasibilityPlan> = _savedPlan.asStateFlow()

    fun setLastReadChapter(chapterId: Int) {
        prefs.edit().putInt(KEY_LAST_CHAPTER, chapterId).apply()
        _lastReadChapterId.value = chapterId
    }

    fun setFontSize(size: Int) {
        val clamped = size.coerceIn(14, 28)
        prefs.edit().putInt(KEY_FONT_SIZE, clamped).apply()
        _fontSizeSp.value = clamped
    }

    fun setThemeMode(mode: ReaderThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun toggleBookmark(chapterId: Int) {
        val current = _bookmarkedChapters.value.toMutableSet()
        if (current.contains(chapterId)) {
            current.remove(chapterId)
        } else {
            current.add(chapterId)
        }
        val array = JSONArray()
        current.forEach { array.put(it) }
        prefs.edit().putString(KEY_BOOKMARKS, array.toString()).apply()
        _bookmarkedChapters.value = current
    }

    fun addNote(chapterId: Int, chapterTitle: String, text: String) {
        if (text.isBlank()) return
        val current = _notes.value.toMutableList()
        val newNote = UserNote(
            id = System.currentTimeMillis().toString(),
            chapterId = chapterId,
            chapterTitle = chapterTitle,
            noteText = text.trim()
        )
        current.add(0, newNote)
        saveNotes(current)
    }

    fun deleteNote(noteId: String) {
        val current = _notes.value.filter { it.id != noteId }
        saveNotes(current)
    }

    fun savePlan(plan: FeasibilityPlan) {
        val json = JSONObject().apply {
            put("projectName", plan.projectName)
            put("startupCapital", plan.startupCapital)
            put("monthlyCosts", plan.monthlyCosts)
            put("expectedRevenue", plan.expectedRevenue)
            put("skillsRequired", plan.skillsRequired)
            put("executionSteps", plan.executionSteps)
            put("timelineYears", plan.timelineYears)
        }
        prefs.edit().putString(KEY_SAVED_PLAN, json.toString()).apply()
        _savedPlan.value = plan
    }

    private fun loadBookmarks(): Set<Int> {
        val raw = prefs.getString(KEY_BOOKMARKS, null) ?: return emptySet()
        return try {
            val array = JSONArray(raw)
            val set = mutableSetOf<Int>()
            for (i in 0 until array.length()) {
                set.add(array.getInt(i))
            }
            set
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun loadNotes(): List<UserNote> {
        val raw = prefs.getString(KEY_NOTES, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<UserNote>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    UserNote(
                        id = obj.getString("id"),
                        chapterId = obj.getInt("chapterId"),
                        chapterTitle = obj.getString("chapterTitle"),
                        noteText = obj.getString("noteText"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveNotes(notes: List<UserNote>) {
        val array = JSONArray()
        notes.forEach { note ->
            val obj = JSONObject().apply {
                put("id", note.id)
                put("chapterId", note.chapterId)
                put("chapterTitle", note.chapterTitle)
                put("noteText", note.noteText)
                put("timestamp", note.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_NOTES, array.toString()).apply()
        _notes.value = notes
    }

    private fun loadPlan(): FeasibilityPlan {
        val raw = prefs.getString(KEY_SAVED_PLAN, null) ?: return FeasibilityPlan()
        return try {
            val obj = JSONObject(raw)
            FeasibilityPlan(
                projectName = obj.optString("projectName", ""),
                startupCapital = obj.optString("startupCapital", ""),
                monthlyCosts = obj.optString("monthlyCosts", ""),
                expectedRevenue = obj.optString("expectedRevenue", ""),
                skillsRequired = obj.optString("skillsRequired", ""),
                executionSteps = obj.optString("executionSteps", ""),
                timelineYears = obj.optString("timelineYears", "4")
            )
        } catch (e: Exception) {
            FeasibilityPlan()
        }
    }

    companion object {
        private const val KEY_LAST_CHAPTER = "key_last_chapter"
        private const val KEY_FONT_SIZE = "key_font_size"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_BOOKMARKS = "key_bookmarks"
        private const val KEY_NOTES = "key_notes"
        private const val KEY_SAVED_PLAN = "key_saved_plan"
    }
}
