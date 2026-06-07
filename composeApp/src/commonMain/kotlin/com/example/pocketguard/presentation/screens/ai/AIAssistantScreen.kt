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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketguard.presentation.theme.PgPrimary
import com.example.pocketguard.presentation.theme.PgPrimaryLight
import org.koin.compose.viewmodel.koinViewModel

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
    val chatMessages = uiState.messages

    LaunchedEffect(initialText) {
        if (!initialText.isNullOrBlank()) {
            promptInput = initialText
        }
    }

    LaunchedEffect(chatMessages.size, uiState.isLoading) {
        if (chatMessages.isNotEmpty()) {
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
                                viewModel.sendMessage(promptInput)
                                promptInput = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = PgPrimary),
                        modifier = Modifier.size(44.dp),
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

            Spacer(modifier = Modifier.height(4.dp))

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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                items(chatMessages) { message ->
                    val isUser = message.isUser
                    val displayText: AnnotatedString = if (isUser) {
                        buildAnnotatedString { append(message.text) }
                    } else {
                        parseAdvancedMarkdown(message.text)
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) PgPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            border = if (isUser) null else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            Text(
                                text = displayText,
                                fontSize = 14.sp,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = PgPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI sedang berpikir...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🛠️ HELPER BARU: Advanced Markdown Parser (Fixed)
 * Mampu membaca Bold (**), Heading (###), Bullet Points (*), dan menghapus (---)
 */
private fun parseAdvancedMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")

        lines.forEachIndexed { index, line ->
            var processedLine = line.trimEnd()

            // 1. Abaikan / Hapus garis horizontal (---)
            if (processedLine.trim() == "---") {
                return@forEachIndexed
            }

            // 2. Deteksi Header (### atau ####)
            var isHeader = false
            if (processedLine.startsWith("### ")) {
                processedLine = processedLine.removePrefix("### ")
                isHeader = true
            } else if (processedLine.startsWith("#### ")) {
                processedLine = processedLine.removePrefix("#### ")
                isHeader = true
            } else if (processedLine.startsWith("## ")) {
                processedLine = processedLine.removePrefix("## ")
                isHeader = true
            }

            // 3. Deteksi Bullet Points (* atau -) dan ganti jadi tanda titik bulat rapi
            if (processedLine.startsWith("* ") || processedLine.startsWith("- ")) {
                processedLine = "• " + processedLine.drop(2)
            }

            // 4. Masukkan teks ke UI dengan format
            if (isHeader) {
                // Beri jarak ekstra (enter tambahan) sebelum header agar tidak menempel
                if (index > 0) append("\n")

                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PgPrimary)) {
                    appendWithBoldFormatting(processedLine)
                }
            } else {
                appendWithBoldFormatting(processedLine)
            }

            // Tambahkan baris baru (enter) kecuali untuk baris terakhir
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

/**
 * Fungsi kecil untuk mengeksekusi format **teks tebal** di dalam satu baris kalimat
 */
private fun AnnotatedString.Builder.appendWithBoldFormatting(text: String) {
    var currentIndex = 0
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    val matches = boldRegex.findAll(text)

    for (match in matches) {
        append(text.substring(currentIndex, match.range.first))
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }
        currentIndex = match.range.last + 1
    }
    append(text.substring(currentIndex))
}