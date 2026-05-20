package com.example.pocketguard.presentation.screens.analytics


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketguard.domain.model.TransactionCategory
import com.example.pocketguard.presentation.components.LoadingIndicator
import com.example.pocketguard.presentation.theme.PgDanger
import com.example.pocketguard.presentation.theme.PgPrimary
import com.example.pocketguard.presentation.theme.PgWarning
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analisis Grafik", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) }
                ,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // ==================== KARTU 1: DONUT CHART (ALOKASI) ====================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Alokasi Pengeluaran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.categoryStats.isEmpty()) {
                            Text(
                                text = "Belum ada data pengeluaran.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                                    Canvas(modifier = Modifier.size(110.dp)) {
                                        var startAngle = -90f
                                        uiState.categoryStats.forEach { stat ->
                                            val sweepAngle = ((stat.totalAmount / uiState.totalExpense) * 360f).toFloat()

                                            // Memetakan warna berdasarkan kategori (Domain Model)
                                            val color = when(stat.category) {
                                                TransactionCategory.FOOD -> PgPrimary
                                                TransactionCategory.TRANSPORT -> Color(0xFF4A90E2)
                                                TransactionCategory.BILLS -> PgWarning
                                                TransactionCategory.SALARY -> PgPrimary // Biasanya bukan pengeluaran
                                                TransactionCategory.OTHER -> PgDanger
                                            }

                                            drawArc(
                                                color = color,
                                                startAngle = startAngle,
                                                sweepAngle = sweepAngle,
                                                useCenter = false,
                                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                                                size = Size(size.width, size.height)
                                            )
                                            startAngle += sweepAngle
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("100%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PgPrimary)
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    uiState.categoryStats.forEach { stat ->
                                        val color = when(stat.category) {
                                            TransactionCategory.FOOD -> PgPrimary
                                            TransactionCategory.TRANSPORT -> Color(0xFF4A90E2)
                                            TransactionCategory.BILLS -> PgWarning
                                            TransactionCategory.SALARY -> PgPrimary
                                            TransactionCategory.OTHER -> PgDanger
                                        }
                                        val emoji = when(stat.category) {
                                            TransactionCategory.FOOD -> "🍜"
                                            TransactionCategory.TRANSPORT -> "🚗"
                                            TransactionCategory.BILLS -> "🏠"
                                            TransactionCategory.SALARY -> "💰"
                                            TransactionCategory.OTHER -> "📦"
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "$emoji ${stat.category.displayName}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== KARTU 2: BAR CHART (TREN MINGGUAN) ====================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tren Mingguan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(PgPrimary, CircleShape))
                                Text(" Pemasukan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(PgDanger, CircleShape))
                                Text(" Pengeluaran", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val containerWidth = maxWidth
                            val containerHeight = maxHeight

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Mencari nilai tertinggi agar grafik dinamis (tidak terpotong jika angkanya jutaan)
                                val maxIncome = uiState.weeklyStats.maxOfOrNull { it.income } ?: 0.0
                                val maxExpense = uiState.weeklyStats.maxOfOrNull { it.expense } ?: 0.0
                                val maxVal = maxOf(maxIncome, maxExpense, 1000.0).toFloat() // Minimal 1000 agar tidak bagi nol

                                val barWidth = 10.dp.toPx()
                                val groupSpacing = containerWidth.toPx() / uiState.weeklyStats.size

                                uiState.weeklyStats.forEachIndexed { index, data ->
                                    val xCenter = (index * groupSpacing) + (groupSpacing / 2)

                                    if (data.income > 0) {
                                        val incHeight = (data.income.toFloat() / maxVal) * containerHeight.toPx()
                                        drawRoundRect(
                                            color = PgPrimary,
                                            topLeft = Offset(xCenter - barWidth - 2.dp.toPx(), containerHeight.toPx() - incHeight),
                                            size = Size(barWidth, incHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                                        )
                                    }

                                    if (data.expense > 0) {
                                        val expHeight = (data.expense.toFloat() / maxVal) * containerHeight.toPx()
                                        drawRoundRect(
                                            color = PgDanger,
                                            topLeft = Offset(xCenter + 2.dp.toPx(), containerHeight.toPx() - expHeight),
                                            size = Size(barWidth, expHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            uiState.weeklyStats.forEach { data ->
                                Text(
                                    text = data.dayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}