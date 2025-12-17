package com.example.bookstore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
  import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// ==========================================
// 1. THE DATA MODEL 📦
// ==========================================
data class DogResponse(
    val message: String, // URL of the dog picture
    val status: String
)

// ==========================================
// 2. THE API SERVICE (The Phone Line 📞)
// ==========================================
interface DogApiService {
    @GET("breeds/image/random")
    suspend fun getRandomDog(): DogResponse
}

// ==========================================
// 3. THE RETROFIT BUILDER (The Phone 📱)
// ==========================================
object RetrofitInstance {
    val api: DogApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DogApiService::class.java)
    }
}

// ==========================================
// 4. THE VIEWMODEL (The Brain 🧠)
// ==========================================
class DogViewModel : ViewModel() {
    private val _dogImage = mutableStateOf("")
    val dogImage: State<String> = _dogImage

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        fetchNewDog()
    }

    fun fetchNewDog() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getRandomDog()
                _dogImage.value = response.message
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ==========================================
// 5. THE DOG UI 🎨 (No MainActivity here!)
// ==========================================
@Composable
fun DogScreen(viewModel: DogViewModel = viewModel()) {
    val imageUrl = viewModel.dogImage.value
    val isLoading = viewModel.isLoading.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🐶 Random Doggo! 🐾",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "A cute dog",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.fetchNewDog() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Show me another Doggo! 🦴", style = MaterialTheme.typography.titleMedium)
        }
    }
}
