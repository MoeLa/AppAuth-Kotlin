package com.example.appauthkotlin

import com.example.appauthkotlin.tutorial.SampleData
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.appauthkotlin.tutorial.Conversation
import com.example.appauthkotlin.ui.theme.AppAuthKotlinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppAuthKotlinTheme {
                Conversation(SampleData.conversationSample)
            }
        }
    }
}
