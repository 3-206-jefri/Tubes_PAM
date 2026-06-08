package com.example.pocketguard.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// HAPUS import java.* dari sini

@Composable
fun BudgetProgressBar(
    totalExpense: Double,
    budgetLimit: Double,
    onEditClick: () -> Unit
) {
    // Menghindari pembagian dengan nol
    val percentage = if (budgetLimit > 0) (totalExpense / budgetLimit) else 0.0
    val progress = percentage.coerceIn(0.0, 1.0).toFloat()

    // Logika perubahan warna psikologis
    val progressColor = when {
        percentage >= 0.9 -> Color(0xFFE53935) // Merah (Bahaya: >90%)
        percentage >= 0.7 -> Color(0xFFFFA000) // Oranye (Peringatan: >70%)
        else -> Color(0xFF43A047)              // Hijau (Aman)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Batas Pengeluaran Bulan Ini",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Anggaran",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar Visual
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Terpakai: Rp ${formatRupiahKmp(totalExpense)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (budgetLimit > 0) "Batas: Rp ${formatRupiahKmp(budgetLimit)}" else "Batas: Belum diatur",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SetBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    // Jika budget 0, kosongkan inputan agar mudah diketik
    var inputValue by remember {
        mutableStateOf(if (currentBudget > 0) currentBudget.toLong().toString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur Batas Pengeluaran", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column {
                Text(
                    text = "Tentukan batas maksimal pengeluaran Anda bulan ini agar kantong tetap aman.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { newValue ->
                        // Hanya izinkan input angka
                        if (newValue.all { it.isDigit() }) {
                            inputValue = newValue
                        }
                    },
                    label = { Text("Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val budget = inputValue.toDoubleOrNull() ?: 0.0
                onSave(budget)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

/**
 * 🛠️ HELPER KMP: Fungsi manual untuk memformat angka menjadi format ribuan Rupiah (contoh: 1.500.000).
 * Menggunakan logika string murni Kotlin tanpa bergantung pada Java.
 */
private fun formatRupiahKmp(amount: Double): String {
    val longAmount = amount.toLong()
    val stringAmount = longAmount.toString()
    val reversedString = stringAmount.reversed()
    val stringBuilder = StringBuilder()

    for (i in reversedString.indices) {
        if (i > 0 && i % 3 == 0) {
            stringBuilder.append('.')
        }
        stringBuilder.append(reversedString[i])
    }

    return stringBuilder.reverse().toString()
}