package com.example.readerapp.screens.details

import android.util.Log
import android.widget.Space
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.readerapp.components.ReaderAppBar
import com.example.readerapp.components.RoundedButton
import com.example.readerapp.data.Resource
import com.example.readerapp.model.Item
import com.example.readerapp.model.MBook
import com.example.readerapp.navigation.ReaderScreens
import com.example.readerapp.screens.home.HorizontalScrollableComponent
import com.example.readerapp.screens.search.ReaderSearchScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun ReaderDetailsScreen(navController: NavController, bookId: String, viewModel: DetailsViewModel = hiltViewModel()) {
    Scaffold(topBar = {
        ReaderAppBar(title = "Book Details", navController = navController, icon = Icons.Default.ArrowBack, showProfile = false){
            navController.navigate(ReaderScreens.SearchScreen.name)
        }
    }) {
        Surface(modifier = Modifier
            .padding(3.dp)
            .fillMaxSize()) {
            Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {

                val bookInfo = produceState<Resource<Item>>(initialValue = Resource.Loading()){
                    value = viewModel.getBookInfo(bookId)
                }.value

                if(bookInfo.data == null){
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        LinearProgressIndicator()
                        Text(text = "Loading...")
                    }
                }else{
                    ShowBookDetails(bookInfo, navController)
                }
            }
        }
    }

}

@Composable
fun ShowBookDetails(
    bookInfo: Resource<Item>,
    navController: NavController
) {
    val imageUrl: String =
        if (bookInfo.data?.volumeInfo!!.imageLinks.smallThumbnail.isEmpty()) "http://books.google.com/books/content?id=JGH0DwAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs=api"
        else {
            bookInfo.data?.volumeInfo!!.imageLinks.smallThumbnail
        }

    Surface(modifier = Modifier
        .padding(16.dp)
        .height(100.dp)
        .width(100.dp),
        shape = CircleShape,
        elevation = 4.dp,
        border = BorderStroke(0.5.dp, Color.LightGray)) {
        Image(painter = rememberImagePainter(data = imageUrl),
            contentDescription = "Book Photo",
            contentScale = ContentScale.FillHeight)
    }

    Column(modifier = Modifier
        .padding(4.dp)
        .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Book Title: ${bookInfo.data?.volumeInfo!!.title}",
            fontWeight = FontWeight.Bold)
        Text(text = "Authors: ${bookInfo.data?.volumeInfo!!.authors}", fontWeight = FontWeight.Thin)
        Text(text = "Page Count: ${bookInfo.data?.volumeInfo!!.pageCount}",
            fontWeight = FontWeight.Thin)
        Text(text = "Categories: ${bookInfo.data?.volumeInfo!!.categories}",
            fontWeight = FontWeight.Thin,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis)
        Text(text = "Published: ${bookInfo.data?.volumeInfo!!.publishedDate}",
            fontWeight = FontWeight.Thin)

        val localDims = LocalContext.current.resources.displayMetrics
        Surface(modifier = Modifier
            .padding(8.dp)
            .height(localDims.heightPixels.dp.times(0.09f)),
            border = BorderStroke(1.dp, Color.LightGray)) {
            val scrollState = rememberScrollState()
            val cleanDescription = HtmlCompat.fromHtml(bookInfo.data?.volumeInfo!!.description, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            Column(modifier = Modifier
                .fillMaxWidth()
                .heightIn(200.dp)
                .verticalScroll(scrollState)) {
                Text(text = cleanDescription, fontWeight = FontWeight.Medium)
            }
        }

        Row(modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly) {

            RoundedButton(label = "Save", radius = 50) {
                val book = MBook(
                    title = bookInfo.data?.volumeInfo!!.title,
                    authors = bookInfo.data?.volumeInfo!!.authors.toString(),
                    description = bookInfo.data?.volumeInfo!!.description.toString(),
                    categories = bookInfo.data?.volumeInfo!!.categories.toString(),
                    notes = "",
                    photoUrl = bookInfo.data?.volumeInfo!!.imageLinks.thumbnail,
                    publishedDate = bookInfo.data?.volumeInfo!!.publishedDate,
                    pageCount = bookInfo.data?.volumeInfo!!.pageCount.toString(),
                    rating = 0.0,
                    googleBookId = bookInfo.data?.id!!,
                    userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                )
                saveToFirebase(book,navController)
            }

            Spacer(modifier = Modifier.padding(12.dp, 0.dp))

            RoundedButton(label = "Cancel", radius = 50) {
                navController.popBackStack()
            }
        }
    }
}

fun saveToFirebase(book: MBook, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val dbCollection = db.collection("books")

    if(book.toString().isNotEmpty()){
        dbCollection.add(book)
            .addOnSuccessListener { documentRef ->
                val docId = documentRef.id
                dbCollection.document(docId)
                    .update(hashMapOf("id" to docId) as Map<String, Any>)
                    .addOnCompleteListener{ task ->
                        if(task.isSuccessful){
                            navController.popBackStack()
                        }
                    }.addOnFailureListener {
                        Log.w("error", "saveToFirebase: Error Update doc", it)
                    }
            }
    }else{

    }
}
