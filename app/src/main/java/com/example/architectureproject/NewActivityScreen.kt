@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.architectureproject

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.example.architectureproject.ui.theme.Green40
import com.example.architectureproject.ui.theme.GreenGrey40
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
        var isRecurring by remember { mutableStateOf(false) }
        var recurrenceFrequency by remember { mutableStateOf("") }
        var recurrenceIntervalText by remember { mutableStateOf("1") } // Use a String state to hold the text input
        var isMealRecurring by remember { mutableStateOf(false) }
        var isCommuteRecurring by remember { mutableStateOf(false) }
        var isPurchaseRecurring by remember { mutableStateOf(false) }

        var selectedTabIndex by remember { mutableStateOf(0) } // Track selected tab index

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "Create a New Activity", style = MaterialTheme.typography.headlineMedium)

            var tabIndex by remember { mutableStateOf(0) } // State variable for tracking selected tab
            val tabs = listOf("Meal", "Commute", "Purchase") // Define tab titles

            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title, fontSize = 16.sp) }, // Apply text styling
                        selected = tabIndex == index,
                        onClick = { tabIndex = index }
                    )

                }
            }
            when (tabIndex) {
                0 -> Column() {
                    MealSection(foodType) { foodType = it }
                    RecurrenceOption("Meal", isMealRecurring, { isMealRecurring = it })
                }
                1 -> Column() {
                    CommuteSection(
                        transportationMode,
                        departure,
                        destination,
                        stops
                    ) { stops.add("") }
                    RecurrenceOption("Commute", isCommuteRecurring, { isCommuteRecurring = it })
                }
                2 -> Column() {
                    PurchaseSection(shoppingMethod) { shoppingMethod = it }
                    RecurrenceOption(
                        "Purchase",
                        isPurchaseRecurring,
                        { isPurchaseRecurring = it })
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            // Common buttons for all tabs
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
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
                        isMealRecurring = false
                        isCommuteRecurring = false
                        isPurchaseRecurring = false
                        recurrenceFrequency = ""
                        recurrenceIntervalText = "1"
                        // Implement your logic for saving the activity here
                    }, colors = ButtonDefaults.buttonColors(Green40)
                ) {
                    Text("Save Activity")
                }
                DatePickerButton(context, date) { selectedDate -> date = selectedDate }
                TimePickerButton(context, time) { selectedTime -> time = selectedTime }
            }
        }
    }

}

@Composable
fun ActivityButton(text: String, selectedActivity: String, onClick: (String) -> Unit) {
    val isSelected = text == selectedActivity
    Button(
        onClick = { onClick(text) }, colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Green40 else GreenGrey40
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

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(value = selectedFoodType,
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
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            foodTypes.forEach { type ->
                DropdownMenuItem(text = { Text(type) }, onClick = {
                    selectedFoodType = type
                    onFoodTypeChange(type)
                    expanded = false
                })
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

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(value = selectedMode,
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
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            transportationModes.forEach { type ->
                DropdownMenuItem(text = { Text(type) }, onClick = {
                    selectedMode = type
                    expanded = false
                })
            }
        }
    }
    OutlinedTextField(value = departure,
        onValueChange = { departure = it },
        label = { Text("Departure") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(value = destination,
        onValueChange = { destination = it },
        label = { Text("Destination") },
        modifier = Modifier.fillMaxWidth()
    )

    stops.forEachIndexed { index, stop ->
        var stopValue by remember { mutableStateOf(stop) }
        OutlinedTextField(value = stopValue, onValueChange = { updatedStop ->
            stops[index] = updatedStop
            stopValue = updatedStop
        }, label = { Text("Stop ${index + 1}") }, modifier = Modifier.fillMaxWidth()
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

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(value = selectedShoppingType,
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
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            shoppingType.forEach { type ->
                DropdownMenuItem(text = { Text(type) }, onClick = {
                    selectedShoppingType = type
                    onShoppingMethodChange(type)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun DatePickerButton(context: Context, date: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
    OutlinedIconButton(onClick = { showDatePicker(context, date, onDateSelected) }) {
        Icon(Icons.Default.DateRange, contentDescription = "date", tint = Green40)
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
    context: Context, currentDate: LocalDate, onDateSelected: (LocalDate) -> Unit
) {
    DatePickerDialog(context, { _, year, month, dayOfMonth ->
        onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
    }, currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth).show()
}

private fun showTimePicker(
    context: Context, currentTime: LocalTime, onTimeSelected: (LocalTime) -> Unit
) {
    TimePickerDialog(context, { _, hourOfDay, minute ->
        onTimeSelected(LocalTime.of(hourOfDay, minute))
    }, currentTime.hour, currentTime.minute, true).show()
}

@Composable
fun RecurrenceOption(
    activityType: String, isRecurring: Boolean, onRecurringChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var recurrenceFrequency by remember { mutableStateOf("") }
    var recurrenceIntervalText by remember { mutableStateOf("1") }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$activityType is recurring:", modifier = Modifier.padding(end = 8.dp))
            Switch(checked = isRecurring, onCheckedChange = { isChecked ->
                onRecurringChange(isChecked)
                if (!isChecked) {
                    recurrenceIntervalText = "1"
                }
            })
        }
        if (isRecurring) {
            Row(Modifier.padding(vertical = 8.dp)) {
                Text(
                    "Frequency: ", modifier = Modifier.align(alignment = Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Corrected call to DropdownMenu with appropriate parameters
                DropdownMenu(selectedFrequency = recurrenceFrequency,
                    onFrequencyChange = { newFrequency -> recurrenceFrequency = newFrequency })
            }
            Row {
                Text(
                    "Every (n) days/weeks: ",
                    modifier = Modifier.align(alignment = Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = recurrenceIntervalText,
                    onValueChange = { newValue ->
                        recurrenceIntervalText = newValue.filter { it.isDigit() }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}


@Composable
fun DropdownMenu(selectedFrequency: String, onFrequencyChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val frequencies = listOf("Daily", "Weekly")
    var selected by remember { mutableStateOf(selectedFrequency) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(value = selected,
            onValueChange = { },
            readOnly = true,
            label = { Text("Recurrence Frequency") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            frequencies.forEach { frequency ->
                DropdownMenuItem(text = { Text(frequency) }, onClick = {
                    selected = frequency
                    onFrequencyChange(frequency)
                    expanded = false
                })
            }
        }
    }
}
