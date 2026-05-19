package com.example.pocketguard.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketguard.domain.model.Transaction
import com.example.pocketguard.domain.model.TransactionCategory
import com.example.pocketguard.domain.model.TransactionType
import com.example.pocketguard.domain.usecase.TransactionSortBy
import com.example.pocketguard.presentation.components.EmptyState
import com.example.pocketguard.presentation.components.ErrorState
import com.example.pocketguard.presentation.components.LoadingIndicator
import com.example.pocketguard.presentation.components.TransactionCard
import org.koin.compose.viewmodel.koinViewModel

// Warna tema hijau PocketGuard
private val GreenDark = Color(0xFF1B5E20)
private val GreenMid = Color(0xFF2E7D32)
private val GreenLight = Color(0xFF43A047)
private val IncomeGreen = Color(0xFF2E7D32)
private val ExpenseRed = Color(0xFFB71C1C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSortBy by viewModel.sortBy.collectAsStateWithLifecycle()

    var showSearch by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Hitung summary dari semua transaksi (tidak terpengaruh filter)
    val allTransactions = when (val state = uiState) {
        is HomeUiState.Success -> state.transactions
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        SearchField(
                            query = when (val state = uiState) {
                                is HomeUiState.Success -> state.query
                                is HomeUiState.Empty -> state.query
                                else -> ""
                            },
                            onQueryChange = viewModel::onSearchQueryChange,
                            onClear = {
                                viewModel.clearSearch()
                                showSearch = false
                            }
                        )
                    } else {
                        Text(
                            "PocketGuard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    if (!showSearch) {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Cari")
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Outlined.Sort, contentDescription = "Urutkan")
                        }
                        SortDropdownMenu(
                            expanded = showSortMenu,
                            currentSortBy = currentSortBy,
                            onSortSelected = {
                                viewModel.onSortByChanged(it)
                                showSortMenu = false
                            },
                            onDismiss = { showSortMenu = false }
                        )
                    }
                    IconButton(onClick = onNavigateToAI) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = "AI Analyzer")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Pengaturan")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = GreenMid,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ===== SUMMARY CARD =====
            SummarySection(transactions = allTransactions)

            // ===== FILTER KATEGORI =====
            CategoryFilterRow(
                selectedCategory = when (val state = uiState) {
                    is HomeUiState.Success -> state.category
                    is HomeUiState.Empty -> state.category
                    else -> null
                },
                onCategorySelected = viewModel::onCategorySelected
            )

            // ===== LABEL RIWAYAT =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ===== KONTEN UTAMA =====
            when (val state = uiState) {
                is HomeUiState.Loading -> LoadingIndicator()

                is HomeUiState.Success -> {
                    TransactionsList(
                        transactions = state.transactions,
                        onTransactionClick = onNavigateToDetail,
                        onDeleteClick = viewModel::deleteTransaction
                    )
                }

                is HomeUiState.Empty -> {
                    EmptyState(
                        title = if (state.query.isNotBlank() || state.category != null)
                            "Tidak Ditemukan"
                        else
                            "Belum Ada Transaksi",
                        message = if (state.query.isNotBlank() || state.category != null)
                            "Coba ubah kata kunci atau filter"
                        else
                            "Mulai catat keuanganmu sekarang",
                        icon = {
                            Icon(
                                Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = GreenMid.copy(alpha = 0.5f)
                            )
                        }
                    )
                }

                is HomeUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.clearSearch() }
                    )
                }
            }
        }
    }
}

// ===== KOMPONEN SUMMARY SECTION =====
@Composable
private fun SummarySection(transactions: List<Transaction>) {
    val totalIncome = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val totalExpense = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    val totalBalance = totalIncome - totalExpense

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Card Total Saldo dengan gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(GreenDark, GreenLight)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Total Saldo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rp ${formatAmount(totalBalance)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Diperbarui barusan",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row Pemasukan & Pengeluaran
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Pemasukan
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(IncomeGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrendingUp,
                            contentDescription = null,
                            tint = IncomeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pemasukan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+${formatAmount(totalIncome)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }
            }

            // Card Pengeluaran
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ExpenseRed.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrendingDown,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pengeluaran",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "-${formatAmount(totalExpense)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    val long = amount.toLong()
    return when {
        long >= 1_000_000_000 -> "${long / 1_000_000_000}M"
        long >= 1_000_000 -> "${long / 1_000_000}Jt"
        else -> {
            // Manual formatting tanpa Java format
            val str = long.toString()
            val result = StringBuilder()
            str.reversed().forEachIndexed { index, c ->
                if (index > 0 && index % 3 == 0) result.append('.')
                result.append(c)
            }
            result.reverse().toString()
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Cari deskripsi transaksi...") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotBlank(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Hapus")
                }
            }
        }
    )
}

@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    currentSortBy: TransactionSortBy,
    onSortSelected: (TransactionSortBy) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        TransactionSortBy.entries.forEach { sortBy ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(sortBy.displayName)
                        if (sortBy == currentSortBy) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                onClick = { onSortSelected(sortBy) }
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: TransactionCategory?,
    onCategorySelected: (TransactionCategory?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Semua") }
            )
        }
        items(TransactionCategory.entries) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(if (selectedCategory == category) null else category) },
                label = { Text(category.displayName) }
            )
        }
    }
}

@Composable
private fun TransactionsList(
    transactions: List<Transaction>,
    onTransactionClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items = transactions, key = { it.id }) { transaction ->
            TransactionCard(
                transaction = transaction,
                onClick = { onTransactionClick(transaction.id) },
                onDeleteClick = { onDeleteClick(transaction.id) }
            )
        }
    }
}