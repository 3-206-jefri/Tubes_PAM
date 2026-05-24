package com.example.pocketguard.presentation.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.domain.model.TransactionCategory
import com.example.pocketguard.domain.model.TransactionType
import com.example.pocketguard.domain.usecase.GetAllTransactionsUseCase // 👈 PERBAIKAN IMPORT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Struktur data bersih untuk UI
data class CategoryStat(val category: TransactionCategory, val totalAmount: Double)
data class DailyStat(val dayName: String, val income: Double, val expense: Double)

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val totalExpense: Double = 0.0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val weeklyStats: List<DailyStat> = emptyList()
)

class AnalyticsViewModel(
    // 👈 PERBAIKAN: Menggunakan kelas UseCase yang spesifik sesuai yang Anda buat
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            // 👈 PERBAIKAN: Memanggil UseCase langsung
            getAllTransactionsUseCase().collect { transactions ->

                // 1. Hitung Total Pengeluaran dan Kelompokkan Berdasarkan Kategori
                val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                val totalExp = expenses.sumOf { it.amount }

                val catStats = expenses.groupBy { it.category }
                    .map { (category, txList) ->
                        CategoryStat(category = category, totalAmount = txList.sumOf { it.amount })
                    }
                    .sortedByDescending { it.totalAmount } // Urutkan dari pengeluaran terbesar

                // 2. Hitung Tren Mingguan (Dikelompokkan Berdasarkan Hari)
                val dayMap = mutableMapOf<String, Pair<Double, Double>>() // Day -> (Income, Expense)

                // Inisialisasi hari agar urutannya rapi
                val daysOfWeek = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                daysOfWeek.forEach { dayMap[it] = Pair(0.0, 0.0) }

                transactions.forEach { tx ->
                    val instant = Instant.fromEpochMilliseconds(tx.createdAt)
                    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

                    val dayName = when (dateTime.dayOfWeek.name) {
                        "MONDAY" -> "Sen"
                        "TUESDAY" -> "Sel"
                        "WEDNESDAY" -> "Rab"
                        "THURSDAY" -> "Kam"
                        "FRIDAY" -> "Jum"
                        "SATURDAY" -> "Sab"
                        "SUNDAY" -> "Min"
                        else -> "Sen"
                    }

                    val currentStats = dayMap[dayName] ?: Pair(0.0, 0.0)
                    if (tx.type == TransactionType.INCOME) {
                        dayMap[dayName] = currentStats.copy(first = currentStats.first + tx.amount)
                    } else {
                        dayMap[dayName] = currentStats.copy(second = currentStats.second + tx.amount)
                    }
                }

                val weekStats = daysOfWeek.map { day ->
                    DailyStat(dayName = day, income = dayMap[day]!!.first, expense = dayMap[day]!!.second)
                }

                // 3. Perbarui UI State agar layar melakukan Re-compose (Render ulang)
                _uiState.value = AnalyticsUiState(
                    isLoading = false,
                    totalExpense = totalExp,
                    categoryStats = catStats,
                    weeklyStats = weekStats
                )
            }
        }
    }
}