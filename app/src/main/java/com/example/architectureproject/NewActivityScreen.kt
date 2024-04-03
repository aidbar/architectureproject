@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.architectureproject

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.architectureproject.ui.theme.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import com.example.architectureproject.tracking.FirebaseTrackingDataProvider
import com.example.architectureproject.tracking.Meal
import com.example.architectureproject.tracking.Purchase
import com.example.architectureproject.tracking.TrackingActivity
import com.example.architectureproject.tracking.RecurrenceSchedule
import com.example.architectureproject.tracking.TrackingDataGranularity
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.Transportation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime

class NewActivityScreen : Screen {
    object TrackingService {
        val provider: TrackingDataProvider = FirebaseTrackingDataProvider()
    }
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
        val scrollState = rememberScrollState()
        var saveAttempted by remember { mutableStateOf(false) }

        var mealRecurrenceFrequency by remember { mutableStateOf("") }
        var mealRecurrenceIntervalText by remember { mutableStateOf("1") }
        var mealEndDate by remember { mutableStateOf<LocalDate?>(null) }

        // Define separate states for Commute
        var commuteRecurrenceFrequency by remember { mutableStateOf("") }
        var commuteRecurrenceIntervalText by remember { mutableStateOf("1") }
        var commuteEndDate by remember { mutableStateOf<LocalDate?>(null) }

        // Define separate states for Purchase
        var purchaseRecurrenceFrequency by remember { mutableStateOf("") }
        var purchaseRecurrenceIntervalText by remember { mutableStateOf("1") }
        var purchaseEndDate by remember { mutableStateOf<LocalDate?>(null) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Create a New Activity", style = MaterialTheme.typography.headlineMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityButton("Meal", activityType) { activityType = it }
                ActivityButton("Commute", activityType) { activityType = it }
                ActivityButton("Purchase", activityType) { activityType = it }
            }

