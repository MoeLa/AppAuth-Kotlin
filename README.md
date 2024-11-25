# AppAuth-Kotlin
Implementation of Paul Ruiz' article "Authenticating on Android with the AppAuth Library" -> https://medium.com/androiddevelopers/authenticating-on-android-with-the-appauth-library-7bea226555d5

# Notes
* When creating the *OAuth 2.0 Client ID*, you need to explicitly enable custom URI scheme (in the Advances Settings), see [developers documentation](https://developers.google.com/identity/protocols/oauth2/native-app#redirect-uri_custom-scheme).
* I had to do some refactoring of Paul's code snippet suggestions, especially when it's about the *jwt*
* My package name differs
