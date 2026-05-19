package com.example.pocketguard.presentation.screens.add_transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketguard.domain.model.TransactionCategory
import com.example.pocketguard.domain.model.TransactionType
import com.example.pocketguard.presentation.components.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

private val GreenDark = Color(0xFF1B5E20)
private val GreenLight = Color(0xFF43A047)
private val IncomeGreen = Color(0xFF2E7D32)
private val ExpenseRed = Color(0xFFB71C1C)
private val ExpenseRedLight = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: Long?,
    initialType: String? = null,
    initialCategory: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Set initial type & category dari bottom sheet
    LaunchedEffect(initialType, initialCategory) {
        initialType?.let { typeStr ->
            val type = runCatching { TransactionType.valueOf(typeStr) }.getOrNull()
            type?.let { viewModel.onTypeChange(it) }
        }
        initialCategory?.let { catStr ->
            val category = runCatching { TransactionCategory.valueOf(catStr) }.getOrNull()
            category?.let { viewModel.onCategoryChange(it) }
        }
    }

    LaunchedEffect(transactionId) {
        transactionId?.let { viewModel.loadTransaction(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddTransactionEvent.TransactionSaved -> onNavigateBack()
                is AddTransactionEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val isExpense = uiState.type == TransactionType.EXPENSE
    val accentColor = if (isExpense) ExpenseRed else IncomeGreen
    val accentColorLight = if (isExpense) ExpenseRedLight else GreenLight

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditMode) "Edit Transaksi" else "Transaksi Baru",
                        fontWeight = FontWeight.Bold
                    )
                },
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
                    .verticalScroll(rememberScrollState())
            ) {
                // ===== HEADER GRADIENT =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = if (isExpense)
                                    listOf(Color(0xFF7F0000), accentColorLight)
                                else
                                    listOf(GreenDark, GreenLight)
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Toggle Pengeluaran / Pemasukan
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TransactionType.entries.forEach { type ->
                                val isSelected = uiState.type == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) Color.White.copy(alpha = 0.25f)
                                            else Color.Transparent
                                        )
                                        .clickable { viewModel.onTypeChange(type) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (type == TransactionType.EXPENSE) "💸 Pengeluaran" else "💰 Pemasukan",
                                        color = Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Nominal besar
                        Text(
                            text = "Rp",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 18.sp
                        )
                        BasicAmountInput(
                            value = uiState.amount,
                            onValueChange = viewModel::onAmountChange,
                            isError = uiState.amountError != null
                        )
                        if (uiState.amountError != null) {
                            Text(
                                text = uiState.amountError!!,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // ===== FORM =====
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Nama Transaksi",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        placeholder = { Text("Contoh: Makan siang kantor") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    CategoryGrid(
                        selectedCategory = uiState.category,
                        onCategorySelected = viewModel::onCategoryChange,
                        accentColor = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== TOMBOL SIMPAN =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveTransaction() },
                        enabled = uiState.canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = if (uiState.isEditMode) "Simpan Perubahan" else "Simpan Transaksi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicAmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(
                        "0",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun CategoryGrid(
    selectedCategory: TransactionCategory,
    onCategorySelected: (TransactionCategory) -> Unit,
    accentColor: Color
) {
    val categoryEmojis = mapOf(
        TransactionCategory.FOOD to "🍜",
        TransactionCategory.TRANSPORT to "🚗",
        TransactionCategory.BILLS to "🏠",
        TransactionCategory.SALARY to "💵",
        TransactionCategory.OTHER to "📦"
    )

    val chunked = TransactionCategory.entries.chunked(3)
    chunked.forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowItems.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) accentColor else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onCategorySelected(category) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = categoryEmojis[category] ?: "📦", fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) accentColor
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            repeat(3 - rowItems.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}