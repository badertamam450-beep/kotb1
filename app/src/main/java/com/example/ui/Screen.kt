package com.example.ui

sealed class Screen {
    data object Home : Screen()
    data class Reader(val chapterId: Int) : Screen()
    data object Search : Screen()
    data object BookmarksAndNotes : Screen()
    data object Worksheet : Screen()
    data object AuthorInfo : Screen()
}
