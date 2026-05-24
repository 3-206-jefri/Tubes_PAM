package com.example.pocketguard.presentation.screens.ai


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketguard.presentation.theme.PgPrimary
import com.example.pocketguard.presentation.theme.PgPrimaryLight
import org.koin.compose.viewmodel.koinViewModel

// Catatan: Jika ChatMessage sudah dideklarasikan di AIAssistantViewModel.kt,
// Anda bisa menghapus data class ini agar tidak bentrok (duplicate class).
// Jika belum, biarkan saja di sini.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    initialText: String?,
    onNavigateBack: () -> Unit,
    viewModel: AIAssistantViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var promptInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // 1. MENGAMBIL DATA DARI VIEWMODEL (Bukan lagi simulasi lokal)
    val chatMessages = uiState.messages

    LaunchedEffect(initialText) {
        if (!initialText.isNullOrBlank()) {
            promptInput = initialText
        }
    }

    // 2. OTOMATIS SCROLL SAAT ADA PESAN BARU ATAU LOADING
    LaunchedEffect(chatMessages.size, uiState.isLoading) {
        if (chatMessages.isNotEmpty()) {
            // Tambah target scroll jika ada indikator loading di paling bawah
            val targetIndex = if (uiState.isLoading) chatMessages.size else chatMessages.size - 1
            listState.animateScrollToItem(targetIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asisten Keuangan", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().imePadding(),
                tonalElevation = 2.dp,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        placeholder = { Text("Tanya sesuatu ke PocketGuard AI...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PgPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    IconButton(
                        onClick = {
                            if (promptInput.isNotBlank()) {
                                // 3. MENGIRIM PESAN ASLI KE GEMINI API VIA VIEWMODEL
                                viewModel.sendMessage(promptInput)
                                promptInput = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = PgPrimary),
                        modifier = Modifier.size(44.dp),
                        // Tombol dinonaktifkan jika input kosong atau AI sedang loading membalas
                        enabled = promptInput.isNotBlank() && !uiState.isLoading
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(color = PgPrimaryLight, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color = PgPrimary, shape = CircleShape)
                )
                Text(
                    text = "AI Aktif",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PgPrimary
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(chatMessages) { message ->
                    val isUser = message.isUser

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) PgPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isUser) 12.dp else 2.dp,
                                bottomEnd = if (isUser) 2.dp else 12.dp
                            ),
                            border = if (isUser) null else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text(
                                text = message.text,
                                fontSize = 13.sp,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // 4. INDIKATOR LOADING AI MENGETIK
                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = PgPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI sedang mengetik...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}