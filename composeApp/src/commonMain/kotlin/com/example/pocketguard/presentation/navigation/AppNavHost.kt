package com.example.pocketguard.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.setValue
import com.example.pocketguard.presentation.screens.add_transaction.AddTransactionScreen
import com.example.pocketguard.presentation.screens.ai.AIAssistantScreen
import com.example.pocketguard.presentation.screens.detail.TransactionDetailScreen
import com.example.pocketguard.presentation.screens.home.HomeScreen
import com.example.pocketguard.presentation.screens.settings.SettingsScreen
import com.example.pocketguard.presentation.screens.analytics.AnalyticsScreen

// 1. Data class diubah agar langsung menampung aksi (onClick) dan validasi (isSelected)
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val isSelected: (NavDestination?) -> Boolean,
    val onClick: () -> Unit
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navigationActions = createNavigationActions(navController)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 2. Deklarasi Menu Bawah yang Bebas Crash!
    val bottomNavItems = listOf(
        BottomNavItem(
            label = "Home",
            icon = Icons.Default.Home,
            isSelected = { it?.hasRoute<Route.Home>() == true },
            onClick = { navigationActions.navigateToHome() }
        ),
        BottomNavItem(
            label = "AI",
            icon = Icons.Default.AutoAwesome,
            isSelected = { it?.hasRoute<Route.AIAssistant>() == true },
            onClick = { navigationActions.navigateToAIAssistant() }
        ),
        BottomNavItem(
            label = "Add",
            icon = Icons.Default.AddCircle,
            isSelected = { it?.hasRoute<Route.AddTransaction>() == true },
            onClick = { navigationActions.navigateToAddTransaction() } // Mengandalkan default parameter null
        ),
        BottomNavItem(
            label = "Grafik",
            icon = Icons.Default.Analytics,
            isSelected = { it?.hasRoute<Route.Analytics>() == true },
            onClick = { navigationActions.navigateToAnalytics() }
        ),
        BottomNavItem(
            label = "Settings",
            icon = Icons.Default.Settings,
            isSelected = { it?.hasRoute<Route.Settings>() == true },
            onClick = { navigationActions.navigateToSettings() }
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            val showBottomBar = bottomNavItems.any { it.isSelected(currentDestination) }

            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .shadow(
                            elevation = 18.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Color(0xFF81C784).copy(alpha = 0.18f)
                        )
                        .clip(RoundedCornerShape(28.dp)),

                    containerColor = Color(0xFFF4F8F4),
                    tonalElevation = 0.dp
                ) {

                    bottomNavItems.forEach { item ->

                        NavigationBarItem(
                            selected = item.isSelected(currentDestination),

                            onClick = item.onClick,

                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            ),

                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight =
                                        if (item.isSelected(currentDestination))
                                            FontWeight.Bold
                                        else
                                            FontWeight.Medium,

                                    color =
                                        if (item.isSelected(currentDestination))
                                            Color(0xFF1B5E20)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },

                            icon = {

                                val selected = item.isSelected(currentDestination)

                                val isAddButton = item.label == "Add"

                                val iconSize by animateDpAsState(
                                    targetValue =
                                        if (selected) 28.dp
                                        else 22.dp,
                                    label = ""
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isAddButton ->
                                                    Color(0xFF2E7D32)

                                                selected ->
                                                    Color(0xFF81C784).copy(alpha = 0.16f)

                                                else ->
                                                    Color.Transparent
                                            }
                                        )
                                        .padding(
                                            if (isAddButton) 10.dp
                                            else 8.dp
                                        )
                                ) {

                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,

                                        modifier = Modifier.size(iconSize),

                                        tint =
                                            when {
                                                isAddButton ->
                                                    Color.White

                                                selected ->
                                                    Color(0xFF1B5E20)

                                                else ->
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Route.Home> {
                HomeScreen(
                    onNavigateToAdd = { type, category ->
                        navigationActions.navigateToAddTransaction(
                            transactionType = type,
                            transactionCategory = category
                        )
                    },
                    onNavigateToDetail = { id -> navigationActions.navigateToTransactionDetail(id) }
                )
            }

            composable<Route.AddTransaction> { backStackEntry ->
                val route: Route.AddTransaction = backStackEntry.toRoute()
                AddTransactionScreen(
                    transactionId = route.transactionId,
                    initialType = route.transactionType,
                    initialCategory = route.transactionCategory,
                    onNavigateBack = { navigationActions.navigateBack() }
                )
            }

            composable<Route.TransactionDetail> { backStackEntry ->
                val route: Route.TransactionDetail = backStackEntry.toRoute()
                TransactionDetailScreen(
                    transactionId = route.transactionId,
                    onNavigateBack = { navigationActions.navigateBack() },
                    onNavigateToEdit = { id -> navigationActions.navigateToAddTransaction(transactionId = id) }
                )
            }

            composable<Route.AIAssistant> { backStackEntry ->
                val route: Route.AIAssistant = backStackEntry.toRoute()
                AIAssistantScreen(
                    initialText = route.initialText,
                    onNavigateBack = { navigationActions.navigateBack() }
                )
            }

            composable<Route.Analytics> {
                AnalyticsScreen(
                    onNavigateBack = { navigationActions.navigateBack() }
                )
            }

            composable<Route.Settings> {
                SettingsScreen(
                    onNavigateBack = { navigationActions.navigateBack() }
                )
            }
        }
    }
}

private fun createNavigationActions(navController: NavHostController): NavigationActions {
    return object : NavigationActions {
        override fun navigateToHome() {
            navController.navigate(Route.Home) {
                popUpTo(Route.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        // Menyocokkan dengan 3 argumen di Routes.kt kamu
        override fun navigateToAddTransaction(
            transactionId: Long?,
            transactionType: String?,
            transactionCategory: String?
        ) {
            navController.navigate(
                Route.AddTransaction(
                    transactionId = transactionId,
                    transactionType = transactionType,
                    transactionCategory = transactionCategory
                )
            ) {
                popUpTo(Route.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        override fun navigateToTransactionDetail(transactionId: Long) {
            navController.navigate(Route.TransactionDetail(transactionId))
        }

        override fun navigateToAIAssistant(initialText: String?) {
            navController.navigate(Route.AIAssistant(initialText)) {
                popUpTo(Route.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        override fun navigateToAnalytics() {
            navController.navigate(Route.Analytics) {
                popUpTo(Route.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        override fun navigateBack() {
            navController.popBackStack()
        }

        override fun navigateToSettings() {
            navController.navigate(Route.Settings) {
                popUpTo(Route.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
}