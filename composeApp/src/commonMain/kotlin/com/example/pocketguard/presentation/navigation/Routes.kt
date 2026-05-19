package com.example.pocketguard.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data class AddTransaction(
        val transactionId: Long? = null,
        val transactionType: String? = null,  // "INCOME" atau "EXPENSE"
        val transactionCategory: String? = null  // nama kategori
    ) : Route

    @Serializable
    data class TransactionDetail(val transactionId: Long) : Route

    @Serializable
    data class AIAssistant(val initialText: String? = null) : Route

    @Serializable
    data object Settings : Route
}

interface NavigationActions {
    fun navigateToHome()
    fun navigateToAddTransaction(
        transactionId: Long? = null,
        transactionType: String? = null,
        transactionCategory: String? = null
    )
    fun navigateToTransactionDetail(transactionId: Long)
    fun navigateToAIAssistant(initialText: String? = null)
    fun navigateBack()
    fun navigateToSettings()
}