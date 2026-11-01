package com.ultracode.ultimate

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class ChatAttachment(val name: String, val mime: String, val content: String)
private data class ChatLine(val text: String, val fromUser: Boolean)

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf<List<ChatAttachment>>(emptyList()) }
    var lines by remember { mutableStateOf(listOf(ChatLine("Hi! I'm Ultra Code Ultimate. Tell me what you want to build, fix, or understand. You can also use + to attach files.", false))) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val added = uris.map { uri ->
            val name = android.provider.OpenableColumns.DISPLAY_NAME
            val displayName = context.contentResolver.query(uri, arrayOf(name), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            } ?: uri.lastPathSegment ?: "Attachment"
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val textLike = mime.startsWith("text/") || mime.contains("json") || mime.contains("xml") ||
                displayName.endsWith(".kt", true) || displayName.endsWith(".java", true) ||
                displayName.endsWith(".py", true) || displayName.endsWith(".js", true) ||
                displayName.endsWith(".ts", true) || displayName.endsWith(".html", true) ||
                displayName.endsWith(".css", true) || displayName.endsWith(".md", true) ||
                displayName.endsWith(".gradle", true) || displayName.endsWith(".txt", true)
            val content = if (textLike) {
                try { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText().take(30000) }.orEmpty() }
                catch (e: Exception) { "[Could not read file: ${e.message}]" }
            } else "[Attached file type: $mime. This version can show the attachment, but image/PDF binary understanding is not integrated yet.]"
            ChatAttachment(displayName, mime, content)
        }
        attachments = attachments + added
        status = "${added.size} attachment(s) added"
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF080D18)).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("AI CHAT", color = Color(0xFF55CFFF), style = MaterialTheme.typography.labelLarge)
                Text("Ask anything. Build with conversation.", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            Text(status, color = Color(0xFF91A8C5), style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(lines) { _, line ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (line.fromUser) Arrangement.End else Arrangement.Start) {
                    Surface(
                        color = if (line.fromUser) Color(0xFF123B59) else Color(0xFF151F31),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.widthIn(max = 330.dp)
                    ) { Text(line.text, Modifier.padding(13.dp), color = Color.White, style = MaterialTheme.typography.bodyMedium) }
                }
            }
            if (busy) item { Text("Ultra Code is thinking…", color = Color(0xFF55CFFF), modifier = Modifier.padding(8.dp)) }
        }
        if (attachments.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                attachments.forEach { a ->
                    AssistChip(onClick = { attachments = attachments - a }, label = { Text("📎 ${a.name}  ×", maxLines = 1) })
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        Surface(color = Color(0xFF141F31), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = prompt, onValueChange = { prompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Message Ultra Code…") },
                    minLines = 2, maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
                    )
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { picker.launch(arrayOf("*/*")) }, enabled = !busy) {
                        Icon(Icons.Default.Add, contentDescription = "Add files", tint = Color(0xFF55CFFF))
                    }
                    Text("Add files", color = Color(0xFF91A8C5), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.weight(1f))
                    FilledIconButton(
                        onClick = {
                            val message = prompt.trim()
                            if (message.isNotEmpty() || attachments.isNotEmpty()) {
                                val attached = attachments.toList()
                                val request = buildString {
                                    append(message.ifBlank { "Please inspect the attached files and help me with them." })
                                    attached.forEach { append("\n\nATTACHMENT: ${it.name} (${it.mime})\n"); append(it.content) }
                                }
                                lines = lines + ChatLine(buildString {
                                    append(message.ifBlank { "Please inspect the attached files." })
                                    if (attached.isNotEmpty()) append("\n\n📎 " + attached.joinToString { it.name })
                                }, true)
                                prompt = ""; attachments = emptyList(); busy = true; status = "Sending"
                                scope.launch {
                                    val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { ProviderApi.generate(request) }
                                    lines = lines + ChatLine(if (result.first.startsWith("Connected")) result.second else "${result.first}: ${result.second}", false)
                                    busy = false; status = "Ready"
                                }
                            }
                        },
                        enabled = !busy && (prompt.isNotBlank() || attachments.isNotEmpty()),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF087EB5), contentColor = Color.White)
                    ) { Icon(Icons.Default.Send, contentDescription = "Send message") }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Text/code files are included in the prompt. Image and PDF content support is not yet implemented.", color = Color(0xFF7890AE), style = MaterialTheme.typography.labelSmall)
    }
}
