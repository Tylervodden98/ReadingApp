package com.example.readerapp.screens.update

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.readerapp.R
import com.example.readerapp.components.InputField
import com.example.readerapp.components.RatingBar
import com.example.readerapp.components.ReaderAppBar
import com.example.readerapp.components.RoundedButton
import com.example.readerapp.data.DataOrException
import com.example.readerapp.data.Resource
import com.example.readerapp.model.MBook
import com.example.readerapp.navigation.ReaderScreens
import com.example.readerapp.screens.home.HomeScreenViewModel
import com.example.readerapp.utils.formatDate
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ReaderUpdateScreen(navController: NavController, bookItemId: String, viewModel: HomeScreenViewModel = hiltViewModel()) {

    Scaffold(topBar = {
        ReaderAppBar(title = "Update Book",
            icon = Icons.Default.ArrowBack,
            showProfile = false,
            navController = navController){
            navController.popBackStack()
        }
    }) {

        val bookInfo = produceState<DataOrException<List<MBook>, Boolean, Exception>>(initialValue = DataOrException(data = emptyList(), true, Exception(""))){
            value = viewModel.data.value
        }.value

        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(3.dp)) {
            Column(modifier = Modifier.padding(top = 3.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Log.d("INFO", "ReaderUpdateScreen: ${viewModel.data.value.data.toString()}")
                if(bookInfo.loading == true){
                    LinearProgressIndicator()
                    bookInfo.loading = false
                }else{
                    Surface(modifier = Modifier
                        .padding(2.dp)
                        .fillMaxWidth(), shape = CircleShape, elevation = 4.dp) {
                        ShowBookUpdate(bookInfo = viewModel.data.value, bookItemId = bookItemId)
                    }

                    ShowSimpleForm(book = viewModel.data.value.data?.first {
                            mBook -> mBook.googleBookId == bookItemId
                    }!!, navController)
                }
            }
        }
        }
    }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShowSimpleForm(book: MBook, navController: NavController) {
    val context = LocalContext.current

    val notesText = remember {
        mutableStateOf("")
    }
    val isStartedReading = remember {
        mutableStateOf(false)
    }

    val isFinishedReading = remember {
        mutableStateOf(false)
    }

    val ratingVal = remember {
        mutableStateOf(0)
    }

    SimpleForm(defaultValue = if (book.notes.toString().isNotEmpty()) book.notes.toString()
        else "No Thoughts Available"){ note ->
        Log.d("Test", "ShowSimpleForm: $note")
        notesText.value = note
    }
    
    Row(modifier = Modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start) {
        TextButton(onClick = { isStartedReading.value = true }, enabled = book.startedReading == null) {
            if (book.startedReading == null){
                if (!isStartedReading.value){
                    Text(text = "Start Reading")
                }else{
                    Text(text = "Started Reading!", modifier = Modifier.alpha(0.6f), color = Color.Red.copy(alpha = 0.5f))
                }
            }else{
                Text(text = "Started on: ${formatDate(book.startedReading!!)}")
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        TextButton(onClick = { isFinishedReading.value = true }, enabled = book.finishedReading == null) {
            if(book.finishedReading == null){
                if(!isFinishedReading.value){
                    Text(text = "Mark as Read")
                }else{
                    Text(text = "Finished Reading!")
                }
            }else{
                Text(text = "Finished on: ${formatDate(book.finishedReading!!)}")
            }
        }

    }
    Text(text = "Rating", modifier = Modifier.padding(bottom = 3.dp))
    book.rating?.toInt().let {
        RatingBar(rating = it!!){ rating ->
            Log.d("Test", "In Rating Bar ${ratingVal.value}")
            ratingVal.value = rating
        }
    }

    Row (modifier = Modifier
        .padding(4.dp)
        .padding(start = 10.dp, top = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly){
        RoundedButton(label = "Update", radius = 40){
            val changedNotes = book.notes != notesText.value
            val changedRating = book.rating?.toInt() != ratingVal.value
            val isFinishedTimeStamp = if(isFinishedReading.value) Timestamp.now() else book.finishedReading
            val isStartedTimeStamp = if(isStartedReading.value) Timestamp.now() else book.startedReading

            val bookUpdate = changedNotes || changedRating || isStartedReading.value || isFinishedReading.value

            val bookToUpdate = hashMapOf(
                "finished_reading" to isFinishedTimeStamp,
                "started_reading" to isStartedTimeStamp,
                "rating" to ratingVal.value,
                "notes" to notesText.value
            ).toMap()

            Log.d("Test", "ShowSimpleForm: ${ratingVal.value}")

            if(bookUpdate){
                FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(book.id!!)
                    .update(bookToUpdate)
                    .addOnCompleteListener{
                        showToast(context, "${book.title} Updated Succesfully")
                        navController.navigate(ReaderScreens.ReaderHomeScreen.name)
                        //Log.d("Success", "ShowSimpleForm: ${task.result} ${ratingVal.value} ${notesText.value}")
                    }.addOnFailureListener {
                        Log.w("Error", "ShowSimpleForm: ERROR UPDATING DOC", it)
                    }
            }
        }
        Spacer(modifier = Modifier.width(60.dp))

        val openDialog = remember {
            mutableStateOf(false)
        }
        
        if(openDialog.value){
            ShowAlertDialog(message = stringResource(id = R.string.app_name) + "\n" + stringResource(
                id = R.string.action), openDialog){
                FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(book.id!!)
                    .delete()
                    .addOnCompleteListener{
                        if(it.isSuccessful){
                            openDialog.value = false
                            //Dont popbackstack since we want the homescreen to be updated
                            navController.navigate(ReaderScreens.ReaderHomeScreen.name)
                        }
                    }
            }
        }
        RoundedButton(label = "Delete", radius = 40){
            openDialog.value = true
        }
    }
}

@Composable
fun ShowAlertDialog(message: String, openDialog: MutableState<Boolean>, onYesPress: () -> Unit) {
    if(openDialog.value){
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = { Text(text = "Delete Book")},
            text = { Text(text = message)},
            buttons = {
                Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = {onYesPress.invoke()}) {
                        Text(text = "Yes")
                    }

                    TextButton(onClick = { openDialog.value = false }) {
                        Text(text = "No")
                    }
                }
            }) 
    }
}

