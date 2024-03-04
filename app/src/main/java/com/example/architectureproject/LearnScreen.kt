package com.example.architectureproject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import com.google.firebase.auth.FirebaseAuth

class LearnScreen : Screen {
    //var auth = FirebaseAuth.getInstance()

    @Composable
    @Preview
    override fun Content() {
        LearnScreenContent()
    }
}

@Composable
fun LearnScreenContent() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Display the introductory text
        Text(
            text = "Learn more about carbon tracking and the environment",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display the links to quizzes
        Text(
            text = "Quizzes:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "1. Carbon Footprint Quiz - https://www.conservation.org/quizzes/carbon-footprint-quiz",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "2. Recycling 101 Quiz - https://recyclingpartnership.org/communitiesforrecycling/recycling-101-quiz/",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display the links to articles
        Text(
            text = "Info/Articles:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "1. What on Earth Newsletter - https://www.cbc.ca/news/science/what-on-earth-newsletter-home-green-living-nuclear-power-1.5075658",
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