            if (activityType.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp)) // Add some spacing for better UI layout

                // Handling specific sections and recurrence options based on activity type
                when (activityType) {
                    "Meal" -> {
                        MealSection(foodType) { foodType = it }
                        RecurrenceOption("Meal", isMealRecurring, { isMealRecurring = it }
                            , recurrenceFrequency = mealRecurrenceFrequency,
                            onRecurrenceFrequencyChange = { mealRecurrenceFrequency = it },
                            recurrenceIntervalText = mealRecurrenceIntervalText,
                            onRecurrenceIntervalChange = { mealRecurrenceIntervalText = it },
                            endDate = mealEndDate,
                            onEndDateChange = { mealEndDate = it })
                    }
                    "Commute" -> {
                        CommuteSection(    initialTransportationMode = transportationMode,
                            onTransportationModeChange = { newMode ->
                                transportationMode = newMode // Here we update the state in the parent
                            }, departure, destination, stops) { stops.add("") }
                        RecurrenceOption("Commute", isCommuteRecurring, { isCommuteRecurring = it }
                            , recurrenceFrequency = commuteRecurrenceFrequency,
                            onRecurrenceFrequencyChange = { commuteRecurrenceFrequency = it },
                            recurrenceIntervalText = commuteRecurrenceIntervalText,
                            onRecurrenceIntervalChange = { commuteRecurrenceIntervalText = it },
                            endDate = commuteEndDate,
                            onEndDateChange = { commuteEndDate = it })
                    }
                    "Purchase" -> {
                        PurchaseSection(shoppingMethod) { shoppingMethod = it }
                        RecurrenceOption("Purchase", isPurchaseRecurring, { isPurchaseRecurring = it }
                            , recurrenceFrequency = purchaseRecurrenceFrequency,
                            onRecurrenceFrequencyChange = { purchaseRecurrenceFrequency = it },
                            recurrenceIntervalText = purchaseRecurrenceIntervalText,
                            onRecurrenceIntervalChange = { purchaseRecurrenceIntervalText = it },
                            endDate = purchaseEndDate,
                            onEndDateChange = { purchaseEndDate = it })
                    }
                }



                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp,
                        Alignment.CenterHorizontally
                    )// This will space the buttons evenly
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    val context = LocalContext.current

                    Button(onClick = {
                        if (isActivityValid(activityType, foodType, transportationMode, departure, shoppingMethod)) {//transportation mode is gone
                            coroutineScope.launch {
                                try {
                                    val selectedDateTime = ZonedDateTime.of(date, time, ZoneId.systemDefault())

                                    val activity: TrackingActivity = when (activityType) {
                                        "Meal" -> Meal(
                                            date = selectedDateTime,
                                            name = "Meal on ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                                            type = Meal.Type.valueOf(foodType), // Convert foodType string to enum
                                            contents = listOf(
                                                Meal.Entry(
                                                    Meal.Entry.Type.valueOf(foodType), 1f
                                                )
                                            ),
                                            schedule = if (isMealRecurring) {
                                                val unit = when (mealRecurrenceFrequency) {
                                                    "Daily" -> TrackingDataGranularity.Day
                                                    "Weekly" -> TrackingDataGranularity.Week
                                                    else -> TrackingDataGranularity.Day // Defaulting to Day if somehow an unexpected value is received.
                                                }
                                                RecurrenceSchedule(
                                                    unit = unit,
                                                    period = mealRecurrenceIntervalText.toIntOrNull() ?: 1, // Safely parsing to Int, defaulting to 1.
                                                    endDate = mealEndDate?.atStartOfDay(ZoneId.systemDefault()) // Converting LocalDate to ZonedDateTime.
                                                )
                                            } else null

                                        )
                                        "Commute" -> Transportation(
                                            date = selectedDateTime,
                                            name = "Commute on ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                                            stops = stops.map {
                                                Transportation.Stop(it, 0.0, 0.0)
                                            },
                                            mode = Transportation.Mode.valueOf(transportationMode),
                                            schedule = if (isCommuteRecurring) {
                                                val unit = when (recurrenceFrequency) {
                                                    "Daily" -> TrackingDataGranularity.Day
                                                    "Weekly" -> TrackingDataGranularity.Week
                                                    else -> TrackingDataGranularity.Day // Defaulting to Day if somehow an unexpected value is received.
                                                }
                                                RecurrenceSchedule(
                                                    unit = unit,
                                                    period = recurrenceIntervalText.toIntOrNull() ?: 1, // Safely parsing to Int, defaulting to 1.
                                                    endDate = null//endDate?.atStartOfDay(ZoneId.systemDefault()) // Converting LocalDate to ZonedDateTime.
                                                )
                                            } else null
                                        )
                                        "Purchase" -> Purchase(
                                            date = selectedDateTime,
                                            name = "Purchase on ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                                            plasticBag = false,
                                            source = Purchase.Source.valueOf(shoppingMethod),
                                            schedule = if (isPurchaseRecurring) {
                                                val unit = when (purchaseRecurrenceFrequency) {
                                                    "Daily" -> TrackingDataGranularity.Day
                                                    "Weekly" -> TrackingDataGranularity.Week
                                                    else -> TrackingDataGranularity.Day // Defaulting to Day if somehow an unexpected value is received.
                                                }
                                                RecurrenceSchedule(
                                                    unit = unit,
                                                    period = purchaseRecurrenceIntervalText.toIntOrNull() ?: 1, // Safely parsing to Int, defaulting to 1.
                                                    endDate = mealEndDate?.atStartOfDay(ZoneId.systemDefault()) // Converting LocalDate to ZonedDateTime.
                                                )
                                            } else null
                                        )
                                        else -> throw IllegalArgumentException("Invalid activity type")
                                    }
                                    Log.d("MyAppTag", "The value of myImmutableValue is: $activity")
                                    val result = GreenTraceProviders.trackingProvider.addActivity(activity)
                                    // Handle result (e.g., show a confirmation message)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Activity saved: $result",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                catch (e: Exception) {
                                    Log.e("MyAppTag", "Error in coroutine", e)
                                }

                            }
                            saveAttempted = false
                        } else {
                            saveAttempted = true
                        }
                    }, colors = ButtonDefaults.buttonColors(Green40)) {
                        Text("Save Activity")
                    }

                    if (saveAttempted && !isActivityValid(activityType, foodType, transportationMode, departure, shoppingMethod)) {
                        Text(
                            text = "Please fill in all required fields for the selected activity type.",
                            color = Color.Red,
                            modifier = Modifier.padding(8.dp)
                        )
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
    onTransportationModeChange: (String) -> Unit,
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
                        onTransportationModeChange(type)
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
@Composable
fun RecurrenceOption(
    activityType: String,
    isRecurring: Boolean,
    onRecurringChange: (Boolean) -> Unit,
    recurrenceFrequency: String,
    onRecurrenceFrequencyChange: (String) -> Unit,
    recurrenceIntervalText: String,
    onRecurrenceIntervalChange: (String) -> Unit,
    endDate: LocalDate?,
    onEndDateChange: (LocalDate?) -> Unit
) {
    val context = LocalContext.current

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$activityType is recurring:", modifier = Modifier.padding(end = 8.dp))
            Switch(
                checked = isRecurring,
                onCheckedChange = {
                    onRecurringChange(it)
                    if (!it) {
                        // Reset to defaults if not recurring
                        onRecurrenceIntervalChange("1")
                        onEndDateChange(null)
                    }
                }
            )
        }
        if (isRecurring) {
            // Frequency Dropdown
            DropdownMenu(
                selectedFrequency = recurrenceFrequency,
                onFrequencyChange = onRecurrenceFrequencyChange
            )

            // Every (n) days/weeks
            EveryNDaysWeeksInput(
                recurrenceIntervalText = recurrenceIntervalText,
                onRecurrenceIntervalChange = onRecurrenceIntervalChange
            )

            // End Date (Optional)
            EndDateInput(
                endDate = endDate,
                onEndDateChange = onEndDateChange,
                context = context
            )
        }
    }
}



@Composable
fun EveryNDaysWeeksInput(
    recurrenceIntervalText: String,
    onRecurrenceIntervalChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Every (n) days/weeks:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = recurrenceIntervalText,
            onValueChange = onRecurrenceIntervalChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EndDateInput(
    endDate: LocalDate?,
    onEndDateChange: (LocalDate?) -> Unit,
    context: Context
) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("End Date (Optional):", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
        val endDateText = endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "Select Date"
        Button(
            onClick = {
                showDatePicker(context, endDate ?: LocalDate.now()) { selectedDate ->
                    onEndDateChange(selectedDate)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(endDateText)
        }
    }
}




@Composable
fun DropdownMenu(selectedFrequency: String, onFrequencyChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val frequencies = listOf("Daily", "Weekly")
    var selected by remember { mutableStateOf(selectedFrequency) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selected,
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
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            frequencies.forEach { frequency ->
                DropdownMenuItem(
                    text = { Text(frequency) },
                    onClick = {
                        selected = frequency
                        onFrequencyChange(frequency)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun isActivityValid(
    activityType: String,
    foodType: String,
    transportationMode: String,
    departure: String,
    shoppingMethod: String
): Boolean {
    return when (activityType) {
        "Meal" -> foodType.isNotEmpty()
        "Commute" -> transportationMode.isNotEmpty() //&& departure.isNotEmpty()
        "Purchase" -> shoppingMethod.isNotEmpty()
        else -> false
    }
}