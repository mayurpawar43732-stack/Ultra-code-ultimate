package com.ultracode.ultimate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
@Composable fun ProviderSettingsScreen(){val providers=ProviderApi.names();var selected by remember{mutableStateOf(ProviderApi.active())};var key by remember(selected){mutableStateOf(SecureKeys.get(ProviderApi.keyName(selected)))};var show by remember{mutableStateOf(false)};var status by remember{mutableStateOf("Keys are encrypted on this device using Android Keystore.")};var busy by remember{mutableStateOf(false)};var account by remember{mutableStateOf(SecureKeys.get("cloudflare_account"))}
Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("AI PROVIDER SETTINGS",style=MaterialTheme.typography.headlineSmall);Text("Select a provider, paste your key, and save. Keys are not hard-coded into the APK.")
Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){providers.forEach{p->FilterChip(selected==p,onClick={selected=p;ProviderApi.select(p);key=SecureKeys.get(ProviderApi.keyName(p));status="$p selected"},label={Text(p)})}}
OutlinedTextField(key,{key=it},modifier=Modifier.fillMaxWidth(),label={Text("$selected API key")},leadingIcon={Icon(Icons.Default.Key,null)},trailingIcon={IconButton(onClick={show=!show}){Icon(if(show)Icons.Default.VisibilityOff else Icons.Default.Visibility,"Show/hide key")}},visualTransformation=if(show)VisualTransformation.None else PasswordVisualTransformation(),singleLine=true)
Button(onClick={SecureKeys.save(ProviderApi.keyName(selected),key);ProviderApi.select(selected);status=if(key.isBlank())"Key cleared" else "$selected key saved securely"},modifier=Modifier.fillMaxWidth()){Text("Save API Key")}
Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={SecureKeys.delete(ProviderApi.keyName(selected));key="";status="$selected key deleted"},modifier=Modifier.weight(1f)){Text("Delete")};OutlinedButton(enabled=!busy,onClick={busy=true;status="Testing $selected…";Thread{val result=ProviderApi.test(selected);android.os.Handler(android.os.Looper.getMainLooper()).post{busy=false;status=if(result.first)"Connection successful: "+result.second else "Connection failed: "+result.second}}.start()},modifier=Modifier.weight(1f)){Text(if(busy)"Testing…" else "Test Connection")}}
HorizontalDivider();Text("Cloudflare setup",style=MaterialTheme.typography.titleMedium);OutlinedTextField(account,{account=it},modifier=Modifier.fillMaxWidth(),label={Text("Cloudflare Account ID")},singleLine=true);Button(onClick={SecureKeys.save("cloudflare_account",account);status="Cloudflare Account ID saved securely"}){Text("Save Account ID")};Text(status,color=MaterialTheme.colorScheme.primary);Text("Use keys from official provider dashboards. Never share or commit API keys.",style=MaterialTheme.typography.bodySmall)}}
