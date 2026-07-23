package com.vocabmaster.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vocabmaster.app.ui.navigation.VocabNavHost
import com.vocabmaster.app.ui.theme.VocabMasterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VocabMasterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VocabNavHost()
                }
            }
        }
    }
}
