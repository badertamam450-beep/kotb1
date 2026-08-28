package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.FeasibilityPlan
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetScreen(
    currentPlan: FeasibilityPlan,
    onSavePlan: (FeasibilityPlan) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var projectName by remember { mutableStateOf(currentPlan.projectName) }
    var startupCapital by remember { mutableStateOf(currentPlan.startupCapital) }
    var monthlyCosts by remember { mutableStateOf(currentPlan.monthlyCosts) }
    var expectedRevenue by remember { mutableStateOf(currentPlan.expectedRevenue) }
    var skillsRequired by remember { mutableStateOf(currentPlan.skillsRequired) }
    var executionSteps by remember { mutableStateOf(currentPlan.executionSteps) }
    var timelineYears by remember { mutableStateOf(currentPlan.timelineYears) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    // Calculations
    val rev = expectedRevenue.toDoubleOrNull() ?: 0.0
    val cost = monthlyCosts.toDoubleOrNull() ?: 0.0
    val monthlyProfit = rev - cost
    val yearlyProfit = monthlyProfit * 12

    Scaffold(
        modifier = modifier.testTag("worksheet_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("مخطط رصد المشاريع والبدء من الصفر") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("worksheet_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareSummary = """
📋 مخطط دراسة جدوى: $projectName
— مستوحى من كتاب (ابدأ من الصفر) للدكتور مالك الرميمة

💰 رأس المال الأولي: $startupCapital
📉 التكاليف الشهرية: $monthlyCosts
📈 الإيراد الشهري المتوقع: $expectedRevenue
💵 صافي الربح الشهري المتوقع: $monthlyProfit
🛠️ المهارات والمهن المطلوبة: $skillsRequired
⏳ خطة المسيرة ($timelineYears سنوات): $executionSteps
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareSummary)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة مخطط المشروع"))
                        },
                        modifier = Modifier.testTag("share_plan_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة المخطط"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Button(
                    onClick = {
                        val updated = FeasibilityPlan(
                            projectName = projectName,
                            startupCapital = startupCapital,
                            monthlyCosts = monthlyCosts,
                            expectedRevenue = expectedRevenue,
                            skillsRequired = skillsRequired,
                            executionSteps = executionSteps,
                            timelineYears = timelineYears
                        )
                        onSavePlan(updated)
                        snackbarMessage = "تم حفظ مخطط دراسة الجدوى بنجاح ✅"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp)
                        .testTag("save_plan_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ المخطط والدراسة", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(IslamicEmerald, IslamicGold))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = IslamicGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "منهجية د. مالك الرميمة للمشاريع",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "«تشكل قوائم تذكر فيها اسم المشروع وطرق تجهيزه وتكاليفه والنسب الخارجة والخسارة والربح.. والصبر والتوكل على الله، وانطلاقاً من الصفر لتبني بيوتاً في الحجر راسخة رسوخ الجبل».",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Project Name
            item {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("١. اسم فكرة المشروع أو الهدف المهني") },
                    placeholder = { Text("مثال: ورشة حرفية / متجر رقمي / تعلم برمجة") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_project_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            // Financial Feasibility Inputs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startupCapital,
                        onValueChange = { startupCapital = it },
                        label = { Text("رأس المال الأولي") },
                        placeholder = { Text("0 إذا بدأت من الصفر") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("plan_capital_input"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = monthlyCosts,
                        onValueChange = { monthlyCosts = it },
                        label = { Text("المصاريف الشهرية") },
                        placeholder = { Text("مثال: 500") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("plan_costs_input"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = expectedRevenue,
                    onValueChange = { expectedRevenue = it },
                    label = { Text("الإيراد الشهري المتوقع") },
                    placeholder = { Text("مثال: 1200") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_revenue_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Financial Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (monthlyProfit >= 0) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "تحليل الجدوى والأرباح المتوقعة:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("صافي الربح الشهري:")
                            Text(
                                text = "$monthlyProfit",
                                fontWeight = FontWeight.Bold,
                                color = if (monthlyProfit >= 0) IslamicEmerald else MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("صافي العائد السنوي المتوقع:")
                            Text(
                                text = "$yearlyProfit",
                                fontWeight = FontWeight.Bold,
                                color = if (yearlyProfit >= 0) IslamicEmerald else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Required Skills & Crafts
            item {
                OutlinedTextField(
                    value = skillsRequired,
                    onValueChange = { skillsRequired = it },
                    label = { Text("٢. المهارات والحرف المطلوبة لاكتسابها (أكثروا من المهن)") },
                    placeholder = { Text("مثال: مهارة التسويق، الصيانة، التصميم، المحاسبة...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("plan_skills_input"),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 4
                )
            }

            // 4-Year Execution Plan
            item {
                OutlinedTextField(
                    value = executionSteps,
                    onValueChange = { executionSteps = it },
                    label = { Text("٣. خطوات التنفيذ ومسيرة السنوات ($timelineYears سنوات)") },
                    placeholder = { Text("السنة الأولى: تعلم المهارة والتطبيق من الصفر\nالسنة الثانية: إطلاق المشروع وتحقيق أول دخل\nالسنة الثالثة والرابعة: مضاعفة الأرقام والتوسع") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("plan_steps_input"),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 6
                )
            }
        }
    }
}
