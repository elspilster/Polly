package uk.boxsignal.polly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    var message by remember { mutableStateOf("") }
    var pollyReply by remember { mutableStateOf("Hi! I'm Polly. What can I help you with?") }
    var isThinking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC)).padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.polly),
                contentDescription = "Polly",
                modifier = Modifier.size(90.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text("Polly", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Your capable AI assistant", fontSize = 15.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Text("Polly", fontWeight = FontWeight.Bold, color = pollyBlue)
        Spacer(modifier = Modifier.height(6.dp))
        Text(if (isThinking) "Thinking..." else pollyReply, fontSize = 18.sp)
        Spacer(modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ask Polly anything...") },
            shape = RoundedCornerShape(16.dp),
            enabled = !isThinking
        )

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val outgoing = message.trim()
                if (outgoing.isNotEmpty() && !isThinking) {
                    message = ""
                    isThinking = true
                    scope.launch {
                        pollyReply = askPolly(outgoing)
                        isThinking = false
                    }
                }
            },
            enabled = !isThinking,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = pollyBlue)
        ) {
            Text(if (isThinking) "THINKING..." else "SEND", fontSize = 17.sp, fontWeight = FontWeight.Bold)
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
