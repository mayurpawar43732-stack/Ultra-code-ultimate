package com.ultracode.ultimate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val bg = Color(0xFF080D18)
private val panel = Color(0xFF111A2B)
private val cyan = Color(0xFF48D9FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SecureKeys.init(applicationContext)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = cyan, background = bg, surface = panel)) {
                UltraCodeApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UltraCodeApp() {
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ULTRA CODE ULTIMATE", fontWeight = androidx.compose.ui.text.font.FontWeight.Black, color = Color.White)
                        Text("AI CODING STUDIO • ANDROID", style = MaterialTheme.typography.labelSmall, color = cyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = panel)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = panel) {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.Terminal, null) }, label = { Text("Workspace") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.Memory, null) }, label = { Text("Local AI") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.Key, null) }, label = { Text("API Keys") })
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                0 -> ProjectWorkspaceScreen()
                1 -> LocalModelsScreen()
                else -> ProviderSettingsScreen()
            }
        }
    }
}
