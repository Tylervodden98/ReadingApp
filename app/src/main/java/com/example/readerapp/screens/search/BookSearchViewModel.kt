package com.example.readerapp.screens.search

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readerapp.data.DataOrException
import com.example.readerapp.data.Resource
import com.example.readerapp.model.Item
import com.example.readerapp.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.http.Query
import javax.inject.Inject

@HiltViewModel
class BookSearchViewModel @Inject constructor(private val repository: BookRepository): ViewModel() {
    var list: List<Item> by mutableStateOf(listOf())
    var isLoading: Boolean by mutableStateOf(true)
    init {
        loadBooks()
    }

    private fun loadBooks(){
        searchBooks("flutter")
    }

    fun searchBooks(query: String){
        viewModelScope.launch {
            isLoading = true
            if(query.isEmpty()){
                return@launch
            }
            try {
                when(val response = repository.getBooks(query)){
                    is Resource.Success -> {
                        list = response.data!!
                        isLoading = false
                    }
                    is Resource.Error -> {
                        Log.d("Network", "searchBooks: Failed getting books")
                        isLoading = false
                    }
                    else -> {isLoading = false}
                }
            }catch (exception: Exception){
                isLoading = false
                Log.d("Network", "searchBooks: ${exception.message.toString()}")
            }
        }
    }
}