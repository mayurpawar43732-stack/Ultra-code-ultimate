package com.ultracode.ultimate
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
object SecureKeys{private const val PREF="ultra_code_keys";private const val ALIAS="ultra_code_aes";private lateinit var ctx:Context
fun init(c:Context){ctx=c.applicationContext}
private fun key():SecretKey{val s=KeyStore.getInstance("AndroidKeyStore").apply{load(null)};(s.getKey(ALIAS,null) as? SecretKey)?.let{return it};val g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");g.init(KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());return g.generateKey()}
fun get(n:String):String{val v=ctx.getSharedPreferences(PREF,0).getString(n,null)?:return "";return try{val b=Base64.decode(v,Base64.NO_WRAP);val z=ByteBuffer.wrap(b,0,4).int;val iv=b.copyOfRange(4,4+z);val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,iv));String(c.doFinal(b.copyOfRange(4+z,b.size)),Charsets.UTF_8)}catch(_:Exception){""}}
fun save(n:String,v:String){if(v.isBlank()){delete(n);return};val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());val iv=c.iv;val e=c.doFinal(v.trim().toByteArray());val b=ByteBuffer.allocate(4+iv.size+e.size).putInt(iv.size).put(iv).put(e).array();ctx.getSharedPreferences(PREF,0).edit().putString(n,Base64.encodeToString(b,Base64.NO_WRAP)).apply()}
fun delete(n:String){ctx.getSharedPreferences(PREF,0).edit().remove(n).apply()}}
object ProviderApi{
fun names()=listOf("Gemini","Groq","OpenRouter","Cloudflare")
fun keyName(p:String)=p.lowercase()
fun active()=SecureKeys.get("active_provider").ifBlank{"Gemini"}
fun select(p:String)=SecureKeys.save("active_provider",p)
fun generate(prompt:String):Pair<String,String>{
    val selected=active()
    val order=listOf(selected)+names().filter{it!=selected}
    val failures=mutableListOf<String>()
    for(p in order){
        val k=SecureKeys.get(keyName(p))
        if(k.isBlank()) continue
        try {
            val result=when(p){
                "Gemini"->gemini(k,prompt)
                "Groq"->chat("https://api.groq.com/openai/v1/chat/completions",k,"llama-3.3-70b-versatile",prompt)
                "OpenRouter"->chat("https://openrouter.ai/api/v1/chat/completions",k,"openrouter/auto",prompt)
                else->cloudflare(prompt)
            }
            if(p!=selected) return ("Fallback • "+result.first) to result.second
            return result
        } catch(e:Exception) {
            failures.add("$p: ${e.message?: "Request failed"}")
        }
    }
    val detail=if(failures.isEmpty()) "No provider API key is saved. Open API Keys and add a Gemini, Groq, OpenRouter, or Cloudflare key."
        else failures.joinToString("\\n").take(900)
    return "All AI providers failed" to detail
}
fun test(p:String):Pair<Boolean,String>{if(SecureKeys.get(keyName(p)).isBlank())return false to "No saved key for $p";return try{val r=when(p){"Gemini"->gemini(SecureKeys.get(keyName(p)),"Reply exactly: OK");"Groq"->chat("https://api.groq.com/openai/v1/chat/completions",SecureKeys.get(keyName(p)),"llama-3.3-70b-versatile","Reply exactly: OK");"OpenRouter"->chat("https://openrouter.ai/api/v1/chat/completions",SecureKeys.get(keyName(p)),"openrouter/auto","Reply exactly: OK");else->cloudflare("Reply exactly: OK")};true to r.second.take(300)}catch(e:Exception){false to (e.message?:"Connection failed")}}
private fun post(url:String,body:JSONObject,headers:Map<String,String> = emptyMap()):JSONObject{val c=URL(url).openConnection() as HttpURLConnection;c.requestMethod="POST";c.connectTimeout=15000;c.readTimeout=45000;c.setRequestProperty("Content-Type","application/json");headers.forEach{(a,b)->c.setRequestProperty(a,b)};c.doOutput=true;c.outputStream.use{it.write(body.toString().toByteArray())};val code=c.responseCode;val s=if(code in 200..299)c.inputStream else c.errorStream;val text=s?.bufferedReader()?.use{it.readText()}?:"";c.disconnect();if(code !in 200..299)throw IllegalStateException("HTTP "+code+": "+text.take(250));return JSONObject(text)}
private fun chat(url:String,key:String,model:String,prompt:String):Pair<String,String>{val msgs=JSONArray().put(JSONObject().put("role","system").put("content","You are Ultra Code Ultimate, a coding assistant. Return useful code and concise explanations.")).put(JSONObject().put("role","user").put("content",prompt));val r=post(url,JSONObject().put("model",model).put("messages",msgs),mapOf("Authorization" to "Bearer "+key));return "Connected • "+model to r.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content","No text returned")}
private fun gemini(key:String,prompt:String):Pair<String,String>{
    val model="gemini-3.8-flash"
    val u="https://generativelanguage.googleapis.com/v1beta/models/"+model+":generateContent?key="+URLEncoder.encode(key,"UTF-8")
    val b=JSONObject()
        .put("systemInstruction",JSONObject().put("parts",JSONArray().put(JSONObject().put("text","You are Ultra Code, a helpful coding assistant. Return practical code and concise explanations."))))
        .put("contents",JSONArray().put(JSONObject().put("role","user").put("parts",JSONArray().put(JSONObject().put("text",prompt)))))
        .put("generationConfig",JSONObject().put("maxOutputTokens",8192))
    val r=post(u,b)
    val direct=r.optString("output_text","")
    if(direct.isNotBlank()) return "Connected • Gemini 3.8 Flash" to direct
    val texts=mutableListOf<String>()
    val candidates=r.optJSONArray("candidates")
    if(candidates!=null){
        for(i in 0 until candidates.length()){
            val candidate=candidates.optJSONObject(i)?:continue
            val content=candidate.optJSONObject("content")?:continue
            val parts=content.optJSONArray("parts")?:continue
            for(j in 0 until parts.length()){
                val part=parts.optJSONObject(j)?:continue
                val t=part.optString("text","")
                if(t.isNotBlank()) texts.add(t)
            }
        }
    }
    val output=r.optJSONArray("output")?:r.optJSONArray("steps")
    if(output!=null){
        for(i in 0 until output.length()){
            val item=output.optJSONObject(i)?:continue
            val content=item.optJSONArray("content")
            if(content!=null) for(j in 0 until content.length()){
                val part=content.optJSONObject(j)?:continue
                val t=part.optString("text","")
                if(t.isNotBlank()) texts.add(t)
            }
            val t=item.optString("text","")
            if(t.isNotBlank()) texts.add(t)
        }
    }
    val answer=texts.distinct().joinToString("\\n")
    if(answer.isBlank()) throw IllegalStateException("Gemini returned no text. Response: "+r.toString().take(500))
    return "Connected • Gemini 3.8 Flash" to answer
}
private fun cloudflare(prompt:String):Pair<String,String>{val account=SecureKeys.get("cloudflare_account");val token=SecureKeys.get("cloudflare");if(account.isBlank()||token.isBlank())return "Missing Cloudflare details" to "Save Cloudflare API token and Account ID.";val u="https://api.cloudflare.com/client/v4/accounts/"+account+"/ai/run/@cf/meta/llama-3.1-8b-instruct";val msgs=JSONArray().put(JSONObject().put("role","system").put("content","You are Ultra Code Ultimate, a coding assistant.")).put(JSONObject().put("role","user").put("content",prompt));val r=post(u,JSONObject().put("messages",msgs),mapOf("Authorization" to "Bearer "+token));return "Connected • Cloudflare AI" to r.optJSONObject("result")?.optString("response",r.toString()).orEmpty()}
}