fun showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SimpleForm(modifier: Modifier = Modifier, loading: Boolean = false, defaultValue: String = "Great Book!", onSearch: (String) -> Unit){
    Column {
        val textFieldValue = rememberSaveable {
            mutableStateOf(defaultValue)
        }
        val keyboardController = LocalSoftwareKeyboardController.current
        val valid = remember(textFieldValue.value){
            textFieldValue.value.trim().isNotEmpty()
        }

        InputField(modifier = Modifier
            .height(140.dp)
            .padding(3.dp)
            .background(Color.White,
                CircleShape)
            .padding(horizontal = 20.dp, vertical = 12.dp),
            valueState = textFieldValue,
            labelId = "Enter Your Thoughts",
            enabled = true,
            onAction = KeyboardActions {
            if (!valid) return@KeyboardActions
                Log.d("Success", "SimpleForm: ${textFieldValue.value}")
            onSearch(textFieldValue.value.trim())
            keyboardController?.hide()
        })
    }
}

@Composable
fun ShowBookUpdate(bookInfo: DataOrException<List<MBook>, Boolean, Exception>, bookItemId: String) {

    Row() {
        Spacer(modifier = Modifier.width(43.dp))
        if(bookInfo.data != null){
            Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.Center) {
                CardListItem(book = bookInfo.data!!.first{
                    mBook -> mBook.googleBookId == bookItemId
                }, onPressDetails = {})
            }
        }
    }
}

@Composable
fun CardListItem(book: MBook, onPressDetails: () -> Unit) {
    Card(modifier = Modifier
        .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 8.dp)
        .clip(
            RoundedCornerShape(20.dp))
        .clickable { }, elevation = 8.dp) {
        Row() {
            Image(painter = rememberImagePainter(data = book.photoUrl.toString()), contentDescription = null, modifier = Modifier
                .height(100.dp)
                .width(120.dp)
                .padding(4.dp)
                .clip(
                    RoundedCornerShape(topStart = 120.dp,
                        topEnd = 20.dp,
                        bottomEnd = 0.dp,
                        bottomStart = 0.dp)))

            Column {
                Text(text = book.title.toString(),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .width(120.dp),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colors.primary)

                Text(text = book.authors.toString(),style = MaterialTheme.typography.body2,modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp))

                Text(text = book.publishedDate.toString(),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 8.dp)
                )

            }
        }

    }
}
