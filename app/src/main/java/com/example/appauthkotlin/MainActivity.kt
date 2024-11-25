package com.example.appauthkotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appauthkotlin.ui.MainActivityScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

class MainActivity : ComponentActivity() {

    private val LOG_TAG = MainActivity::class.qualifiedName

    // Stores, as you might guess, the auth state of our app
    private val authStateViewModel = AuthStateViewModel()

    // Stores identity information for your authenticated user - moved to UiComponents
    // private var jwt: JWT? = null

    // Used for managing the auth flow
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
            MainActivityScreen(
                authStateViewModel,
                onAuthorize = { attemptAuthorization() },
                onMakeApiCall = { makeApiCall() },
                onSignOut = { signOutWithoutRedirect() })
        }
    }

    private fun restoreState() {
        val jsonString = application
            .getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(Constants.AUTH_STATE, null)

        if (jsonString != null && !TextUtils.isEmpty(jsonString)) {
            try {
                val authState = AuthState.jsonDeserialize(jsonString)
                authStateViewModel.onAuthStateChange(authState)

                // Note: Extracting the JWT is done within the UI component, that actually use/display the data
            } catch (_: JSONException) {
            }
        }
    }

    private fun persistState() {
        val authState = authStateViewModel.authState.value!!

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
            .setBrowserMatcher(
                BrowserAllowList(
                    VersionedBrowserMatcher.CHROME_CUSTOM_TAB
                )
            )
            .build()

        authorizationService = AuthorizationService(
            application,
            appAuthConfiguration
        )
    }

    private fun makeApiCall() {
        val authState = authStateViewModel.authState.value

        authState!!.performActionWithFreshTokens(
            authorizationService,
            object : AuthState.AuthStateAction {
                override fun execute(
                    accessToken: String?,
                    idToken: String?,
                    ex: AuthorizationException?
                ) {
                    GlobalScope.launch {
                        async(Dispatchers.IO) {
                            val client = OkHttpClient()

                            val request = Request.Builder()
                                .url(Constants.URL_API_CALL)
                                .addHeader("Authorization", "Bearer $accessToken")
                                .build()

                            try {
                                val response = client.newCall(request).execute()
                                val jsonBody = response.body?.string() ?: ""
                                Log.i(LOG_TAG, JSONObject(jsonBody).toString())
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }
        )
    }

    private fun attemptAuthorization() {
        Log.i(LOG_TAG, "Attempting authorization...")

        val secureRandom = SecureRandom()
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)

        val encoding = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        val codeVerifier = Base64.encodeToString(bytes, encoding)

        Log.i(LOG_TAG, "codeVerifier: $codeVerifier")

        val digest = MessageDigest.getInstance(Constants.MESSAGE_DIGEST_ALGORITHM)
        val hash = digest.digest(codeVerifier.toByteArray())
        val codeChallenge = Base64.encodeToString(hash, encoding)

        Log.i(LOG_TAG, "codeChallenge: $codeChallenge")

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

    private fun signOut() {
        val authState = authStateViewModel.authState.value!!

        try {
            val endSessionRequest = EndSessionRequest.Builder(authServiceConfig)
                .setPostLogoutRedirectUri(Uri.parse(Constants.URL_LOGOUT))
                .setIdTokenHint(authState.accessToken)
                .build()

            val intent = authorizationService.getEndSessionRequestIntent(endSessionRequest)

            launchForResult(intent)
        } catch (_: Exception) {
        }
    }

    private fun signOutWithoutRedirect() {
        val authState = authStateViewModel.authState.value!!

        val request = Request.Builder()
            .url(Constants.URL_LOGOUT + authState.idToken)
            .build()

        val client = OkHttpClient()
        try {
            GlobalScope.launch {
                async(Dispatchers.IO) {
                    // Execute API call in separate thread
                    client.newCall(request).execute()
                }
            }
        } catch (_: IOException) {
        } finally {
            authStateViewModel.onAuthStateChange(AuthState())

            persistState()
        }
    }

    private fun launchForResult(authIntent: Intent) {
        authorizationLauncher.launch(authIntent)
    }

    private fun handleAuthorizationResponse(intent: Intent) {
        val authorizationResponse: AuthorizationResponse? = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)

        val authState = AuthState(authorizationResponse, error)
        // Note: We gonna update the authStateViewModel after the token request, not now

        if (authorizationResponse != null) {
            val tokenExchangeRequest = authorizationResponse.createTokenExchangeRequest()

            Log.i(LOG_TAG, "tokenExchangeRequest: $tokenExchangeRequest")

            authorizationService.performTokenRequest(tokenExchangeRequest) { response, exception ->
                if (exception != null) {
                    authStateViewModel.onAuthStateChange(AuthState())
                } else {
                    if (response != null) {
                        authState.update(response, exception)
                        authStateViewModel.onAuthStateChange(authState)
                    }
                }

                persistState()
            }
        }
    }
}

/**
 * ViewModel to hold the authState resp. its LiveData representative.
 */
class AuthStateViewModel : ViewModel() {

    private val _authState: MutableLiveData<AuthState> = MutableLiveData(AuthState())
    val authState: LiveData<AuthState> = _authState

    fun onAuthStateChange(newAuthState: AuthState) {
        _authState.value = newAuthState
    }

}