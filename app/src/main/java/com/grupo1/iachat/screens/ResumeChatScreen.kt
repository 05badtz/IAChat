package com.grupo1.iachat.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo1.iachat.components.loadMessages
import com.grupo1.iachat.data.PartBody
import com.grupo1.iachat.models.Message

@Composable
fun ResumeChatScreen(navController: NavController, conversationID: String) {
    val context = LocalContext.current
    val messages = remember { mutableStateListOf<Message>() }
    var isLoading by remember { mutableStateOf(false) }

    var isResume by remember { mutableStateOf(false) }
    LaunchedEffect(conversationID) {
        val sharedPreferences = context.getSharedPreferences("myMessages", Context.MODE_PRIVATE)

        val conversation = loadMessages(sharedPreferences).find { it.id == conversationID }
        if (conversation != null) {
            messages.addAll(conversation.messages)
        } else {
            messages.add(
                Message(
                    itsMine = false,
                    createdAt = getCurrentTime(),
                    partBody = PartBody(text = "¡Hola! Soy Jake, tu asistente de cocina. ¿Qué receta te gustaría conocer?")
                )
            )
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { TopBar(messages, isLoading, navController, true) },
        bottomBar = {
            if (isResume) {
                BottomBar(messages, isLoading) { isLoading = it }
            } else {
                Button(
                    onClick = { isResume = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text("Continuar Chat")
                }
            }
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
