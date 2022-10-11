package com.example.readerapp.screens.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.ThumbUpAlt
import androidx.compose.material.icons.sharp.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.readerapp.components.ReaderAppBar
import com.example.readerapp.model.Item
import com.example.readerapp.model.MBook
import com.example.readerapp.navigation.ReaderScreens
import com.example.readerapp.screens.home.HomeScreenViewModel
import com.example.readerapp.screens.search.BookRow
import com.example.readerapp.utils.formatDate
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ReaderStatsScreen(navController: NavController, viewModel: HomeScreenViewModel = hiltViewModel()) {
    var books: List<MBook>
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    Scaffold(topBar = {
        ReaderAppBar(title = "Book Stats", icon = Icons.Default.ArrowBack, showProfile = false, navController = navController){
            navController.popBackStack()
        }
    }) {
        Surface {
            //only show books by this user that are READ
            books = if(!viewModel.data.value.data.isNullOrEmpty()){
                viewModel.data.value.data!!.filter { mBook ->
                    (mBook.userId == currentUser?.uid)
                }
            }else{
                emptyList()
            }

            Column (modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top){
                Row(horizontalArrangement = Arrangement.Center){
                    Box(modifier = Modifier
                        .size(45.dp)
                        .padding(2.dp)){
                        Icon(imageVector = Icons.Sharp.Person, contentDescription = "Person Icon")
                    }
                    Text(text = "Hi, ${currentUser?.email.toString().split("@")[0].uppercase()}")
                }
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                    shape = CircleShape,
                    elevation = 5.dp) {
                    val readBooksList: List<MBook> = if (!viewModel.data.value.data.isNullOrEmpty()){
                        books.filter { mBook ->
                            (mBook.userId == currentUser?.uid) && (mBook.finishedReading != null)
                        }
                    }else{
                        emptyList()
                    }

                    val readingBooks = books.filter { mBook ->
                        (mBook.startedReading!= null) && (mBook.finishedReading == null)
                    }
                    Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                        Text(text = "Your Stats:", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp))
                        Divider()
                        Text(text = "You're Reading : ${readingBooks.size} books", style = TextStyle(fontWeight = FontWeight.Light, fontSize = 16.sp), color = Color.DarkGray)
                        Text(text = "You've Read : ${readBooksList.size} books", style = TextStyle(fontWeight = FontWeight.Light, fontSize = 16.sp), color = Color.DarkGray)
                    }
                }

                if (viewModel.data.value.loading == true){
                    LinearProgressIndicator()
                }else{
                    Divider()
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)){
                        //filter books by only finished ones
                        val readBooks: List<MBook> = if (!viewModel.data.value.data.isNullOrEmpty()){
                            viewModel.data.value.data!!.filter { mBook ->
                                (mBook.userId == currentUser?.uid) && (mBook.finishedReading != null)
                            }
                        }else{
                            emptyList()
                        }

                        items(items = readBooks){ book ->
                            BookRowStats(book = book)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookRowStats(book: MBook) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .padding(4.dp)
        .clickable {

        }, elevation = 4.dp, shape = RectangleShape) {
        Row() {
            val imageUrl: String = if(book.photoUrl.toString().isEmpty()) "http://books.google.com/books/content?id=JGH0DwAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs=api"
            else{
                book.photoUrl.toString()
            }

            Image(painter = rememberImagePainter(data = imageUrl), contentDescription = "Android pic",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(70.dp)
                    .padding(4.dp), contentScale = ContentScale.FillHeight)
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(4.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = if (book.title!!.isNotEmpty()) book.title!! else{"Null"}, fontSize = 16.sp, overflow = TextOverflow.Ellipsis)
                    if (book.rating!! > 3) Icon(imageVector = Icons.Rounded.ThumbUpAlt, contentDescription = "Liked Icon", tint = Color.Red.copy(alpha = 0.4f)) else Box{}
                }

                Text(text = if (book.authors.toString().isNotEmpty()) "Authors: ${book.authors.toString()}"
                else{"Null"}, fontSize = 12.sp, fontStyle = FontStyle.Italic,overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption)
                Text(text = "Started On: ${formatDate(book.startedReading!!)}", fontSize = 12.sp, fontStyle = FontStyle.Italic,overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption)
                Text(text = "Finished On: ${formatDate(book.finishedReading!!)}", fontSize = 12.sp, fontStyle = FontStyle.Italic,overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption)

            }
        }
    }
}