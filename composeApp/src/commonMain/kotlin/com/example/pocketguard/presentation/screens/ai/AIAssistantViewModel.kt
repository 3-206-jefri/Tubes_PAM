package com.example.pocketguard.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.domain.repository.AIRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Deklarasi model pesan dipindahkan ke sini
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

data class AIAssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AIAssistantViewModel(
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()

    fun sendMessage(prompt: String) {
        if (prompt.isBlank()) return

        // 1. Masukkan pesan dari User ke layar dan aktifkan efek Loading
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(ChatMessage(text = prompt, isUser = true))

        _uiState.update { it.copy(messages = currentMessages, isLoading = true, error = null) }

        // 2. Kirim prompt ke server Gemini AI menggunakan fungsi chat() dari AIRepository
        viewModelScope.launch {
            val result = aiRepository.chat(prompt)

            result.onSuccess { aiResponse ->
                // 3. Jika berhasil, masukkan balasan AI ke layar
                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages.add(ChatMessage(text = aiResponse, isUser = false))

                _uiState.update { it.copy(messages = updatedMessages, isLoading = false) }
            }.onFailure { err ->
                // 4. Jika gagal, tampilkan pesan error sebagai pesan AI
                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages.add(ChatMessage(text = "Maaf, terjadi kesalahan: ${err.message}", isUser = false))

                _uiState.update { it.copy(messages = updatedMessages, isLoading = false, error = err.message) }
            }
        }
    }
}