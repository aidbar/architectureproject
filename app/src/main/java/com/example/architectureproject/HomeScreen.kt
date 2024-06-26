package com.example.architectureproject

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.example.architectureproject.profile.UserLifestyle
import com.example.architectureproject.tracking.TrackingDataGranularity
import com.example.architectureproject.tracking.TrackingEntry
import com.example.architectureproject.tracking.TrackingPeriod
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.launch
import com.example.architectureproject.ui.theme.*

enum class GraphOption {
    Weekly, Monthly, Yearly
}

class HomeScreenModel : ScreenModel {
    var selectedTab by mutableStateOf(GraphOption.Weekly)
    var data by mutableStateOf(listOf<TrackingEntry>())
    var loaded by mutableStateOf(false)
    val tasks = mutableStateListOf("Use public transport", "Sort waste", "Plant a tree", "Participate in a cleaning drive", "Reduce energy consumption", "Task 6", "Task 7", "Task 8", "Task 9", "Task 10")
    fun fetchData() {
        screenModelScope.launch {
            data = when (selectedTab) {
                GraphOption.Weekly -> TrackingDataGranularity.Day.let {
                    GreenTraceProviders.trackingProvider.getImpact(TrackingPeriod.pastWeeks().align(it), it)
                }
                GraphOption.Monthly -> TrackingDataGranularity.Month.let {
                    GreenTraceProviders.trackingProvider.getImpact(TrackingPeriod.pastYears().align(it), it)
                }
                GraphOption.Yearly -> TrackingDataGranularity.Year.let {
                    GreenTraceProviders.trackingProvider.getImpact(TrackingPeriod.pastYears(4).align(it), it)
                }
            }
            loaded = true
        }
    }
}

data class Recommendation(
    val task: String,
    val tag: String
)

class HomeScreen :Screen{
    private fun createValueFormatter(values: List<String>): AxisValueFormatter<AxisPosition.Horizontal.Bottom> {
        return AxisValueFormatter { position, _ ->
            val index = position.toInt()
            if (index in values.indices) {
                values[index]
            } else {
                ""
            }
        }
    }

    private fun getValueFormatter(option: GraphOption, data: List<TrackingEntry>): AxisValueFormatter<AxisPosition.Horizontal.Bottom> {
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val monthsOfYear = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        return createValueFormatter(when (option) {
            GraphOption.Weekly -> data.map { daysOfWeek[it.period.start.dayOfWeek.value - 1] }
            GraphOption.Monthly -> data.map { monthsOfYear[it.period.start.monthValue - 1] }
            GraphOption.Yearly -> data.map { it.period.start.year.toString() }
        })
    }

    private fun retrieveRecommendations(): List<Recommendation> {

        val recommendationList = mutableListOf(
            Recommendation("Buy second-hand clothing", "purchase"),
            Recommendation("Carpool with coworkers/friends", "commute"),
            Recommendation("Cook a meal at home rather than eating out", "meal"),
            Recommendation("Switch to eco-friendly cleaning products", "purchase"),
            Recommendation("Plan your week's meals to reduce food wastage", "meal"),
            Recommendation("Use ride-share instead of your next Uber trip", "commute"),
            Recommendation("Combine errands to reduce the number of trips", "commute"),
            Recommendation("Buy in bulk to reduce packaging waste", "purchase"),
            Recommendation("Upgrade to double or triple-pane windows for better insulation", "purchase"),
            Recommendation("Install a rainwater harvesting system for watering plants and gardens", "purchase"),
        )

        if(GreenTraceProviders.userProvider.hasUserLifestyle()) {
            val userLifestyle = GreenTraceProviders.userProvider.userLifestyle()

            if(userLifestyle.disabilities.isEmpty()) {
                recommendationList.add(Recommendation("Walk or bike to nearby places instead of driving", "commute"))
                recommendationList.add(Recommendation("Use electric scooter or bike reach nearby places", "commute"))
            }

            // locallySourcedFoodPreference
            if(userLifestyle.locallySourcedFoodPreference == UserLifestyle.Frequency.Always) {
                recommendationList.add(Recommendation("Explore community-supported agriculture programs in your area", ""))
            }

            if(userLifestyle.locallySourcedFoodPreference == UserLifestyle.Frequency.Sometimes) {
                recommendationList.add(Recommendation("Buy food products having local produce labels", "purchase"))
            }

            if(userLifestyle.locallySourcedFoodPreference == UserLifestyle.Frequency.Always || userLifestyle.locallySourcedFoodPreference == UserLifestyle.Frequency.Sometimes){
                recommendationList.add(Recommendation("Shop at local farmers' markets for fresh produce", "purchase"))
                recommendationList.add(Recommendation("Explore local bakeries for bread and pastries", "purchase"))
            } else {
                recommendationList.add(Recommendation("Consider researching the benefits of locally sourced foods", ""))
            }

            // dietaryRestrictions
            if(userLifestyle.diet == UserLifestyle.Diet.None || userLifestyle.diet == UserLifestyle.Diet.Pescatarian) {
                recommendationList.add(Recommendation("Visit local butcher shops for sustainably sourced meat", "purchase"))
            }

            if (userLifestyle.diet != UserLifestyle.Diet.Vegan) {
                recommendationList.add(Recommendation("Support local dairy farms for fresh dairy products", "purchase"))
            }

            // transportationMode
            if (userLifestyle.transportationPreference == UserLifestyle.TransportationMethod.PublicTransport) {
                recommendationList.add(Recommendation("Use public transportation", "commute"))
            }

            // sustainableShoppingPreference
            if (userLifestyle.sustainabilityInfluence == UserLifestyle.Frequency.Rarely || userLifestyle.sustainabilityInfluence == UserLifestyle.Frequency.Never) {
                recommendationList.add(Recommendation("Choose one or two sustainable products to try out each time you shop", ""))
                recommendationList.add(Recommendation("Consider researching brands that prioritize sustainability and ethical practices", ""))
            } else {
                recommendationList.add(Recommendation("Buy products with minimal packaging or packaging made from recyclable materials", "purchase"))
            }
        }

        return recommendationList.shuffled().take(6)
    }

