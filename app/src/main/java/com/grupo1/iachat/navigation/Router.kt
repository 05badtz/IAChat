package com.grupo1.iachat.navigation

sealed class Router(val router: String) {
    object Chat : Router("ChatScreen")
    object History : Router("HistoryScreen")
    object ResumeChat : Router("ResumeChatScreen")
}