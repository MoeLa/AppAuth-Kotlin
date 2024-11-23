package com.example.appauthkotlin.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.appauthkotlin.AuthStateViewModel
import com.example.appauthkotlin.ui.theme.AppAuthKotlinTheme

@Composable
fun MainActivityScreen(
    authStateViewModel: AuthStateViewModel,
    onLogin: () -> Unit,
    onMakeApiCall: () -> Unit,
    onSignOut: () -> Unit
) {
    val authState by authStateViewModel.authState.observeAsState()

    AppAuthKotlinTheme {
        if (authState?.isAuthorized == true) {
            MakeApiCallScreen(
                onMakeApiCall = { onMakeApiCall() },
                onSignOut = { onSignOut() }
            )
        } else {
            LoginScreen(
                onLogin = { onLogin() }
            )
        }
    }
}

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Text("AppAuth Kotlin")
            Button(
                onClick = { onLogin() }
            ) {
                Text("Login")
            }
        }
    }
}

@Composable
fun MakeApiCallScreen(
    onMakeApiCall: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text("AppAuth Kotlin (logged in)")
            Button(
                onClick = { onMakeApiCall() }
            ) {
                Text("Make API call")
            }
            Button(
                onClick = { onSignOut() }
            ) {
                Text("Sign out")
            }
        }
    }
}

@Preview(
    name = "DarkMode Preview",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    showBackground = true
)
@Composable
private fun LoginScreenPreview() {
    AppAuthKotlinTheme {
        LoginScreen({})
    }
}

@Preview(
    name = "DarkMode Preview",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    showBackground = true
)
@Composable
private fun MakeApiCallScreenPreview() {
    AppAuthKotlinTheme {
        MakeApiCallScreen({}, {})
    }
}