package com.ultracode.ultimate
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
private val bg=Color(0xFF080D18);private val panel=Color(0xFF111A2B);private val cyan=Color(0xFF48D9FF)
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);SecureKeys.init(applicationContext);setContent{MaterialTheme(colorScheme=darkColorScheme(primary=cyan,background=bg,surface=panel)){App()}}}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun App(){var tab by remember{mutableIntStateOf(0)};Scaffold(containerColor=bg,topBar={androidx.compose.material3.TopAppBar(title={Column{Text("ULTRA CODE ULTIMATE",fontWeight=FontWeight.Black,color=Color.White,letterSpacing=1.sp);Text("ANDROID CODING STUDIO",fontSize=10.sp,color=cyan)}},actions={IconButton(onClick={tab=2}){Icon(Icons.Default.Settings,"Settings",tint=cyan)}},colors=TopAppBarDefaults.topAppBarColors(containerColor=panel))},bottomBar={NavigationBar(containerColor=panel){NavigationBarItem(selected=tab==0,onClick={tab=0},icon={Icon(Icons.Default.Terminal,null)},label={Text("Workspace")});NavigationBarItem(selected=tab==1,onClick={tab=1},icon={Icon(Icons.Default.Memory,null)},label={Text("Local AI")});NavigationBarItem(selected=tab==2,onClick={tab=2},icon={Icon(Icons.Default.Key,null)},label={Text("API Keys")})}}){p->Box(Modifier.padding(p).fillMaxSize()){when(tab){0->Workspace({tab=2},{tab=1});1->LocalModelsScreen();else->ProviderSettingsScreen()}}}}
@Composable private fun Workspace(settings:()->Unit,local:()->Unit){var prompt by remember{mutableStateOf("")};var output by remember{mutableStateOf("Generated code will appear here.")};var status by remember{mutableStateOf("Ready • select a provider in API Keys")};Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){Text("BUILD SOMETHING GREAT",color=cyan,style=MaterialTheme.typography.labelLarge);Text("From idea to code.",style=MaterialTheme.typography.headlineMedium,color=Color.White);Text("Describe a website, app, API or coding problem. Choose a configured provider.");OutlinedTextField(prompt,{prompt=it},label={Text("Describe your project")},minLines=4,modifier=Modifier.fillMaxWidth());Button(onClick={if(prompt.isBlank()){status="Enter a project description first"}else{status="Requesting code…";Thread{val result=ProviderApi.generate(prompt);android.os.Handler(android.os.Looper.getMainLooper()).post{status=result.first;output=result.second}}.start()}},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.AutoAwesome,null);Spacer(Modifier.width(8.dp));Text("Generate Code")};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick=settings,modifier=Modifier.weight(1f)){Text("API Settings")};OutlinedButton(onClick=local,modifier=Modifier.weight(1f)){Text("Local AI")}};Text(status,color=cyan);Surface(color=panel,shape=MaterialTheme.shapes.large){Column(Modifier.padding(14.dp)){Text("CODE OUTPUT",color=cyan);Spacer(Modifier.height(8.dp));Text(output,color=Color.White)}};Text("This release returns generated code as text. Running projects and building APK/EXE files inside this app are not implemented yet.",color=Color.LightGray,style=MaterialTheme.typography.bodySmall)}}
