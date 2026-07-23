package com.vocabmaster.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vocabmaster.app.ui.home.HomeScreen
import com.vocabmaster.app.ui.learn.LearnScreen
import com.vocabmaster.app.ui.library.LibraryScreen
import com.vocabmaster.app.ui.settings.SettingsScreen

@Composable
fun VocabNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // 学习进行中时隐藏底部导航，沉浸式答题
            if (currentRoute != Destination.LEARN_ROUTE && currentRoute != null) {
                NavigationBar {
                    Destination.entries.forEach { dest ->
                        val selected = backStackEntry?.destination?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.start.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.Home.route) {
                HomeScreen(
                    onStartLearning = {
                        navController.navigate(Destination.LEARN_ROUTE)
                    }
                )
            }
            composable(Destination.Library.route) {
                LibraryScreen()
            }
            composable(Destination.Settings.route) {
                SettingsScreen()
            }
            composable(Destination.LEARN_ROUTE) {
                LearnScreen(
                    onExit = { navController.popBackStack() }
                )
            }
        }
    }
}
