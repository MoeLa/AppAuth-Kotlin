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
import coil3.compose.AsyncImage
import com.auth0.android.jwt.JWT
import com.example.appauthkotlin.AuthStateViewModel
import com.example.appauthkotlin.Constants
import com.example.appauthkotlin.ui.theme.AppAuthKotlinTheme

@Composable
fun MainActivityScreen(
    authStateViewModel: AuthStateViewModel,
    onAuthorize: () -> Unit,
    onMakeApiCall: () -> Unit,
    onSignOut: () -> Unit
) {
    val authState by authStateViewModel.authState.observeAsState()

    AppAuthKotlinTheme {
        if (authState?.isAuthorized == true) {
            val jwt = JWT(authState!!.idToken!!)

            MakeApiCallScreen(
                onMakeApiCall = { onMakeApiCall() },
                onSignOut = { onSignOut() },
                picture = jwt.getClaim(Constants.DATA_PICTURE).asString()!!,
                givenName = jwt.getClaim(Constants.DATA_FIRST_NAME).asString()!!,
                familyName = jwt.getClaim(Constants.DATA_LAST_NAME).asString()!!,
                email = jwt.getClaim(Constants.DATA_EMAIL).asString()!!
            )
        } else {
            LoginScreen(
                onAuthorize = { onAuthorize() }
            )
        }
    }
}

@Composable
fun LoginScreen(onAuthorize: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Text("AppAuth Kotlin")
            Button(
                onClick = { onAuthorize() }
            ) {
                Text("Authorize")
            }
        }
    }
}

@Composable
fun MakeApiCallScreen(
    onMakeApiCall: () -> Unit,
    onSignOut: () -> Unit,
    picture: String = "https://lh3.googleusercontent.com/a/AATXAJxLybvnZXDYrK8zm2XglOSqt_anMmSP1_vqXbvr=s96-c",
    givenName: String = "John",
    familyName: String = "Doe",
    email: String = "john.doe@gmail.com"
) {

    Surface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text("AppAuth Kotlin")
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
            // Note: The AsyncImage doesn't appear in Android Studio's Design Preview
            AsyncImage(
                model = picture,
                contentDescription = "Your Google's Profile Picture"
            )
            Text("$givenName $familyName")
            Text(email)
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