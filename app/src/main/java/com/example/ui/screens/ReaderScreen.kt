package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.util.TtsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapterId: Int,
    isBookmarked: Boolean,
    fontSizeSp: Int,
    themeMode: ReaderThemeMode,
    ttsManager: TtsManager,
    onNavigateBack: () -> Unit,
    onNavigateToChapter: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onThemeModeChange: (ReaderThemeMode) -> Unit,
    onSaveNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chapter = remember(chapterId) {
        BookRepository.allChapters.find { it.id == chapterId } ?: BookRepository.allChapters.first()
    }
    val totalChapters = BookRepository.allChapters.size
    val listState = rememberLazyListState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    val isPlayingAudio by ttsManager.isPlaying.collectAsState()

    // Build plain text for TTS narration
    val chapterPlainText = remember(chapter) {
        val sb = StringBuilder()
        sb.append(chapter.title).append(". ")
        if (chapter.subtitle.isNotBlank()) sb.append(chapter.subtitle).append(". ")
        chapter.content.forEach { block ->
            when (block) {
                is ContentBlock.Paragraph -> sb.append(block.text).append(" ")
                is ContentBlock.Highlight -> sb.append(block.text).append(" ")
                is ContentBlock.Quote -> sb.append(block.text).append(" ")
                is ContentBlock.Verse -> sb.append("قال الله تعالى: ").append(block.text).append(" ")
                is ContentBlock.Subheading -> sb.append(block.text).append(". ")
                is ContentBlock.BulletList -> block.items.forEach { sb.append(it).append(". ") }
            }
        }
        chapter.quranVerses.forEach { verse ->
            sb.append("شاهد من القرآن الكريم، سورة ").append(verse.surah).append(": ").append(verse.text).append(". ")
        }
        sb.toString()
    }

    LaunchedEffect(chapterId) {
        listState.scrollToItem(0)
    }

    Scaffold(
        modifier = modifier.testTag("reader_screen_${chapter.id}"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "فصل ${chapter.id} من $totalChapters • صـ ${chapter.pageNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("reader_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة للرئيسية",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Audio speech button
                    IconButton(
                        onClick = {
                            if (isPlayingAudio) {
                                ttsManager.stop()
                            } else {
                                ttsManager.speak(chapterPlainText)
                            }
                        },
                        modifier = Modifier.testTag("reader_tts_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Default.StopCircle else Icons.Default.VolumeUp,
                            contentDescription = if (isPlayingAudio) "إيقاف القراءة الصوتية" else "بدء القراءة الصوتية",
                            tint = if (isPlayingAudio) IslamicGold else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Bookmark button
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.testTag("reader_bookmark_btn")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "إشارة مرجعية",
                            tint = if (isBookmarked) IslamicGold else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Reader Settings button
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("reader_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "تخصيص الخط والنمط",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Chapter Button
                    TextButton(
                        onClick = {
                            if (chapter.id > 1) onNavigateToChapter(chapter.id - 1)
                        },
                        enabled = chapter.id > 1,
                        modifier = Modifier.testTag("prev_chapter_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("السابق")
                    }

                    // Add Note Action
                    FilledTonalButton(
                        onClick = { showNoteDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_note_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تدوين فائدة")
                    }

                    // Next Chapter Button
                    TextButton(
                        onClick = {
                            if (chapter.id < totalChapters) onNavigateToChapter(chapter.id + 1)
                        },
                        enabled = chapter.id < totalChapters,
                        modifier = Modifier.testTag("next_chapter_btn")
                    ) {
                        Text("التالي")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
            ) {
                // Audio Player Active Banner
                if (isPlayingAudio) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = IslamicGold.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = IslamicGold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "جاري الاستماع إلى الفصل صوتياً...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(onClick = { ttsManager.stop() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "إيقاف",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Chapter Header
                item {
                    ChapterHeaderCard(
                        chapter = chapter,
                        isBookmarked = isBookmarked,
                        onToggleBookmark = onToggleBookmark,
                        onPlayAudio = {
                            if (isPlayingAudio) ttsManager.stop() else ttsManager.speak(chapterPlainText)
                        }
                    )
                }

                // Main Chapter Content Blocks
                items(chapter.content.size) { index ->
                    when (val block = chapter.content[index]) {
                        is ContentBlock.Paragraph -> {
                            Text(
                                text = block.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp * 1.7f).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is ContentBlock.Highlight -> {
                            HighlightCard(
                                text = block.text,
                                fontSizeSp = fontSizeSp
                            )
                        }

                        is ContentBlock.Quote -> {
                            QuoteCard(
                                text = block.text,
                                attribution = block.attribution,
                                fontSizeSp = fontSizeSp
                            )
                        }

                        is ContentBlock.Verse -> {
                            QuranVerseCard(
                                verse = QuranVerse(block.text, block.surah, block.ayah),
                                fontSizeSp = fontSizeSp
                            )
                        }

                        is ContentBlock.Subheading -> {
                            Text(
                                text = block.text,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        is ContentBlock.BulletList -> {
                            BulletListView(
                                items = block.items,
                                fontSizeSp = fontSizeSp
                            )
                        }
                    }
                }

                // Quranic Verses Section (if present)
                if (chapter.quranVerses.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "شواهد من القرآن الكريم في هذا الفصل",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(chapter.quranVerses.size) { index ->
                        QuranVerseCard(
                            verse = chapter.quranVerses[index],
                            fontSizeSp = fontSizeSp
                        )
                    }
                }

                // Key Takeaways
                if (chapter.keyTakeaways.isNotEmpty()) {
                    item {
                        KeyTakeawaysCard(takeaways = chapter.keyTakeaways)
                    }
                }

                // Share Excerpt / Action Row
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    val shareText = "فصل: ${chapter.title}\nمن كتاب (ابدأ من الصفر فالحياة لا تهب الأماني) للدكتور مالك عبدالرحمن الرميمة\n\n${chapter.content.filterIsInstance<ContentBlock.Paragraph>().firstOrNull()?.text ?: ""}"
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "مشاركة اقتباس من الفصل"))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مشاركة الفصل")
                            }

                            TextButton(
                                onClick = { showNoteDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoteAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إضافة ملاحظة")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsSheet) {
        ReaderSettingsBottomSheet(
            currentFontSize = fontSizeSp,
            onFontSizeChange = onFontSizeChange,
            currentTheme = themeMode,
            onThemeChange = onThemeModeChange,
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (showNoteDialog) {
        NoteInputDialog(
            chapterTitle = chapter.title,
            onSaveNote = { note ->
                onSaveNote(note)
                showNoteDialog = false
            },
            onDismiss = { showNoteDialog = false }
        )
    }
}
