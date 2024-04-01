@file:OptIn(ExperimentalLayoutApi::class)

package com.example.architectureproject

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.architectureproject.ui.theme.*

data class Article(
    val title: String,
    val url: String,
    val imageUrl: String
) : java.io.Serializable

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class Quiz(
    val title: String,
    val description: String,
    val questions: List<Question>
) : java.io.Serializable

class LearnScreen : Screen {

    // Reference: https://www.proprofs.com/quiz-school/story.php?title=water-conservation
    val QuizOneQuestions = listOf(
        Question("How much of the water on earth is available for people's everyday use?", listOf("10%", "More than 25%", "Less than 1%", "25%"), 2),
        Question("Which of the following uses less water?", listOf("Taking a 5 minute shower", "Taking a 5 minute bath", "Both use equal water"), 0),
        Question("In the average household, which of the following wastes the most water?", listOf("Running water while washing dishes", "Running water while brushing teeth", "Leaky toilet", "Taking a long shower"), 2),
        Question("Why do we need to conserve water?", listOf("There is a lot of water", "It is an limited resource", "It is a unlimited resource"), 1),
        Question("Which of the following is a way of conserving water?", listOf("Leave the tap running while brushing teeth", "Use a hose to wash the car", "Leave the shower running while soaping", "Use the washing machine at full load"), 3)
    )

    // Reference: https://www.euronews.com/green/2019/07/02/quiz-how-plastic-free-are-you
    val QuizTwoQuestions = listOf(
        Question("You avoid buying plastic bottles", listOf("True", "False"), 0),
        Question("You avoid take away food in plastic containers", listOf("True", "False"), 0),
        Question("Do you choose bar soaps over liquid soap stored in plastic containers", listOf("True", "False"), 0),
        Question("Do you refuse straws with take-away and eat-in drinks", listOf("True", "False"), 0),
        Question("You buy dried foods (e.g nuts, grains and spices) in bulk", listOf("True", "False"), 0)
    )

    // Reference: https://recyclesmartma.org/quiz-1-0/
    val QuizThreeQuestions = listOf(
        Question("Lids & caps should be removed before recycling", listOf("True", "False"), 1),
        Question("Any plastic item that has a number in a triangle can be recycled", listOf("True", "False"), 1),
        Question("Which one of the below items doesn't belong in your household recycling bin?", listOf("Cardboard", "Plastic wrap", "Glass jars", "Metal cans", "Plastic bottles"), 1),
        Question("Once recyclables are collected and transported to the recycling sorting facility, they are:", listOf("Hand-sorted to remove problematic materials", "Mechanically sorted with magnets, screens, and optical sorters", "Compressed into large bricks called bales and sold to companies that clean and convert them into new products", "All of the above"), 3),
        Question("It’s ok to bag my recyclables before putting them in the bin", listOf("True", "False"), 1),
    )

    // Reference: https://www.conservation.org/quizzes/carbon-footprint-quiz
    val QuizFourQuestions = listOf(
        Question("Whether you exercise first thing in the morning or later in the evening, which option below would have the smallest impact on the environment?", listOf("Go to the gym", "Run on a treadmill or ride a stationary bike at home", "Exercise outdoors", "Put on a short exercise video"), 2),
        Question("Keeping the environment in mind, what should you have for breakfast?", listOf("Toast with butter", "Fresh grapefruit", "Scrambled eggs", "A bowl of cereal"), 1),
        Question("Of the clothing options listed below, which fabric yielded the smallest carbon footprint during the production process?", listOf("Linen shirt or blouse", "Polyester shirt or blouse", "Cotton shirt or blouse", "Wool sweater"), 0),
        Question("What’s the least carbon-intensive way to travel?", listOf("Drive a car", "Take the bus", "Rent an electric scooter", "Rent a bike"), 1),
        Question("What's the most carbon-friendly way to wash your dirty dishes?", listOf("Hand-wash them with the tap running", "Pre-rinse them under the tap then put them in the dishwasher", "Hand-wash them (without the tap running)", "Put them in the dishwasher without pre-rinsing"), 2)
    )

    private val quizzes = listOf(
        Quiz("Water Conservation Quiz", "Test your knowledge on saving water.", QuizOneQuestions),
        Quiz("Plastic-Free Life", "How plastic-free are you?", QuizTwoQuestions),
        Quiz("Recycling 101 Quiz", "Do you know what everyday items are recyclable?", QuizThreeQuestions),
        Quiz("Carbon Footprint Quiz", "See if you can identify which choices yield the smallest carbon footprint.", QuizFourQuestions)
    )

    @Composable
    override fun Content() {
        val articles = remember { mutableStateOf<List<Article>?>(emptyList()) }
        val navigator = LocalNavigator.current

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
                    Text(text = "Articles", style = MaterialTheme.typography.titleLarge)
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
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp, bottom = 8.dp, start = 4.dp)
                ){
                    Text(text = "Quizzes", style = MaterialTheme.typography.titleLarge)
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
                        QuizCard(quiz = quizzes[index]) {
                            navigator?.push(QuizScreen(selectedQuiz = quizzes[index]))
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchArticles(articles: MutableState<List<Article>?>) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            val query = "Eco-friendly"
            val url = "https://newsdata.io/api/1/news?apikey=pub_9776cec1c68b5a1afc4c88f91a0207adacc5&q=$query&language=en&category=education,environment,lifestyle"
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
                        val link = articleObject.getString("link")
                        val imageUrl = articleObject.getString("image_url")

                        val article = Article(title, link, imageUrl)
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
fun QuizCard(quiz: Quiz, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(160.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = quiz.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = quiz.description, style = MaterialTheme.typography.bodySmall)
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