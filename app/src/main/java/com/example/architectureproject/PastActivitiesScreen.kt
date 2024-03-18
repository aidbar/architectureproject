package com.example.architectureproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.architectureproject.tracking.Meal
import com.example.architectureproject.tracking.Purchase
import com.example.architectureproject.tracking.TrackingActivity
import com.example.architectureproject.tracking.TrackingDataGranularity
import com.example.architectureproject.tracking.TrackingPeriod
import com.example.architectureproject.tracking.Transportation
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class PastActivitiesScreen:Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val data = remember { mutableStateListOf<Pair<TrackingPeriod, List<TrackingActivity>>>() }
        val scope = rememberCoroutineScope()
        var loading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) { loadMore(data); loading = false }
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator?.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return")
                    }
                }
            }

            item{
                Row(modifier = Modifier.padding(start = 10.dp)){
                    Text("Activity History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
            }

            items(data.size) { index ->
                val (period, activities) = data[index]
                Section(
                    month = "${period.start.month.name} ${period.start.year}",
                    activities = activities
                )
            }

            item {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        scope.launch { loading = true; loadMore(data); loading = false }
                    }, enabled = !loading) {
                        if (loading)
                            Text("Loading...")
                        else
                            Text("Load more")
                    }
                }
            }
        }
    }

    private suspend fun loadMore(
        data: SnapshotStateList<Pair<TrackingPeriod, List<TrackingActivity>>>
    ) {
        var activities = listOf<TrackingActivity>()
        // TODO: fix ugly hardcoded start range
        val lastPeriod = data.lastOrNull()?.first ?:
            TrackingPeriod.pastMonths().shiftPeriods(TrackingDataGranularity.Month, -12)

        var period = lastPeriod.shiftPeriods(TrackingDataGranularity.Month, 1)
        while (activities.isEmpty() && period.start <= ZonedDateTime.now()) {
            activities = GreenTraceProviders.trackingProvider!!.getActivities(period)
            period = period.shiftPeriods(TrackingDataGranularity.Month, 1)
        }

        if (activities.isEmpty()) return
        data.add(period to activities)
    }
}

@Composable
fun Section(month: String, activities: List<TrackingActivity>) {
    Column(modifier = Modifier.padding(8.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(Color(0f, 0.59f, 0.53f, 1f), RoundedCornerShape(10.dp))
            .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = month, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(text = "CO2:", style = MaterialTheme.typography.titleSmall, color = Color.White)
        }
        activities.forEach { activity ->
            ActivityItem(activity = activity)
        }
    }
}

@Composable
fun ActivityItem(activity: TrackingActivity) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 8.dp, top = 10.dp)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val yOffset = size.height - strokeWidth / 2
                drawLine(
                    color = Color(0f, 0.59f, 0.53f, 1f),
                    start = Offset(0f, yOffset + 20),
                    end = Offset(size.width, yOffset + 20),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        ActivityIcon(activity)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(text = activity.name, style = MaterialTheme.typography.titleMedium)
            Text(text = activity.date.toLocalDate().toString(), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.weight(1f))
        Text(text = activity.impact.toString(), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ActivityIcon(activity: TrackingActivity){
    when (activity) {
        is Meal -> {
            return Image(painter = painterResource(id = R.drawable.baseline_set_meal_24), contentDescription = "Meal", modifier = Modifier.size(30.dp))
        }

        is Transportation -> {
            return Image(painter = painterResource(id = R.drawable.baseline_directions_bus_24), contentDescription = "Transportation", modifier = Modifier.size(30.dp))
        }

        is Purchase -> {
            return Image(painter = painterResource(id = R.drawable.baseline_shopping_cart_24), contentDescription = "Purchase", modifier = Modifier.size(30.dp))
        }
    }
}