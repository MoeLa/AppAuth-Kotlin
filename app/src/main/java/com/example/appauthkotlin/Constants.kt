package com.example.appauthkotlin

class Constants {
    companion object {
        val SHARED_PREFERENCES_NAME = "AUTH_STATE_PREFERENCE"
        val AUTH_STATE = "AUTH_STATE"

        val SCOPE_PROFILE = "profile"
        val SCOPE_EMAIL = "email"
        val SCOPE_OPENID = "openid"
        val SCOPE_DRIVE = "https://www.googleapis.com/auth/drive"

        val DATA_PICTURE = "picture"
        val DATA_FIRST_NAME = "given_name"
        val DATA_LAST_NAME = "family_name"
        val DATA_EMAIL = "email"

        val CLIENT_ID = "431127341567-gp7i9oep4le5fae1fv1t62j0ah879thv.apps.googleusercontent.com" // %USERPROFILE%\.android\debug.keystore
//        val CLIENT_ID = "431127341567-js9o520sk8n1jqkv1ladjpssfjird53u.apps.googleusercontent.com" // C:\Android\.android\debug.keystore
        val CODE_VERIFIER_CHALLENGE_METHOD = "S256"
        val MESSAGE_DIGEST_ALGORITHM = "SHA-256"

        val URL_AUTHORIZATION = "https://accounts.google.com/o/oauth2/v2/auth"
        val URL_TOKEN_EXCHANGE = "https://www.googleapis.com/oauth2/v4/token"
        val URL_AUTH_REDIRECT = "com.example.appauthkotlin:/oauth2redirect"
//        val URL_AUTH_REDIRECT = "com.googleusercontent.apps.PREFIX:/oauth/callback"
        val URL_API_CALL = "https://www.googleapis.com/drive/v2/files"
        val URL_LOGOUT = "https://accounts.google.com/o/oauth2/revoke?token="

        val URL_LOGOUT_REDIRECT = "com.example.appauthkotlin:/logout"
    }
}