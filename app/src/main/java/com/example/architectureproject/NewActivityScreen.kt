@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.architectureproject

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        var shoppingMethod by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Create a New Activity", style = MaterialTheme.typography.headlineMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityButton("Meal", activityType) { activityType = it }
                ActivityButton("Commute", activityType) { activityType = it }
                ActivityButton("Purchase", activityType) { activityType = it }
            }

            when (activityType) {
                "Meal" -> MealSection(foodType) { newFoodType -> foodType = newFoodType }
                "Commute" -> CommuteSection(
                    transportationMode,
                    departure,
                    destination,
                    stops
                ) { stops.add("") }

                "Purchase" -> PurchaseSection(shoppingMethod) { newShoppingMethod ->
                    shoppingMethod = newShoppingMethod
                }
            }
            if (activityType.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp,
                        Alignment.CenterHorizontally
                    )// This will space the buttons evenly
                ) {
                    Button(
                        onClick = {
                            activityType = ""
                            date = LocalDate.now()
                            time = LocalTime.now()
                            foodType = ""
                            transportationMode = ""
                            departure = ""
                            destination = ""
                            stops.clear()
                            shoppingMethod = ""
                            keyboardController?.hide()
                        }
                    ) {
                        Text("Save Activity")
                    }
                    DatePickerButton(context, date) { selectedDate -> date = selectedDate }
                    TimePickerButton(context, time) { selectedTime -> time = selectedTime }
                }
            }
        }
    }
}

@Composable
fun ActivityButton(text: String, selectedActivity: String, onClick: (String) -> Unit) {
    val isSelected = text == selectedActivity
    Button(
        onClick = { onClick(text) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.Gray else Color.LightGray
        )
    ) {
        Text(text)
    }
}

@Composable
fun MealSection(foodType: String, onFoodTypeChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val foodTypes = listOf("Meat", "Dairy", "Poultry", "Egg", "Fish", "Vegetable", "Fruit", "Grain")
    var selectedFoodType by remember { mutableStateOf(foodType) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedFoodType,
            onValueChange = { },
            readOnly = false,
            label = { Text("Type of food consumed") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            foodTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        selectedFoodType = type
                        onFoodTypeChange(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CommuteSection(
    initialTransportationMode: String,
    initialDeparture: String,
    initialDestination: String,
    stops: MutableList<String>,
    onAddStop: () -> Unit
) {
    var departure by remember { mutableStateOf(initialDeparture) }
    var destination by remember { mutableStateOf(initialDestination) }

    var expanded by remember { mutableStateOf(false) }
    val transportationModes = listOf("car", "bus", "walk", "bike", "train", "plane", "ferry", "LRT")
    var selectedMode by remember { mutableStateOf(initialTransportationMode) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedMode,
            onValueChange = { },
            readOnly = false,
            label = { Text("Type of transportation") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            transportationModes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        selectedMode = type
                        expanded = false
                    }
                )
            }
        }
    }
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

    OutlinedButton(onClick = onAddStop) {
        Text("Add Stop")
    }
}

@Composable
fun PurchaseSection(shoppingMethod: String, onShoppingMethodChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val shoppingType = listOf("In-Store", "Online", "SecondHand")
    var selectedShoppingType by remember { mutableStateOf(shoppingMethod) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedShoppingType,
            onValueChange = { },
            readOnly = false,
            label = { Text(text = "Select way of purchase") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            shoppingType.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        selectedShoppingType = type
                        onShoppingMethodChange(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DatePickerButton(context: Context, date: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
    OutlinedIconButton(onClick = { showDatePicker(context, date, onDateSelected) }) {
        Icon(Icons.Default.DateRange, contentDescription = "date")
    }
}

@Composable
fun TimePickerButton(context: Context, time: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    OutlinedButton(
        onClick = { showTimePicker(context, time, onTimeSelected) },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.typography.bodySmall.color
        ),
        modifier = Modifier.wrapContentWidth()
    ) {
        Text(time.format(timeFormatter))
    }
}

private fun showDatePicker(
    context: Context,
    currentDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    DatePickerDialog(context, { _, year, month, dayOfMonth ->
        onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
    }, currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth).show()
}

private fun showTimePicker(
    context: Context,
    currentTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    TimePickerDialog(context, { _, hourOfDay, minute ->
        onTimeSelected(LocalTime.of(hourOfDay, minute))
    }, currentTime.hour, currentTime.minute, true).show()
}
