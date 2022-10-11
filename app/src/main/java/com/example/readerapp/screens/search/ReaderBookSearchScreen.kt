package com.example.readerapp.screens.search

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection.Companion.In
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.readerapp.components.InputField
import com.example.readerapp.components.ReaderAppBar
import com.example.readerapp.data.Resource
import com.example.readerapp.model.Item
import com.example.readerapp.model.MBook
import com.example.readerapp.navigation.ReaderScreens
import java.io.Reader
import com.example.readerapp.screens.search.BookRow as BookRow


@Composable
fun ReaderSearchScreen(navController: NavController, viewModel: BookSearchViewModel = hiltViewModel()) {
    Scaffold(topBar = {
        ReaderAppBar(title = "Search Books", icon = Icons.Default.ArrowBack, navController = navController, showProfile = false){
            navController.navigate(ReaderScreens.ReaderHomeScreen.name)
        }
    }) {
        androidx.compose.material.Surface() {
            Column {
                SearchForm(modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                    viewModel = viewModel){ query ->
                    viewModel.searchBooks(query)
                }
                Spacer(modifier = Modifier.height(13.dp))

                BookList(navController = navController, viewModel)

            }
        }
    }
}

@Composable
fun BookList(navController: NavController, viewModel: BookSearchViewModel = hiltViewModel()) {
    val listOfBooks = viewModel.list
    if(viewModel.isLoading){
        LinearProgressIndicator()
    }else{
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)){
            items(items = listOfBooks){ book ->
                BookRow(book,navController)
            }
        }
    }
}


@Composable
fun BookRow(book: Item, navController: NavController) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .padding(4.dp)
        .clickable {
                   navController.navigate(ReaderScreens.DetailScreen.name + "/${book.id}")
        }, elevation = 4.dp, shape = RectangleShape) {
        Row() {
            val imageUrl: String = if(book.volumeInfo.imageLinks.smallThumbnail.isEmpty()) "http://books.google.com/books/content?id=JGH0DwAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs=api"
            else{
                book.volumeInfo.imageLinks.smallThumbnail
            }

            Image(painter = rememberImagePainter(data = imageUrl), contentDescription = "Android pic",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(70.dp)
                    .padding(4.dp), contentScale = ContentScale.FillHeight)
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(4.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
                Text(text = if (book.volumeInfo.title.isNotEmpty()) book.volumeInfo.title
                    else{"Null"}, fontSize = 16.sp, overflow = TextOverflow.Ellipsis)
                Text(text = if (book.volumeInfo.authors.toString().isNotEmpty()) book.volumeInfo.authors.toString()
                    else{"Null"}, fontSize = 12.sp, fontStyle = FontStyle.Italic,overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption)
                Text(text = "Date: ${book.volumeInfo.publishedDate}", fontSize = 12.sp, fontStyle = FontStyle.Italic,overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption)
                Text(text = "Categories: ${book.volumeInfo.categories}", fontSize = 12.sp, fontStyle = FontStyle.Italic,overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption)

            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchForm(
    modifier: Modifier = Modifier,
    viewModel: BookSearchViewModel,
    loading: Boolean = false,
    hint: String = "Search",
    onSearch: (String) -> Unit = {}
)
{
    val searchQueryState = rememberSaveable {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val valid = remember(searchQueryState.value){
        searchQueryState.value.trim().isNotEmpty()
    }

    Column() {
        OutlinedTextField(value = searchQueryState.value, onValueChange = {
            searchQueryState.value = it
        }, label = { Text(text = "Search") },
            singleLine = true,
            textStyle = TextStyle(fontSize = 18.sp, color = MaterialTheme.colors.onBackground),
            modifier = modifier
                .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            enabled = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions {
                if (!valid) return@KeyboardActions
                onSearch(searchQueryState.value.trim())
                searchQueryState.value = ""
                keyboardController?.hide()
            }
        )
        }
}