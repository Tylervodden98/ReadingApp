package com.example.readerapp.components

import android.media.Image
import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.ImagePainter.State.Empty.painter
import coil.compose.rememberImagePainter
import com.example.readerapp.R
import com.example.readerapp.model.MBook
import com.example.readerapp.navigation.ReaderScreens
import com.example.readerapp.screens.home.BookTitle
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ReaderLogo(modifier: Modifier = Modifier) {
    Text(
        text = "Reading List",
        modifier = modifier.padding(bottom = 16.dp),
        style = MaterialTheme.typography.h3,
        color = Color.Red.copy(alpha = 0.5f)
    )
}


@Composable
fun EmailInput(modifier: Modifier = Modifier, emailState: MutableState<String>, labelId: String = "Email", enabled: Boolean = true, imeAction: ImeAction = ImeAction.Next, onAction: KeyboardActions = KeyboardActions.Default){
    InputField(modifier = modifier, valueState = emailState, labelId = labelId, enabled = enabled, keyboardType = KeyboardType.Email, imeAction = imeAction, onAction = onAction)
}

@Composable
fun InputField(modifier: Modifier = Modifier, valueState: MutableState<String>, labelId: String, enabled: Boolean, isSingleLine: Boolean = true, keyboardType: KeyboardType = KeyboardType.Text, imeAction: ImeAction = ImeAction.Next, onAction: KeyboardActions = KeyboardActions.Default){
    OutlinedTextField(value = valueState.value, onValueChange = {
        valueState.value = it
    }, label = { Text(text = labelId)},
        singleLine = isSingleLine,
        textStyle = TextStyle(fontSize = 18.sp, color = MaterialTheme.colors.onBackground),
        modifier = modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = onAction
    )
}

//For Password UI
@Composable
fun PasswordInput(
    modifier: Modifier,
    passwordState: MutableState<String>,
    labelId: String,
    enabled: Boolean,
    passwordVisibility: MutableState<Boolean>,
    imeAction: ImeAction = ImeAction.Done,
    onAction: KeyboardActions = KeyboardActions.Default,
) {
    val visualTransformation = if(passwordVisibility.value) VisualTransformation.None else PasswordVisualTransformation()
    OutlinedTextField(value = passwordState.value, onValueChange = {
        passwordState.value = it
    },
        label = { Text(text = labelId)},
        singleLine = true,
        textStyle = TextStyle(fontSize = 18.sp, color = MaterialTheme.colors.onBackground),
        modifier = modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        visualTransformation = visualTransformation,
        trailingIcon = {PasswordVisibility(passwordVisibility = passwordVisibility)},
        keyboardActions = onAction)
}

@Composable
fun PasswordVisibility(passwordVisibility: MutableState<Boolean>) {
    val visibile = passwordVisibility.value
    IconButton(onClick = {passwordVisibility.value = !visibile}) {
        Icons.Default.Close
    }
}

//For AppBar & Home Screen Titles
@Composable
fun TitleSection(modifier: Modifier = Modifier, label: String){
    Surface(modifier = modifier.padding(start = 5.dp, top = 1.dp)) {
        Column {
            Text(text = label,fontSize = 19.sp, fontStyle = FontStyle.Normal, textAlign = TextAlign.Left)
        }
    }
}

@Composable
fun ReaderAppBar(title: String, icon: ImageVector? = null, showProfile: Boolean = true, navController: NavController, onBackArrowClicked:() -> Unit = {}) {
    TopAppBar(title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showProfile) {
                Icon(imageVector = Icons.Default.Book,
                    contentDescription = "Logo Icon",
                    modifier = Modifier
                        .padding(start = 4.dp, top = 4.dp, end = 14.dp, bottom = 4.dp)
                        .clip(
                            RoundedCornerShape(12.dp))
                        .scale(0.9f))

            }
            if(icon!= null){
                Icon(imageVector = icon, contentDescription = "arrow back", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier
                    .padding(start = 4.dp, top = 4.dp, end = 14.dp, bottom = 4.dp)
                    .clickable {
                        onBackArrowClicked.invoke()
                    })
            }

            Text(text = title,
                color = Color.Red.copy(0.7f),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp))
        }
    } , actions = {
        IconButton(onClick = {
            FirebaseAuth.getInstance().signOut().run {
                navController.navigate(ReaderScreens.LoginScreen.name)
            }
        }){

            if(showProfile) Row(){
                Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout Button", modifier = Modifier.padding(4.dp), tint = Color.LightGray.copy(0.7f))
            }else Box{}

        }
    }, backgroundColor = Color.Transparent, elevation = 0.dp)
}

