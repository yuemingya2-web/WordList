package com.vocabmaster.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Home("home", "学习", Icons.Filled.PlayArrow),
    Library("library", "词库", Icons.Filled.MenuBook),
    Settings("settings", "设置", Icons.Filled.Settings);

    companion object {
        val start: Destination = Home

        /** 学习屏路由：不参与底部导航，从首页按钮进入。 */
        const val LEARN_ROUTE = "learn"
    }
}
