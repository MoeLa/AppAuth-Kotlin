package com.example.appauthkotlin.tutorial

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.appauthkotlin.ui.theme.AppAuthKotlinTheme

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Preview
@Composable
fun PreviewConversation() {
    AppAuthKotlinTheme {
        Conversation(SampleData.conversationSample)
    }
}