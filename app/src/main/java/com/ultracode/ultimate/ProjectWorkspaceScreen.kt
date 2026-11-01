package com.ultracode.ultimate

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class WorkspaceFile(val file: DocumentFile, val path: String)

@Composable
fun ProjectWorkspaceScreen() {
    val context = LocalContext.current
    var rootUri by remember { mutableStateOf<Uri?>(null) }
    var root by remember { mutableStateOf<DocumentFile?>(null) }
    var files by remember { mutableStateOf<List<WorkspaceFile>>(emptyList()) }
    var selected by remember { mutableStateOf<WorkspaceFile?>(null) }
    var source by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf("") }
    var task by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Choose a project folder to begin.") }
    var busy by remember { mutableStateOf(false) }

    fun scan(folder: DocumentFile, prefix: String = ""): List<WorkspaceFile> {
        val found = mutableListOf<WorkspaceFile>()
        folder.listFiles().sortedBy { it.name?.lowercase().orEmpty() }.forEach { child ->
            val name = child.name ?: return@forEach
            val path = if (prefix.isEmpty()) name else "$prefix/$name"
            if (child.isDirectory) {
                if (path.split("/").lastOrNull() !in setOf(".git", ".gradle", "build", "node_modules", ".idea")) {
                    found.addAll(scan(child, path))
                }
            } else if (name.matches(Regex(".*\\.(kt|kts|java|xml|json|js|ts|tsx|jsx|html|css|py|md|txt|gradle|properties|yaml|yml)$", RegexOption.IGNORE_CASE))) {
                found.add(WorkspaceFile(child, path))
            }
        }
        return found
    }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } catch (_: Exception) { }
            rootUri = uri
            SecureKeys.save("workspace_tree_uri", uri.toString())
            root = DocumentFile.fromTreeUri(context, uri)
            files = root?.let { scan(it) }.orEmpty()
            selected = null
            source = ""
            draft = ""
            status = "Project loaded • ${files.size} supported files found."
        }
    }

    LaunchedEffect(Unit) {
        val saved = SecureKeys.get("workspace_tree_uri")
        if (saved.isNotBlank()) {
            try {
                val uri = Uri.parse(saved)
                rootUri = uri
                root = DocumentFile.fromTreeUri(context, uri)
                files = root?.let { scan(it) }.orEmpty()
                status = "Project restored • ${files.size} supported files found."
            } catch (_: Exception) { status = "Reopen your project folder to continue." }
        }
    }

    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("PROJECT WORKSPACE", color = Color(0xFF48D9FF), style = MaterialTheme.typography.labelLarge)
                Text(root?.name ?: "No project opened", style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = { folderPicker.launch(rootUri) }) {
                Icon(Icons.Default.FolderOpen, null)
                Spacer(Modifier.width(6.dp))
                Text("Open")
            }
        }
        Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        if (root == null) {
            Text("Open a folder using Android's folder picker. Ultra Code can then read and update supported source files after you review each change.")
        } else {
            Text("FILES • ${files.size}", style = MaterialTheme.typography.labelLarge)
            Column(Modifier.weight(0.8f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                files.forEach { item ->
                    Surface(
                        color = if (selected?.path == item.path) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
                            selected = item
                            source = try {
                                context.contentResolver.openInputStream(item.file.uri)?.bufferedReader()?.use { it.readText().take(50000) }.orEmpty()
                            } catch (e: Exception) { "Unable to read file: ${e.message}" }
                            draft = ""
                            status = "Opened ${item.path}"
                        }
                    ) { Text(item.path, Modifier.padding(9.dp), style = MaterialTheme.typography.bodySmall) }
                }
            }
            HorizontalDivider()
            Text(selected?.path ?: "Select a file", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = source, onValueChange = { source = it }, modifier = Modifier.fillMaxWidth().weight(1.2f),
                label = { Text("Selected file • editable") }, minLines = 5,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
            OutlinedTextField(
                value = task, onValueChange = { task = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text("What should AI change in this file?") }, minLines = 2
            )
            Button(
                enabled = selected != null && task.isNotBlank() && !busy,
                onClick = {
                    val chosen = selected ?: return@Button
                    val prompt = """You are an expert software engineer. Modify exactly the one file provided. Follow the user's task. Preserve unrelated behavior. Return ONLY the complete replacement file contents, with no Markdown fences or explanation.
FILE: ${chosen.path}
USER TASK: $task
CURRENT FILE CONTENT:
$source"""
                    busy = true
                    status = "AI is preparing a replacement. Review it before saving."
                    Thread {
                        val result = ProviderApi.generate(prompt)
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            busy = false
                            if (result.first.startsWith("Connected")) {
                                draft = result.second.trim().removePrefix("```").removeSuffix("```").trim()
                                status = "Draft ready • compare it carefully before applying."
                            } else status = result.first + " • " + result.second
                        }
                    }.start()
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(Modifier.width(8.dp))
                Text(if (busy) "Working…" else "Ask AI to edit selected file")
            }
            if (draft.isNotBlank()) {
                Text("AI REPLACEMENT PREVIEW", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(draft, { draft = it }, Modifier.fillMaxWidth().weight(1f), minLines = 4,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                Button(onClick = {
                    val chosen = selected
                    if (chosen != null) {
                        try {
                            context.contentResolver.openOutputStream(chosen.file.uri, "wt")?.bufferedWriter()?.use { it.write(draft) }
                            source = draft
                            draft = ""
                            task = ""
                            status = "Saved ${chosen.path}"
                        } catch (e: Exception) { status = "Save failed: ${e.message}" }
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Apply reviewed change to file")
                }
            }
        }
        Text("Safety: AI edits only the selected file. Review the preview before applying. This Android build does not run shell commands or compile projects on-device.", style = MaterialTheme.typography.bodySmall)
    }
}
