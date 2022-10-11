package com.example.readerapp.screens.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.readerapp.R
import com.example.readerapp.components.EmailInput
import com.example.readerapp.components.PasswordInput
import com.example.readerapp.components.ReaderLogo
import com.example.readerapp.navigation.ReaderScreens
import com.example.readerapp.screens.home.ReaderHomeScreen
import com.google.firebase.firestore.auth.User

@Composable
fun ReaderLoginScreen(navController: NavController, viewModel: LoginScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val showLoginForm = rememberSaveable {
        mutableStateOf(true)
    }
    
    androidx.compose.material.Surface(modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = CenterHorizontally, verticalArrangement = Arrangement.Top) {
            ReaderLogo()
            if(showLoginForm.value){
                UserForm(loading = false, isCreateAccount = false){ email, password ->
                    Log.d("Form", "ReaderLoginScreen: $email $password")
                    viewModel.signInWithEmailAndPassword(email, password){
                        navController.navigate(ReaderScreens.ReaderHomeScreen.name)
                    }
                }
            } else{
                UserForm(loading = false, isCreateAccount = true){ email, password ->
                    //Todo: create FB account
                    viewModel.createUserWithEmailAndPassword(email,password){
                        navController.navigate(ReaderScreens.ReaderHomeScreen.name)
                    }
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            Row(modifier = Modifier.padding(15.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = CenterVertically) {
                val text = if (showLoginForm.value) "Sign Up" else "Login"
                val navtext = if (showLoginForm.value) "New User?" else "Already Have an Account?"
                Text(text = navtext)
                Text(text = text, modifier = Modifier
                    .clickable { showLoginForm.value = !showLoginForm.value }
                    .padding(5.dp), color = Color(
                    0xFF0B8174), fontWeight = FontWeight.Bold)
            }
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun UserForm(loading: Boolean = false, isCreateAccount: Boolean = false, onDone: (String, String) -> Unit = { _, _ ->}){
    val email = rememberSaveable {
        mutableStateOf("")
    }

    val password = rememberSaveable {
        mutableStateOf("")
    }

    val passwordVisibility = rememberSaveable{
        mutableStateOf(false)
    }
    val passwordFocusRequest = FocusRequester.Default
    val keyboardController = LocalSoftwareKeyboardController.current
    val valid = remember(email.value, password.value) {
        email.value.trim().isNotEmpty() && password.value.trim().isNotEmpty()
    }

    val modifier = Modifier
        .height(250.dp)
        .background(MaterialTheme.colors.background)
        .verticalScroll(
            rememberScrollState()
        )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if(isCreateAccount) Text(text = stringResource(id = R.string.create_acct), modifier = Modifier.padding(4.dp))
        else Text(text = "")
        EmailInput(emailState = email, enabled = !loading, onAction = KeyboardActions {passwordFocusRequest.requestFocus()})
        PasswordInput(
            modifier = Modifier.focusRequester(passwordFocusRequest),
            passwordState = password,
            labelId = "Password",
            enabled = !loading,
            passwordVisibility = passwordVisibility,
            onAction = KeyboardActions {
                if (!valid) return@KeyboardActions
                onDone(email.value.trim(), password.value.trim())
            }
        )
        
        SubmitButton(textId = if(isCreateAccount) "Create Account" else "Login", loading = loading, validInputs = valid){
            onDone(email.value.trim(), password.value.trim())
            keyboardController?.hide()
        }
    }
}

@Composable
fun SubmitButton(textId: String, loading: Boolean, validInputs: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick,
        modifier = Modifier
            .padding(3.dp)
            .fillMaxWidth(),
        enabled = !loading && validInputs,
        shape = CircleShape) {
        if(loading) CircularProgressIndicator(modifier = Modifier.size(25.dp))
        else Text(text = textId, modifier = Modifier.padding(5.dp))
    }
}


