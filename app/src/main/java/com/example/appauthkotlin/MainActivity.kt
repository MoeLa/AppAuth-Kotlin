package com.example.appauthkotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.auth0.android.jwt.JWT
import com.example.appauthkotlin.ui.theme.AppAuthKotlinTheme
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import org.json.JSONException
import java.security.MessageDigest
import java.security.SecureRandom

class MainActivity : ComponentActivity() {

    // stores, as you might guess, the auth state of our app
    private var authState: AuthState = AuthState()

    // stores identity information for your authenticated user
    private var jwt: JWT? = null

    // used for managing the auth flow
    private lateinit var authorizationService: AuthorizationService
    private lateinit var authServiceConfig: AuthorizationServiceConfiguration

    private val authorizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                handleAuthorizationResponse(result.data!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreState()
        initAuthService()
        initAuthServiceConfig()

        setContent {
            AppAuthKotlinTheme {
                LoginScreen()
            }
        }
    }

    /**
     * Restores the authState, if possible.
     */
    fun restoreState() {
        val jsonString = application
            .getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(Constants.AUTH_STATE, null)

        if (jsonString != null && !TextUtils.isEmpty(jsonString)) {
            try {
                authState = AuthState.jsonDeserialize(jsonString)

                if (!TextUtils.isEmpty(authState.idToken)) {
                    jwt = JWT(authState.idToken!!)
                }
            } catch (_: JSONException) {
            }
        }
    }

    /**
     * Persists the authState.
     */
    fun persistState() {
        application
            .getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(Constants.AUTH_STATE, authState.jsonSerializeString())
            .apply()
    }

    private fun initAuthServiceConfig() {
        authServiceConfig = AuthorizationServiceConfiguration(
            Uri.parse(Constants.URL_AUTHORIZATION),
            Uri.parse(Constants.URL_TOKEN_EXCHANGE),
            null,
            Uri.parse(Constants.URL_LOGOUT)
        )
    }

    private fun initAuthService() {
        val appAuthConfiguration = AppAuthConfiguration.Builder()
//            .setBrowserMatcher(
//                BrowserAllowList(
//                    VersionedBrowserMatcher.CHROME_CUSTOM_TAB
//                )
//            )
            .build()

        authorizationService = AuthorizationService(
            application,
            appAuthConfiguration
        )
    }

    @Composable
    fun LoginScreen() {
        Surface(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Text("AppAuth Kotlin")
                Button(
                    onClick = { attemptAuthorization() }
                ) {
                    Text("Login")
                }
            }
        }
    }

    fun attemptAuthorization(): Unit {
        println("Attempting authorization...")

        val secureRandom = SecureRandom()
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)

        val encoding = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        val codeVerifier = Base64.encodeToString(bytes, encoding)

        println("codeVerifier: $codeVerifier")

        val digest = MessageDigest.getInstance(Constants.MESSAGE_DIGEST_ALGORITHM)
        val hash = digest.digest(codeVerifier.toByteArray())
        val codeChallenge = Base64.encodeToString(hash, encoding)

        println("codeChallenge: $codeChallenge")

        val builder = AuthorizationRequest.Builder(
            authServiceConfig,
            Constants.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(Constants.URL_AUTH_REDIRECT),
        ).setCodeVerifier(
            codeVerifier,
            codeChallenge,
            Constants.CODE_VERIFIER_CHALLENGE_METHOD
        )

        builder.setScopes(
            Constants.SCOPE_PROFILE,
            Constants.SCOPE_EMAIL,
            Constants.SCOPE_OPENID,
            Constants.SCOPE_DRIVE
        )

        // Add further parameters, if needed
//        val parameters = HashMap<String, String>()
//        paramters.put("Example", "Example")
//        builder.setAdditionalParameters(parameters)

        val request = builder.build()

        val authIntent = authorizationService.getAuthorizationRequestIntent(request)
        launchForResult(authIntent)
    }

    private fun launchForResult(authIntent: Intent) {
        authorizationLauncher.launch(authIntent)
    }

    private fun handleAuthorizationResponse(intent: Intent) {
        val authorizationResponse: AuthorizationResponse? = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)

        authState = AuthState(authorizationResponse, error)

        if (authorizationResponse != null) {
            val tokenExchangeRequest = authorizationResponse.createTokenExchangeRequest()

            println("tokenExchangeRequest: $tokenExchangeRequest")

            authorizationService.performTokenRequest(tokenExchangeRequest) { response, exception ->
                if (exception != null) {
                    authState = AuthState()
                } else {
                    if (response != null) {
                        authState.update(response, exception)
                        jwt = JWT(response.idToken!!)
                    }
                }
                persistState()
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
            LoginScreen()
        }
    }
}