package com.ultracode.ultimate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Surface(color = Color(0xFF06152A), shape = MaterialTheme.shapes.medium) {
                            Text("UC", Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = cyan, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("ULTRA CODE", fontWeight = androidx.compose.ui.text.font.FontWeight.Black, color = Color.White)
                            Text("AI CODING STUDIO", style = MaterialTheme.typography.labelSmall, color = cyan)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = panel)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = panel) {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.Chat, null) }, label = { Text("Chat") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.Terminal, null) }, label = { Text("Workspace") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.Memory, null) }, label = { Text("Local AI") })
                NavigationBarItem(selected = tab == 3, onClick = { tab = 3 }, icon = { Icon(Icons.Default.Key, null) }, label = { Text("API Keys") })
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                0 -> ChatScreen()
                1 -> ProjectWorkspaceScreen()
                2 -> LocalModelsScreen()
                else -> ProviderSettingsScreen()
            }
        }
    }
}
