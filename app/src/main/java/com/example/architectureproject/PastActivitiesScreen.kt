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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

data class Activity(
    val name: String,
    val date: String,
    val carbonEmission: String
)

// Sample activities for each month
val activitiesJanuary = listOf(
    Activity( "Transportation", "January 5, 2024", "2.5 kg"),
    Activity("Purchase", "January 12, 2024", "1.2 kg"),
    Activity("Meal", "January 20, 2024", "0.8 kg")
)

val activitiesFebruary = listOf(
    Activity( "Transportation", "February 3, 2024", "2.7 kg"),
    Activity( "Purchase", "February 15, 2024", "1.4 kg"),
    Activity("Meal", "February 22, 2024", "0.9 kg")
)
class PastActivitiesScreen:Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        LazyColumn(modifier = Modifier.padding(16.dp)) {

            item{
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator?.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                    }
                }
            }
            item{
                Row(modifier = Modifier.padding(start = 10.dp)){
                    Text("Activity History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            item { Section(month = "February 2024", activities = activitiesFebruary) }
            item { Section(month = "January 2024", activities = activitiesJanuary) }
        }
    }
}

@Composable
fun Section(month: String, activities: List<Activity>) {
    Column(modifier = Modifier.padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp).background(Color(0f,0.59f,0.53f,1f), RoundedCornerShape(10.dp)).padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
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
fun ActivityItem(activity: Activity) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 8.dp, top = 10.dp).drawBehind{
                val strokeWidth = 1.dp.toPx()
                val yOffset = size.height - strokeWidth / 2
                drawLine(
                    color = Color(0f,0.59f,0.53f,1f),
                    start = Offset(0f, yOffset+20),
                    end = Offset(size.width, yOffset+20),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        ActivityIcon(activity = activity.name)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(text = activity.name, style = MaterialTheme.typography.titleMedium)
            Text(text = activity.date, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.weight(1f))
        Text(text = activity.carbonEmission, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ActivityIcon(activity: String){
    if (activity == "Meal"){
        return Image(painter = painterResource(id = R.drawable.baseline_set_meal_24), contentDescription = activity, modifier = Modifier.size(30.dp))
    }else if (activity == "Transportation"){
        return Image(painter = painterResource(id = R.drawable.baseline_directions_bus_24), contentDescription = activity, modifier = Modifier.size(30.dp))
    }else if (activity == "Purchase"){
        return Image(painter = painterResource(id = R.drawable.baseline_shopping_cart_24), contentDescription = activity, modifier = Modifier.size(30.dp))
    }
}