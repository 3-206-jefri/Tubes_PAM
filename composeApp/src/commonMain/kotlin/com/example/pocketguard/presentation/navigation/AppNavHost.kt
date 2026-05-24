package com.example.pocketguard.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add // <-- Diganti menjadi Add biasa
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.pocketguard.presentation.screens.add_transaction.AddTransactionScreen
import com.example.pocketguard.presentation.screens.ai.AIAssistantScreen
import com.example.pocketguard.presentation.screens.detail.TransactionDetailScreen
import com.example.pocketguard.presentation.screens.home.HomeScreen
import com.example.pocketguard.presentation.screens.settings.SettingsScreen
import com.example.pocketguard.presentation.screens.analytics.AnalyticsScreen

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
            icon = Icons.Default.Add, // ✅ Menggunakan ikon Add biasa agar rapi di dalam lingkaran
            isSelected = { it?.hasRoute<Route.AddTransaction>() == true },
            onClick = { navigationActions.navigateToAddTransaction() }
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isAddButton = item.label == "Add"
                        val isSelected = item.isSelected(currentDestination)
                        val itemLabel: @Composable (() -> Unit) =  {
                             Text(text = item.label, fontSize = 11.sp) }


                        NavigationBarItem(
                            selected = isSelected,
                            onClick = item.onClick,
                            label = itemLabel,
                            icon = {
                                if (isAddButton) {
                                    // ✅ Styling khusus tombol Add (Lingkaran Hijau Lebih Besar)
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = Color(0xFF2E7D32), // Hijau khas PocketGuard
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                } else {
                                    // Styling standar untuk menu lainnya
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                }
                            },
                            // ✅ Menghilangkan efek indikator abu-abu bawaan Material 3 khusus di tombol Add
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = if (isAddButton) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            alwaysShowLabel = true
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