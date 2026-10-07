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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PollyApp()
        }
    }
}

@Composable
fun PollyApp() {
    var showChat by remember { mutableStateOf(false) }

    if (showChat) {
        PollyChat()
    } else {
        PollyHome(
            onTalkToPolly = { showChat = true }
        )
    }
}

@Composable
fun PollyHome(onTalkToPolly: () -> Unit) {

    val pollyBlue = Color(0xFF1976D2)
    val background = Color(0xFFF7F9FC)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.polly),
            contentDescription = "Polly",
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Hello, I'm Polly.",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF20242A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your capable AI assistant.",
            fontSize = 17.sp,
            color = Color(0xFF606770)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onTalkToPolly,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = pollyBlue
            )
        ) {
            Text(
                text = "TALK TO POLLY",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun PollyChat() {

    val pollyBlue = Color(0xFF1976D2)
    var message by remember { mutableStateOf("") }
    var pollyReply by remember {
        mutableStateOf("Hi! I'm Polly. What can I help you with?")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .padding(20.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = R.drawable.polly),
                contentDescription = "Polly",
                modifier = Modifier.size(90.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Polly",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your capable AI assistant",
                    fontSize = 15.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Polly",
            fontWeight = FontWeight.Bold,
            color = pollyBlue
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = pollyReply,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Ask Polly anything...")
            },
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (message.isNotBlank()) {
                    pollyReply =
                        "I heard you say: \"$message\"\n\nMy AI brain isn't connected yet — but I'm listening!"
                    message = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = pollyBlue
            )
        ) {
            Text(
                text = "SEND",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}