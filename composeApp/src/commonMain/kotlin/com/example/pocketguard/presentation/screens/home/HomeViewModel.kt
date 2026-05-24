package com.example.pocketguard.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.domain.model.Transaction
import com.example.pocketguard.domain.usecase.DeleteTransactionUseCase
import com.example.pocketguard.domain.usecase.GetAllTransactionsUseCase
import com.example.pocketguard.domain.usecase.TransactionSortBy
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    // 🛠️ PERBAIKAN: Mengganti kategori menjadi filter Bulan
    private val _selectedMonth = MutableStateFlow<String?>(null)
    private val _sortBy = MutableStateFlow(TransactionSortBy.DATE_DESC)
    val sortBy: StateFlow<TransactionSortBy> = _sortBy.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        _query,
        _selectedMonth,
        _sortBy,
        getAllTransactionsUseCase()
    ) { query, selectedMonth, sort, transactions ->

        // 1. Ekstrak semua bulan unik dari data transaksi untuk Filter Chips
        val availableMonths = transactions.map { getMonthYear(it.createdAt) }.distinct()

        // 2. Filter berdasarkan Pencarian
        val filteredByQuery = if (query.isBlank()) {
            transactions
        } else {
            transactions.filter { it.description.contains(query, ignoreCase = true) }
        }

        // 3. Filter berdasarkan Bulan
        val filteredByMonth = if (selectedMonth == null) {
            filteredByQuery
        } else {
            filteredByQuery.filter { getMonthYear(it.createdAt) == selectedMonth }
        }

        // 4. Urutkan data
        val sortedTransactions = when (sort) {
            TransactionSortBy.DATE_ASC -> filteredByMonth.sortedBy { it.createdAt }
            TransactionSortBy.DATE_DESC -> filteredByMonth.sortedByDescending { it.createdAt }
            TransactionSortBy.AMOUNT_ASC -> filteredByMonth.sortedBy { it.amount }
            TransactionSortBy.AMOUNT_DESC -> filteredByMonth.sortedByDescending { it.amount }
            TransactionSortBy.CATEGORY -> filteredByMonth.sortedBy { it.category.name }
        }

        if (sortedTransactions.isEmpty()) {
            HomeUiState.Empty(query, selectedMonth, availableMonths)
        } else {
            HomeUiState.Success(sortedTransactions, query, selectedMonth, availableMonths)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    // ==================== USER ACTIONS ====================

    fun onSearchQueryChange(newQuery: String) { _query.value = newQuery }

    fun clearSearch() {
        _query.value = ""
        _selectedMonth.value = null
    }

    fun onMonthSelected(month: String?) { _selectedMonth.value = month }
    fun onSortByChanged(sort: TransactionSortBy) { _sortBy.value = sort }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            deleteTransactionUseCase(id)
        }
    }

    // 🛠️ FUNGSI HELPER: Mengubah timestamp ms ke format "Bulan Tahun" (Contoh: "Mei 2026")
    private fun getMonthYear(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
        return "${monthNames[dateTime.monthNumber - 1]} ${dateTime.year}"
    }
}

// ==================== UI STATE MODELS ====================

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val transactions: List<Transaction>,
        val query: String,
        val selectedMonth: String?,
        val availableMonths: List<String>
    ) : HomeUiState

    data class Empty(
        val query: String,
        val selectedMonth: String?,
        val availableMonths: List<String>
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}