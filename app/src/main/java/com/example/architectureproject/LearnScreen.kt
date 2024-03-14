@file:OptIn(ExperimentalLayoutApi::class)

package com.example.architectureproject

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

data class Article(
    val title: String,
    val url: String,
    val imageUrl: String
) : java.io.Serializable

data class Quiz(
    val title: String,
    val description: String,
//    val backgroundUrl: String
) : java.io.Serializable

class LearnScreen : Screen {

    private val quizzes = listOf(
        Quiz("Water Conservation Quiz", "Test your knowledge on saving water."),
        Quiz("Plastic-Free Life", "How well do you know about reducing plastic usage?"),
        Quiz("Carbon Footprint Quiz", "See if you can identify which choices yield the smallest carbon footprint."),
        Quiz("Recycling 101 Quiz", "Do you actually know what everyday items are recyclable?")
    )

    @Composable
    override fun Content() {
        val articles = remember { mutableStateOf<List<Article>?>(emptyList()) }

        LaunchedEffect(Unit) {
            fetchArticles(articles)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)){
                    Text(text = "Learning Hub", style = MaterialTheme.typography.titleLarge)
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)){
                    Text(text = "Articles", style = MaterialTheme.typography.titleMedium)
                    Text(text = "See All >", style = MaterialTheme.typography.bodySmall)
                }
            }

            articles.value?.let {
                item {
                    LazyRow {
                        items(it.size) { index ->
                            ArticleCard(article = articles.value!![index])
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)){
                    Text(text = "Quizzes", style = MaterialTheme.typography.titleMedium)
                    Text(text = "See All >", style = MaterialTheme.typography.bodySmall)
                }
            }

            item{
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(400.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    items(quizzes.size) {index ->
                        QuizCard(quiz = quizzes[index])
                    }
                }
            }
        }
    }

    private suspend fun fetchArticles(articles: MutableState<List<Article>?>) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            val query = "green%20living"
            val url = "https://newsdata.io/api/1/news?apikey=pub_9776cec1c68b5a1afc4c88f91a0207adacc5&q=$query&language=en&category=education,environment"
            val request = Request.Builder()
                .url(url)
                .build()

            try {
                val response: Response = client.newCall(request).execute()
                val jsonData: String? = response.body?.string()

                if (response.isSuccessful && jsonData != null) {
                    val jsonObject = JSONObject(jsonData)
                    val jsonArray = jsonObject.getJSONArray("results")
                    val fetchedArticles = mutableListOf<Article>()

                    for (i in 0 until jsonArray.length()) {
                        val articleObject = jsonArray.getJSONObject(i)
                        val title = articleObject.getString("title")
                        val url = articleObject.getString("link")
                        val imageUrl = articleObject.getString("image_url")

                        val article = Article(title, url, imageUrl)
                        fetchedArticles.add(article)
                    }

                    articles.value = fetchedArticles
                    Log.d("Articles", "Successfully fetched ${fetchedArticles.size} articles")
                } else {
                    Log.e("FetchArticles", "Error in fetching articles: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("FetchArticles", "Exception occurred: ${e.message}", e)
            }
        }
    }
}

@Composable
fun ArticleCard(article: Article) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(300.dp)
            .height(230.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = rememberAsyncImagePainter(article.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.LightGray)
                    .align(Alignment.BottomStart)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = article.title,
                        color = Color(33, 36, 39)
                    )
                }
            }
        }
    }
}

@Composable
fun QuizCard(quiz: Quiz) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = quiz.title, style = MaterialTheme.typography.titleSmall)
            Text(text = quiz.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}