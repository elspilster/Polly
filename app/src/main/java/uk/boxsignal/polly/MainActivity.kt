package uk.boxsignal.polly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(val fromPolly: Boolean, val text: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PollyApp() }
    }
}

@Composable
fun PollyApp() {
    var showChat by remember { mutableStateOf(false) }
    if (showChat) PollyChat() else PollyHome(onTalkToPolly = { showChat = true })
}

@Composable
fun PollyHome(onTalkToPolly: () -> Unit) {
    val pollyBlue = Color(0xFF1976D2)
    val background = Color(0xFFF7F9FC)
    Column(
        modifier = Modifier.fillMaxSize().background(background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.polly),
            contentDescription = "Polly",
            modifier = Modifier.fillMaxWidth().height(360.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Hello, I'm Polly.", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF20242A))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your capable AI assistant.", fontSize = 17.sp, color = Color(0xFF606770))
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = onTalkToPolly,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = pollyBlue)
        ) {
            Text("TALK TO POLLY", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun PollyChat() {
    val pollyBlue = Color(0xFF1976D2)
    val messages = remember {
        mutableStateListOf(ChatMessage(true, "Hi! I'm Polly. What can I help you with?"))
    }
    var message by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isThinking) {
        val extra = if (isThinking) 1 else 0
        val target = messages.size + extra - 1
        if (target >= 0) listState.animateScrollToItem(target)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.polly),
                contentDescription = "Polly",
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Polly", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Your capable AI assistant", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { chat ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (chat.fromPolly) "Polly" else "You",
                        fontWeight = FontWeight.Bold,
                        color = if (chat.fromPolly) pollyBlue else Color(0xFF50555C)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(chat.text, fontSize = 17.sp, color = Color(0xFF20242A))
                }
            }
            if (isThinking) {
                item {
                    Column {
                        Text("Polly", fontWeight = FontWeight.Bold, color = pollyBlue)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("Thinking…", fontSize = 17.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ask Polly anything...") },
            shape = RoundedCornerShape(16.dp),
            enabled = !isThinking,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val outgoing = message.trim()
                if (outgoing.isNotEmpty() && !isThinking) {
                    messages.add(ChatMessage(false, outgoing))
                    message = ""
                    isThinking = true
                    scope.launch {
                        val reply = askPolly(outgoing)
                        messages.add(ChatMessage(true, reply))
                        isThinking = false
                    }
                }
            },
            enabled = !isThinking,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = pollyBlue)
        ) {
            Text(if (isThinking) "THINKING..." else "SEND", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private suspend fun askPolly(message: String): String = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
        connection = (URL("https://polly-brain.vercel.app/api/chat").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 45000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }
        val body = JSONObject().put("message", message).toString()
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        val json = if (responseText.isNotBlank()) JSONObject(responseText) else JSONObject()
        if (status in 200..299) {
            json.optString("reply").ifBlank { "I'm here, but I couldn't form a reply." }
        } else {
            json.optString("error").ifBlank { "I couldn't reach my AI brain just now." }
        }
    } catch (e: Exception) {
        "I couldn't connect to my AI brain. Please check the internet connection and try again."
    } finally {
        connection?.disconnect()
    }
}
