@file:OptIn(ExperimentalLayoutApi::class)

package com.example.architectureproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

class QuizScreenModel(private val selectedQuiz: Quiz) : ScreenModel {
    var userResponses by mutableStateOf(List(selectedQuiz.questions.size) { -1 })
    var score by mutableIntStateOf(-1)
    var showQuizScore by mutableStateOf(false)

    fun calculateScore() {
        var currentScore = 0
        for (i in selectedQuiz.questions.indices) {
            if (userResponses[i] == selectedQuiz.questions[i].correctAnswerIndex) {
                currentScore += 1
            }
        }

        score = (currentScore * 100 / selectedQuiz.questions.size)
    }
}

class QuizScreen(private val selectedQuiz: Quiz) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { QuizScreenModel(selectedQuiz) }
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = {navigator?.pop()}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedQuiz.title, style = MaterialTheme.typography.headlineSmall)
                }
            }

            item {
                Text(selectedQuiz.description, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 12.dp))
            }

            items(selectedQuiz.questions.size) { index ->
                val question = selectedQuiz.questions[index]

                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(text = "${index + 1}. ${question.text}")

                    question.options.forEachIndexed { optionIndex, option ->
                        val isOptionSelected = model.userResponses[index] == optionIndex

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isOptionSelected,
                                onClick = {
                                    model.userResponses = model.userResponses.toMutableList().also {
                                        it[index] = if (isOptionSelected) -1 else optionIndex
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option)
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            model.calculateScore()
                            model.showQuizScore = true
                        },
                    ) {
                        Text("Check Score")
                    }
                }
            }
        }

        if (model.showQuizScore) {
            Dialog(onDismissRequest = {
                model.showQuizScore = false
            }) {
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(150.dp)
                        .padding(16.dp)
                        .background(Color.White, MaterialTheme.shapes.medium)
                ) {
                    Column (
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row (
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Your Score: ${model.score}%", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { model.showQuizScore = false }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5F)),
                contentAlignment = Alignment.Center
            ) {
                // This empty box is necessary to prevent clicks from passing through
                Box(modifier = Modifier.size(1.dp))
            }
        }
    }
}
