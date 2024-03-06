@file:OptIn(ExperimentalLayoutApi::class)

package com.example.architectureproject

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

val backgrounUrl: String = "https://source.unsplash.com/random/1920x1080/?landscape"

data class Article(
    val title: String,
    val url: String,
//    val backgroundUrl: String
)

data class Quiz(
    val title: String,
    val description: String,
//    val backgroundUrl: String
)

class LearnScreen : Screen {

    private val articles = listOf(
        Article("40 Ways to Be More Eco Friendly in 2024", "https://www.greenmatch.co.uk/blog/how-to-be-more-eco-friendly"),
        Article("The Top 9 Environmentally Friendly Tips to Save the Planet", "https://justenergy.com/blog/the-top-9-environmentally-friendly-tips-to-save-the-planet/"),
        Article("Eco Friendly: Fundamentals for Decoding Sustainable Choices", "https://www.graygroupintl.com/blog/eco-friendly")
    )

    private val quizzes = listOf(
        Quiz("Water Conservation Quiz", "Test your knowledge on saving water."),
        Quiz("Plastic-Free Life", "How well do you know about reducing plastic usage?"),
                Quiz("Carbon Footprint Quiz", "See if you can identify which choices yield the smallest carbon footprint."),
    Quiz("Recycling 101 Quiz", "Do you actually know what everyday items are recyclable?")
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)){
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

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    articles.forEach { article ->
                        Card(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .width(200.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                    context.startActivity(intent)
                                },
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 6.dp
                            ),
                            colors = CardDefaults.cardColors(containerColor = Color(243,244,248))
                        ) {
                            Box(modifier = Modifier.height(150.dp)){
                                Image(painter = rememberAsyncImagePainter(model = backgrounUrl), contentDescription = "background", modifier = Modifier
                                    .fillMaxSize()
                                    .clip(
                                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                    ), contentScale = ContentScale.Crop);
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = article.title, style = MaterialTheme.typography.titleSmall, color = Color(33,36,39))
                            }
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