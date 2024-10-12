package com.grupo1.iachat.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grupo1.iachat.R
import com.grupo1.iachat.components.Conversations
import com.grupo1.iachat.components.saveMessages
import com.grupo1.iachat.data.ContentBody
import com.grupo1.iachat.data.PartBody
import com.grupo1.iachat.data.RequestBody
import com.grupo1.iachat.geminiApi.ApiService
import com.grupo1.iachat.models.Message
import com.grupo1.iachat.navigation.Router
import com.grupo1.iachat.ui.theme.IAchatTheme
import com.grupo1.iachat.ui.theme.PurpleGrey80
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID


@Composable
fun ChatScreen(navController: NavController, conversation: Conversations) {
    val messages = remember { mutableStateListOf<Message>() }
    var isLoading by remember { mutableStateOf(false) }
    val isResume by remember { mutableStateOf(conversation.messages.isNotEmpty()) }

    // Enviar un mensaje inicial de la IA al cargar la pantalla
    LaunchedEffect(Unit) {
        messages.add(
            Message(
                itsMine = false,
                createdAt = getCurrentTime(),
                partBody = PartBody(text = "¡Hola! Soy Jake, tu asistente de cocina. ¿Qué receta te gustaría conocer?")
            )
        )
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { TopBar(messages, isLoading, navController, isResume) },
        bottomBar = {
            BottomBar(
                messages,
                isLoading,
                onLoadingChange = { isLoading = it })
        }
    ) { innerPadding ->
        val lazyState = rememberLazyListState()
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                state = lazyState
            ) {
                items(messages) { message ->
                    BubbleMessage(message = message)
                }
            }
        }
        LaunchedEffect(messages.size) {
            lazyState.animateScrollToItem(messages.size - 1)
        }
    }
}

fun sendToApi(
    apiService: ApiService,
    apiKey: String,
    messages: List<Message>,
    onResponse: (String?) -> Unit
) {
    val partBodies: List<PartBody> = messages.map { it.partBody }

    val iaBody =
        PartBody("Actúa como Jake el Perro de Hora de Aventura y trata  al usuario como si fuera Finn el Humano. Eres un asistente de cocina, eres un chef experto en repostería, desde ahora, enseñame preparaciones y hazme preguntas al finalizar tu mensaje para asefurarte que estoy siguiendo correctamente las indicaciones. Si escribo algo que no entiendes, hazme una pregunta que te ayude a seguir la conversación de la forma más fluida posible. En relación a la respuesta, no uses markdown y que no dure más de 300 caracteres. Utiliza un tono comunicacional amable e incluye pocos emojis en la respuesta")

    val requestBody = RequestBody(
        contents = listOf(
            ContentBody(
                parts = listOf(iaBody) + partBodies
            )
        )
    )

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = apiService.askToGemini(apiKey, requestBody)
            if (response.isSuccessful) {
                val responseData = response.body()
                val apiMessage =
                    responseData?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                onResponse(apiMessage)
            } else {
                onResponse(null) // Devolver null si hubo algún error
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResponse(null) // Devolver null si hubo algún error
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    messages: SnapshotStateList<Message>,
    isLoading: Boolean,
    navController: NavController,
    isResume: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("myMessages", Context.MODE_PRIVATE)
    TopAppBar({
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp)
                .then(modifier),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Asistente de IA",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            )
            if (isResume) {
                Button(onClick = { navController.navigate(Router.Chat.router) }) {
                    Text("Volver")
                }
            } else {
                Button(onClick = {
                    val conversation = Conversations(
                        UUID.randomUUID().toString(),
                        messages.toList(),
                        getCurrentTime()
                    )
                    saveMessages(sharedPreferences, conversation)
                    navController.navigate(Router.History.router)
                }) {
                    Text(text = "Ver historial")
                }
            }

        }
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }
    })
}

@Composable
fun BottomBar(
    messages: SnapshotStateList<Message>,
    isLoading: Boolean, // Recibimos el estado de carga
    onLoadingChange: (Boolean) -> Unit // Función para actualizar el estado de carga
) {

    var message by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val hideKeyboard = LocalSoftwareKeyboardController.current
    BottomAppBar(containerColor = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(start = 10.dp),
                value = message,
                onValueChange = { message = it },
                enabled = !isLoading
            )
            IconButton(
                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                onClick = {
                    if (message.isNotBlank() && !isLoading) {
                        val newMessage = Message(
                            itsMine = true,
                            createdAt = getCurrentTime(),
                            partBody = PartBody(text = message)
                        )
                        messages.add(newMessage)
                        message = ""
                        onLoadingChange(true)

                        // Crear la instancia de Retrofit
                        val retrofit = Retrofit.Builder()
                            .baseUrl("https://generativelanguage.googleapis.com")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()

                        // Hacer la llamada a la API en un coroutine
                        coroutineScope.launch {
                            sendToApi(
                                apiService = retrofit.create(ApiService::class.java),
                                apiKey = "insertApiKey",
                                messages = messages
                            ) { apiResponse ->
                                apiResponse?.let {
                                    val newMessageIA = Message(
                                        itsMine = false,
                                        createdAt = getCurrentTime(),
                                        partBody = PartBody(text = it)
                                    )
                                    messages.add(newMessageIA)// Mensaje de la IA
                                }
                                onLoadingChange(false)
                            }
                        }
                    }
                    hideKeyboard?.hide()

                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun BubbleMessage(message: Message) {
    val avatarIA = R.drawable.chefjake
    val avatarMe = R.drawable.finn
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (message.itsMine) Arrangement.End else Arrangement.Start
    ) {
        if (!message.itsMine) {
            Image(
                painter = painterResource(avatarIA),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(50.dp)
                    .clip(CircleShape)
            )
        }

        Column(
            modifier = Modifier
                .padding(6.dp)
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .align(if (message.itsMine) Alignment.End else Alignment.Start)
                    .clip(
                        RoundedCornerShape(
                            topStart = if (message.itsMine) 48f else 0f,
                            topEnd = if (message.itsMine) 0f else 48f,
                            bottomStart = 48f,
                            bottomEnd = 48f
                        )
                    )
                    .background(PurpleGrey80)
                    .padding(15.dp)
            ) {
                Column(
                    horizontalAlignment = if (message.itsMine) Alignment.End else Alignment.Start
                ) {
                    Text(
                        text = message.partBody.text,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.createdAt,
                        color = Color.Gray,
                        modifier = Modifier.align(if (message.itsMine) Alignment.End else Alignment.Start)
                    )
                }
            }
        }
        if (message.itsMine) {
            Image(
                painter = painterResource(avatarMe),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(50.dp)
                    .clip(CircleShape)
            )
        }
    }
}


fun getCurrentTime(): String {
    val now = LocalTime.now()
    val format = DateTimeFormatter.ofPattern("HH:mm")
    return now.format(format)
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    IAchatTheme {
        //ChatScreen(navController = rememberNavController())
    }
}