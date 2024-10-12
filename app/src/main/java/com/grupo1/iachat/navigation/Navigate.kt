package com.grupo1.iachat.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.grupo1.iachat.components.Conversations
import com.grupo1.iachat.screens.ChatScreen
import com.grupo1.iachat.screens.HistoryScreen
import com.grupo1.iachat.screens.ResumeChatScreen
import com.grupo1.iachat.screens.getCurrentTime

@Composable
fun Navigate() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Router.Chat.router) {
        composable(route = Router.Chat.router) {
            ChatScreen(navController, Conversations("", emptyList(), getCurrentTime()))
        }
        composable(route = Router.History.router) {
            HistoryScreen(navController)
        }
        composable(
            route = "${Router.ResumeChat.router}?conversationID={conversationID}",
            arguments = listOf(
                navArgument("conversationID") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val conversationID = backStackEntry.arguments?.getString("conversationID") ?: ""
            Log.d("ResumeChat", "ConversationID: $conversationID")
            ResumeChatScreen(navController, conversationID)
        }
    }
}


