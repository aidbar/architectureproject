@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.yourapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NewActivityScreen : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val keyboardController = LocalSoftwareKeyboardController.current
        var activityType by remember { mutableStateOf("") }
        var date by remember { mutableStateOf(LocalDate.now()) }
        var time by remember { mutableStateOf(LocalTime.now()) }
        var foodType by remember { mutableStateOf("") }
        var transportationMode by remember { mutableStateOf("") }
        var departure by remember { mutableStateOf("") }
        var destination by remember { mutableStateOf("") }
        val stops = remember { mutableStateListOf("") }
        var plasticBagUsed by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Create a New Activity", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            ActivityTypeInput(activityType = activityType) { selectedType ->
                // Converts the input to lowercase for comparison and stores it capitalized
                activityType = selectedType.capitalize()
            }

            when (activityType.lowercase()) { // Compares in lowercase
                "meal" -> MealSection(foodType) { newFoodType -> foodType = newFoodType }
                "commute" -> CommuteSection(transportationMode, departure, destination, stops) { stops.add("") }
                "purchase" -> PurchaseSection(plasticBagUsed) { newPlasticBagUsed -> plasticBagUsed = newPlasticBagUsed }
            }

            DatePickerButton(context, date) { selectedDate -> date = selectedDate }
            TimePickerButton(context, time) { selectedTime -> time = selectedTime }

            Button(
                onClick = {
                    // Resets all input fields
                    activityType = ""
                    date = LocalDate.now()
                    time = LocalTime.now()
                    foodType = ""
                    transportationMode = ""
                    departure = ""
                    destination = ""
                    stops.clear()
                    plasticBagUsed = false
                    keyboardController?.hide()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Save Activity")
            }
        }
    }
}

@Composable
fun ActivityTypeInput(activityType: String, onActivityTypeChange: (String) -> Unit) {
    OutlinedTextField(
        value = activityType,
        onValueChange = onActivityTypeChange,
        label = { Text("Activity Type") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun MealSection(foodType: String, onFoodTypeChange: (String) -> Unit) {
    OutlinedTextField(
        value = foodType,
        onValueChange = onFoodTypeChange,
        label = { Text("Type of food consumed") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CommuteSection(
    initialTransportationMode: String,
    initialDeparture: String,
    initialDestination: String,
    stops: MutableList<String>,
    onAddStop: () -> Unit
) {
    var transportationMode by remember { mutableStateOf(initialTransportationMode) }
    var departure by remember { mutableStateOf(initialDeparture) }
    var destination by remember { mutableStateOf(initialDestination) }

    OutlinedTextField(
        value = transportationMode,
        onValueChange = { transportationMode = it },
        label = { Text("Transportation Mode") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = departure,
        onValueChange = { departure = it },
        label = { Text("Departure") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = destination,
        onValueChange = { destination = it },
        label = { Text("Destination") },
        modifier = Modifier.fillMaxWidth()
    )

    stops.forEachIndexed { index, stop ->
        var stopValue by remember { mutableStateOf(stop) }
        OutlinedTextField(
            value = stopValue,
            onValueChange = { updatedStop ->
                stops[index] = updatedStop
                stopValue = updatedStop
            },
            label = { Text("Stop ${index + 1}") },
            modifier = Modifier.fillMaxWidth()
        )
    }

    Button(onClick = onAddStop) {
        Text("Add Stop")
    }
}

@Composable
fun PurchaseSection(plasticBagUsed: Boolean, onPlasticBagUsedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Any plastic bag used? ")
        Switch(checked = plasticBagUsed, onCheckedChange = onPlasticBagUsedChange)
    }
}

@Composable
fun DatePickerButton(context: Context, date: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
    Button(onClick = { showDatePicker(context, date, onDateSelected) }) {
        Text("Select Date: ${date.format(dateFormatter)}")
    }
}

@Composable
fun TimePickerButton(context: Context, time: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    Button(onClick = { showTimePicker(context, time, onTimeSelected) }) {
        Text("Select Time: ${time.format(timeFormatter)}")
    }
}

private fun showDatePicker(context: Context, currentDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    DatePickerDialog(context, { _, year, month, dayOfMonth ->
        onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
    }, currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth).show()
}

private fun showTimePicker(context: Context, currentTime: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
    TimePickerDialog(context, { _, hourOfDay, minute ->
        onTimeSelected(LocalTime.of(hourOfDay, minute))
    }, currentTime.hour, currentTime.minute, true).show()
}