@Composable
fun FABContent(onTap: () -> Unit) {
    FloatingActionButton(onClick = { onTap.invoke() },
        shape = RoundedCornerShape(50.dp),
        backgroundColor = Color(0xFF92CBDF)) {
        Icon(imageVector = Icons.Default.Add,
            contentDescription = "Add a Book",
            tint = MaterialTheme.colors.onSecondary)
    }
}

@Composable
fun BookRating(score: Double = 4.5, book: MBook) {
    Surface(modifier = Modifier
        .height(70.dp)
        .padding(4.dp),
        shape = RoundedCornerShape(56.dp),
        elevation = 6.dp,
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = if(book.rating != 0.0) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = "Star", modifier = Modifier.padding(3.dp),tint = if (book.rating!! != 0.0) Color.Yellow else Color.Black )
            Text(text = score.toString(), style = MaterialTheme.typography.subtitle1)
        }
    }
}


@Composable
fun ListCard(book: MBook,
             onPressDetails: (String) -> Unit = {}){
    val context = LocalContext.current
    val resources = context.resources

    val displayMetrics = resources.displayMetrics

    val screenWidth = displayMetrics.widthPixels / displayMetrics.density
    val spacing = 10.dp

    Card(shape = RoundedCornerShape(29.dp),
        backgroundColor = Color.White,
        elevation = 6.dp,
        modifier = Modifier
            .padding(16.dp)
            .height(242.dp)
            .width(202.dp)
            .clickable { onPressDetails.invoke(book.title.toString()) }) {
        Column(modifier = Modifier.width(screenWidth.dp - (spacing * 2))) {
            Row(horizontalArrangement = Arrangement.Center) {
                val imageUrl = if(book.photoUrl!!.isEmpty()) {
                    "http://books.google.com/books/content?id=JGH0DwAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs=api"
                }else{
                    book.photoUrl!!
                }

                Image(
                    painter = rememberImagePainter(data = imageUrl),
                    contentDescription = "Book Image",
                    modifier = Modifier
                        .height(140.dp)
                        .width(100.dp)
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(50.dp))
                Column(
                    modifier = Modifier.padding(top = 25.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if(book.rating!! < 3) Icons.Rounded.FavoriteBorder else Icons.Filled.Favorite,
                        contentDescription = "Fav Icon",
                        modifier = Modifier.padding(bottom = 1.dp),
                        tint = if(book.rating!! > 3) Color.Red else Color.Black
                    )
                    BookRating(score = book.rating!!, book = book)
                }
            }
            BookTitle(bookname = book.title, bookauthor = book.authors)

        }
        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
            RoundedButton(label = if(book.startedReading == null) "Not Read" else "Reading!",radius = 70){
                Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Preview
@Composable
fun RoundedButton(
    label: String = "Reading",
    radius: Int = 29,
    onPress: () -> Unit = {}
){
    Surface(modifier = Modifier.clip(RoundedCornerShape(bottomEndPercent = radius,
        topStartPercent = radius)),
        color = Color(0xFF92CBDF)) {
        Column(modifier = Modifier
            .width(90.dp)
            .heightIn(40.dp)
            .clickable { onPress.invoke() },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = TextStyle(color = Color.White, fontSize = 15.sp))
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Int,
    onPressRating: (Int) -> Unit
) {
    var ratingState by remember {
        mutableStateOf(rating)
    }

    var selected by remember {
        mutableStateOf(false)
    }
    val size by animateDpAsState(
        targetValue = if (selected) 42.dp else 34.dp,
        spring(Spring.DampingRatioMediumBouncy)
    )

    Row(
        modifier = Modifier.width(280.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..5) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_star_24),
                contentDescription = "star",
                modifier = modifier
                    .width(size)
                    .height(size)
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                selected = true
                                ratingState = i
                                onPressRating(ratingState)
                            }
                            MotionEvent.ACTION_UP -> {
                                selected = false
                            }
                        }
                        true
                    },
                tint = if (i <= ratingState) Color(0xFFFFD700) else Color(0xFFA2ADB1)
            )
        }
    }
}