    @Composable
    @Preview
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { HomeScreenModel() }
        val user = remember { GreenTraceProviders.userProvider.userInfo() }
        LaunchedEffect(model.selectedTab) { model.fetchData() }

        val randomRecommendations = retrieveRecommendations()

        if (!model.loaded) {
            LoadingScreen()
            return
        }

        val chartModel = ChartEntryModelProducer(
            model.data.mapIndexed { i, it -> entryOf(i, it.value) }
        )

        val valueFormatter = getValueFormatter(model.selectedTab, model.data)

        val chartTitle = when (model.selectedTab) {
            GraphOption.Weekly -> "Your Weekly carbon emission"
            GraphOption.Monthly -> "Your Monthly carbon emission"
            GraphOption.Yearly -> "Your Yearly carbon emission"
        }

        val datasetLineSpec = arrayListOf(
            LineChart.LineSpec(
                lineColor = Color.Black.toArgb(),
                lineBackgroundShader = DynamicShaders.fromBrush(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF5FA573).copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                            Color.White.copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_END)
                        )
                    )
                )
            )
        )

        val tabNavigator = LocalTabNavigator.current

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxSize()
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, top = 30.dp, end = 20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape) // Wrap the icon in a circle
                                    .border(2.dp, Color.Black, CircleShape)
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Rounded.Person), // Use Icons.Rounded.Person
                                    contentDescription = "Person Icon",
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Welcome ${user.name}!",
                                color = Color(0xFF5FA573),
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = chartTitle,
                            color = Color.Black,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .padding(top = 25.dp, start = 80.dp)
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navigator?.push(PastActivitiesScreen())
                                },
                            elevation = 17.dp
                        ) {
                            Chart(
                                chart = lineChart(
                                    lines = datasetLineSpec
                                ),
                                chartModelProducer = chartModel,
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(valueFormatter = valueFormatter),
                            )
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(
                                        color = if (model.selectedTab == GraphOption.Weekly) Color(
                                            0xFF5FA573
                                        ) else Color.White,
                                        shape = RoundedCornerShape(
                                            topStart = 8.dp,
                                            bottomStart = 8.dp
                                        )
                                    )
                                    .clickable {
                                        model.selectedTab = GraphOption.Weekly
                                    }
                                    .weight(1f)
                                    .padding(vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Weekly",
                                    modifier = Modifier
                                        .padding(start = 28.dp, end = 10.dp),
                                    color = Color.Black
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(color = Color.Black)
                                    .width(1.dp)
                                    .height(25.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(
                                        color = if (model.selectedTab == GraphOption.Monthly) Color(
                                            0xFF5FA573
                                        ) else Color.White
                                    )
                                    .clickable {
                                        model.selectedTab = GraphOption.Monthly
                                    }
                                    .weight(1f)
                                    .padding(vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Monthly",
                                    modifier = Modifier
                                        .padding(start = 25.dp, end = 25.dp),
                                    color = Color.Black
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(color = Color.Black)
                                    .width(1.dp)
                                    .height(25.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(
                                        color = if (model.selectedTab == GraphOption.Yearly) Color(
                                            0xFF5FA573
                                        ) else Color.White,
                                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                                    )
                                    .clickable {
                                        model.selectedTab = GraphOption.Yearly
                                    }
                                    .weight(1f)
                                    .padding(vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Yearly",
                                    modifier = Modifier
                                        .padding(start = 30.dp, end = 25.dp),
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(50.dp))
                    androidx.compose.material.Text(
                        text = "Tasks for you",
                        color = Color.Black,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 20.dp)
                    )
                }

                items(items = randomRecommendations, itemContent = { item ->
                    Card(
                        border = BorderStroke(3.dp, Color(0xFF5FA573)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp, start = 20.dp, end = 20.dp)
                            .shadow(30.dp),
//                            .clickable(onClick = {
//                                tabNavigator.current = NewActivityTab
//                            }),
                        elevation = 8.dp
                    ) {
                        androidx.compose.material.Text(
                            text = item.task,
                            color = Color.Black,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                })
            }
        }
    }
}