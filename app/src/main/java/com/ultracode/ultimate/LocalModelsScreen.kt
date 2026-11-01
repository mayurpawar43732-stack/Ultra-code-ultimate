package com.ultracode.ultimate
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
private data class Model(val name:String,val size:String,val url:String,val note:String)
@Composable fun LocalModelsScreen(){val ctx=androidx.compose.ui.platform.LocalContext.current;val models=listOf(Model("Qwen2.5-Coder 0.5B Q4_K_M","~400 MB","https://huggingface.co/Qwen/Qwen2.5-Coder-0.5B-Instruct-GGUF/resolve/main/qwen2.5-coder-0.5b-instruct-q4_k_m.gguf","Smaller model; speed depends on device."),Model("Qwen2.5-Coder 1.5B Q4_K_M","~1.1 GB","https://huggingface.co/Qwen/Qwen2.5-Coder-1.5B-Instruct-GGUF/resolve/main/qwen2.5-coder-1.5b-instruct-q4_k_m.gguf","Requires more storage and memory."));var status by remember{mutableStateOf("Inference runtime not bundled: model downloads will not run yet.")}
Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){Text("LOCAL AI MODEL MANAGER",style=MaterialTheme.typography.headlineSmall);Text("Offline model support");Text("This release can queue model downloads, but a compatible inference engine must be integrated before local code generation works.");Surface(color=MaterialTheme.colorScheme.surface,shape=MaterialTheme.shapes.large){Column(Modifier.padding(14.dp)){Text("Runtime status: Not installed",style=MaterialTheme.typography.titleMedium);Text("Model files alone cannot generate answers.")}}
models.forEach{m->Surface(color=MaterialTheme.colorScheme.surface,shape=MaterialTheme.shapes.large){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(m.name,style=MaterialTheme.typography.titleMedium);Text("Estimated size: "+m.size);Text(m.note);Button(onClick={try{val file=m.url.substringAfterLast("/").substringBefore("?");val req=DownloadManager.Request(Uri.parse(m.url)).setTitle("Ultra Code: "+m.name).setDescription("Downloading local model").setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalFilesDir(ctx,Environment.DIRECTORY_DOWNLOADS,file);(ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req);status="Download queued. Local inference remains unavailable until runtime integration."}catch(e:Exception){status="Download error: "+(e.message?:"unknown error")}},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Download,null);Spacer(Modifier.width(8.dp));Text("Download Model")}}}}
Text(status,color=MaterialTheme.colorScheme.primary)}}