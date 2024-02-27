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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.architectureproject.profile.FirebaseUserProvider
import com.example.architectureproject.profile.UserProvider
import com.example.architectureproject.tracking.TrackingDataGranularity
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.TrackingEntry
import com.example.architectureproject.tracking.TrackingImpactProvider
import com.example.architectureproject.tracking.TrackingPeriod
import com.example.architectureproject.tracking.demo.DummyTrackingData
import com.example.architectureproject.tracking.demo.DummyTrackingDataProvider
import com.example.architectureproject.tracking.demo.DummyTrackingImpactProvider
import com.google.firebase.auth.FirebaseAuth
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
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf

enum class GraphOption {
    Weekly, Monthly, Yearly
}
class HomeScreen :Screen{
    val userProvider: UserProvider = FirebaseUserProvider()
    val provider: TrackingDataProvider = DummyTrackingDataProvider()
    val impactProvider: TrackingImpactProvider = DummyTrackingImpactProvider()

    init {
        // Load in dummy data
        DummyTrackingData(impactProvider).addTo(provider)
    }

    private fun getData(option: GraphOption) =
        when (option) {
            GraphOption.Weekly -> provider.getImpact(TrackingPeriod.pastWeeks(), TrackingDataGranularity.Day)
            GraphOption.Monthly -> provider.getImpact(TrackingPeriod.pastYears(), TrackingDataGranularity.Month)
            GraphOption.Yearly -> provider.getImpact(TrackingPeriod.pastYears(4), TrackingDataGranularity.Year)
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

    @Composable
    @Preview
    override fun Content() {
        val navigator = LocalNavigator.current
        val user = remember { userProvider.userInfo() }

        var selectedTab = remember {
            mutableStateOf(GraphOption.Weekly)
        }

        val tasks = listOf("Use public transport", "Sort waste", "Plant a tree", "Participate in a cleaning drive", "Reduce energy consumption", "Task 6", "Task 7", "Task 8", "Task 9", "Task 10")

        val data = getData(selectedTab.value)
        val chartModel = ChartEntryModelProducer(
            data.mapIndexed { i, it -> entryOf(i, it.value) }
        )

        val valueFormatter = getValueFormatter(selectedTab.value, data)

        val chartTitle = when (selectedTab.value) {
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
                            Color(0xFF009688).copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                            Color.White.copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_END)
                        )
                    )
                )
            )
        )

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
                            text = "Welcome ${user.name}!",  // text = "Welcome, "+auth.currentUser?.email
                            color = Color(0xFF009688),
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
                        modifier = Modifier.fillMaxWidth(),
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
                                    color = if (selectedTab.value == GraphOption.Weekly) Color(
                                        0xFF009688
                                    ) else Color.White,
                                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                                )
                                .weight(1f)
                                .padding(vertical = 5.dp)
                        ) {
                            Text(
                                text = "Weekly",
                                modifier = Modifier
                                    .clickable {
                                        selectedTab.value = GraphOption.Weekly
                                    }
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
                                    color = if (selectedTab.value == GraphOption.Monthly) Color(
                                        0xFF009688
                                    ) else Color.White
                                )
                                .weight(1f)
                                .padding(vertical = 5.dp)
                        ) {
                            Text(
                                text = "Monthly",
                                modifier = Modifier
                                    .clickable {
                                        selectedTab.value = GraphOption.Monthly
                                    }
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
                                    color = if (selectedTab.value == GraphOption.Yearly) Color(
                                        0xFF009688
                                    ) else Color.White,
                                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                                )
                                .weight(1f)
                                .padding(vertical = 5.dp)
                        ) {
                            Text(
                                text = "Yearly",
                                modifier = Modifier
                                    .clickable {
                                        selectedTab.value = GraphOption.Yearly
                                    }
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

            items(items = tasks, itemContent = { item ->
                Card(
                    border = BorderStroke(3.dp, Color(0xFF009688)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp, start = 20.dp, end = 20.dp)
                        .shadow(30.dp),
                    elevation = 8.dp
                ) {
                    androidx.compose.material.Text(
                        text = item,
                        color = Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            })
        }
    }
}

fun createValueFormatter(values: List<String>): AxisValueFormatter<AxisPosition.Horizontal.Bottom> {
    return AxisValueFormatter<AxisPosition.Horizontal.Bottom> { position, _ ->
        val index = position.toInt()
        if (index in values.indices) {
            values[index]
        } else {
            ""
        }
    }
}