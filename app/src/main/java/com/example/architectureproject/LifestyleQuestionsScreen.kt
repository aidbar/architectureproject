package com.example.architectureproject

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.architectureproject.profile.UserLifestyle
import kotlinx.coroutines.launch

class NewAccountSetupScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                Spacer(modifier = Modifier.height(250.dp))
                Text(
                    color = Color(0xFF009688),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    text = "Welcome!",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "We're glad you're here. Just a few more steps and your account will be ready to go.",
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = Color(0xFF009688),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navigator?.push(ProfileSetupScreen()) },
                modifier = Modifier
                    .fillMaxWidth() // Make the button fill the entire width
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Let's Start!")
            }
        }
    }
}

class ProfileSetupScreenModel : ScreenModel {
    var nameState by mutableStateOf(TextFieldValue())
    var bioState by mutableStateOf(TextFieldValue())
    var ageState by mutableStateOf(TextFieldValue())

    suspend fun updateUserProfile() {
        //screenModelScope.launch {
            GreenTraceProviders.userProvider.userProfile(
                nameState.text,
                bioState.text,
                ageState.text.toInt()
            )
        //}
    }
}
class ProfileSetupScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val scope = rememberCoroutineScope()
        val model = rememberScreenModel{ProfileSetupScreenModel()}
        var displayLifestyleScreen by remember{ mutableStateOf(false) }
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navigator?.pop() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                }
            }

            Text(
                text = "Create your profile",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Placeholder for user image upload
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(50))
                    .border(2.dp, Color.Gray, RoundedCornerShape(50))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
//                Text("Tap to upload", color = Color.Gray)
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Upload",
                    modifier = Modifier.size(40.dp),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text input fields
            //val nameState = remember { mutableStateOf(TextFieldValue()) }
            OutlinedTextField(
                value = model.nameState,
                onValueChange = { model.nameState = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(32.dp),
                singleLine = true,
                placeholder = { Text(text = "Alias (required)") }
            )

            //val bioState = remember { mutableStateOf(TextFieldValue()) }
            OutlinedTextField(
                value = model.bioState,
                onValueChange = { model.bioState = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(32.dp),
                placeholder = { Text(text = "Bio") }
            )

            //val ageState = remember { mutableStateOf(TextFieldValue()) }
            OutlinedTextField(
                value = model.ageState,
                onValueChange = { model.ageState = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(32.dp),
                placeholder = { Text(text = "Age (required)") }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val ageStr = model.ageState.text.trim()
                    if(model.nameState.text.isBlank()) {
                        Toast.makeText(context, "Please enter an alias!", Toast.LENGTH_SHORT).show()
                    }
                    if (ageStr.isEmpty() || model.ageState.text.contains(Regex.fromLiteral("[^0-9]"))) {
                        Log.e("ProfileSetupScreen", "bad age value: $ageStr")
                        Toast.makeText(context, "Please enter your age as a number!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        model.updateUserProfile()
                        navigator?.push(StartQuestionsScreen())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = model.nameState.text.isNotEmpty()
            ) {
                Text("Accept")
            }
        }
    }
}

class StartQuestionsScreen() : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                color = Color(0xFF009688),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                text = "Welcome to GreenTrace, ${GreenTraceProviders.userProvider.userInfo().name}.\n",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                color = Color(0xFF009688),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                text = "Your answers to the following questions " +
                        "can help us create personalized and sustainable plans tailored to your lifestyle.",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    navigator?.push(MainScreen(false))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Skip for now")
            }

            Button(
                onClick = { navigator?.push(TransportationQScreen(hasLifestyleResponses = false, UserLifestyle.Builder())) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}

class TransportationQScreenModel(hasLifestyleResponses : Boolean, val userResponses: UserLifestyle.Builder) : ScreenModel {
    fun saveResponses() {
        userResponses.transportationPreference = selectedPrimaryOption.second
        if (selectedSecondaryOption.second) {
            userResponses.disabilities = setOf(UserLifestyle.Disability.DifficultyWalking)
        }
    }

    val primary_options = listOf(
        "Walking" to UserLifestyle.TransportationMethod.Walk,
        "Cycling" to UserLifestyle.TransportationMethod.Cycle,
        "Public Transportation (Bus, Train, Subway)" to UserLifestyle.TransportationMethod.PublicTransport,
        "Personal Vehicle (Car, Motorcycle)" to UserLifestyle.TransportationMethod.PersonalVehicle,
        "Carpooling" to UserLifestyle.TransportationMethod.Carpool,
        "Remote/Work from Home (No Commute)" to UserLifestyle.TransportationMethod.None
    )
    val secondary_options = listOf(
        "Yes" to true, "No" to false
    )

    var selectedPrimaryOption by if(hasLifestyleResponses) {
        val userLifestyle = GreenTraceProviders.userProvider.userLifestyle()
        val preferenceString = primary_options.find { it.second == userLifestyle.transportationPreference }?.first ?: ""
        mutableStateOf(preferenceString to userLifestyle.transportationPreference)
    } else {
        mutableStateOf("" to userResponses.transportationPreference)
    }

    var selectedSecondaryOption by if(hasLifestyleResponses) {
            val userLifestyle = GreenTraceProviders.userProvider.userLifestyle()
            if(userLifestyle.disabilities.isNotEmpty()) {
                mutableStateOf("Yes" to true)
            } else {
                mutableStateOf("No" to false)
            }
        } else {
            mutableStateOf("" to false)
        }
}

class TransportationQScreen(private val hasLifestyleResponses: Boolean = false,
                            private val userResponses: UserLifestyle.Builder) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel {
            TransportationQScreenModel(hasLifestyleResponses, userResponses)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {navigator?.popUntilRoot()}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                }
            }

            Text(
                text = "How do you usually get around for your daily activities?",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            model.primary_options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable { model.selectedPrimaryOption = option }
                ) {
                    RadioButton(
                        selected = model.selectedPrimaryOption == option,
                        onClick = { model.selectedPrimaryOption = option }
                    )
                    Text(text = option.first, modifier = Modifier.padding(start = 8.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Do you have any difficulty walking or using stairs?",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            model.secondary_options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable { model.selectedSecondaryOption = option }
                ) {
                    RadioButton(
                        selected = model.selectedSecondaryOption == option,
                        onClick = { model.selectedSecondaryOption = option }
                    )
                    Text(text = option.first, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = {
                        navigator?.pop()
                    },
                    modifier = Modifier.width(150.dp)
                ) {
                    Text("BACK")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        model.saveResponses()
                        navigator?.push(FoodQScreen(hasLifestyleResponses = hasLifestyleResponses, model.userResponses))
                    },
                    modifier = Modifier.width(150.dp),
                    enabled = model.selectedPrimaryOption.first.isNotEmpty() && model.selectedSecondaryOption.first.isNotEmpty()
                ) {
                    Text("NEXT")
                }
            }

        }

    }
}

class FoodQScreenModel(hasLifestyleResponses: Boolean, val userResponses: UserLifestyle.Builder) : ScreenModel {
    fun saveResponses() {
        userResponses.diet = selectedRestrictionOption.second
        userResponses.locallySourcedFoodPreference = selectedFrequencyOption.second
    }

    val restriction_options = listOf(
        "No Restrictions" to UserLifestyle.Diet.None,
        "Vegetarian (No Meat)" to UserLifestyle.Diet.Vegetarian,
        "Vegan (No Animal Products)" to UserLifestyle.Diet.Vegan,
        "Pescatarian (Fish, No Other Meat)" to UserLifestyle.Diet.Pescatarian
    )
    val frequency_options = listOf(
        "Always" to UserLifestyle.Frequency.Always,
        "Sometimes" to UserLifestyle.Frequency.Sometimes,
        "Rarely" to UserLifestyle.Frequency.Rarely,
        "Never" to UserLifestyle.Frequency.Never
    )

    var selectedRestrictionOption by if(hasLifestyleResponses) {
            val userLifestyle = GreenTraceProviders.userProvider.userLifestyle()
            val preferenceString = restriction_options.find { it.second == userLifestyle.diet }?.first ?: ""
            mutableStateOf(preferenceString to userLifestyle.diet)
        } else {
            mutableStateOf("" to userResponses.diet)
        }

    var selectedFrequencyOption by if(hasLifestyleResponses) {
            val userLifestyle = GreenTraceProviders.userProvider.userLifestyle()
            val preferenceString = frequency_options.find { it.second == userLifestyle.locallySourcedFoodPreference }?.first ?: ""
            mutableStateOf(preferenceString to userLifestyle.locallySourcedFoodPreference)
        } else {
            mutableStateOf("" to userResponses.locallySourcedFoodPreference)
        }
}

class FoodQScreen(private val hasLifestyleResponses: Boolean = false,
    private val userResponses: UserLifestyle.Builder) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel {
            FoodQScreenModel(hasLifestyleResponses, userResponses)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {navigator?.popUntilRoot()}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                }
            }

            Text(
                text = "Which best describes your typical diet?",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            model.restriction_options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable { model.selectedRestrictionOption = option }
                ) {
                    RadioButton(
                        selected = model.selectedRestrictionOption == option,
                        onClick = { model.selectedRestrictionOption = option }
                    )
                    Text(text = option.first, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "How often do you prefer locally sourced food products?",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            model.frequency_options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable { model.selectedFrequencyOption = option }
                ) {
                    RadioButton(
                        selected = model.selectedFrequencyOption == option,
                        onClick = { model.selectedFrequencyOption = option }
                    )
                    Text(text = option.first, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = {
                        navigator?.pop()
                    },
                    modifier = Modifier.width(150.dp)
                ) {
                    Text("BACK")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        model.saveResponses()
                        navigator?.push(ShoppingQScreen(hasLifestyleResponses = hasLifestyleResponses, model.userResponses))
                    },
                    modifier = Modifier.width(150.dp),
                    enabled = model.selectedFrequencyOption.first.isNotEmpty() && model.selectedRestrictionOption.first.isNotEmpty()
                ) {
                    Text("NEXT")
                }
            }
        }
    }
}

class ShoppingQScreenModel(hasLifestyleResponses: Boolean, val userResponses: UserLifestyle.Builder) : ScreenModel {
    suspend fun saveResponses() {
        userResponses.shoppingPreference = selectedPrimaryOption.second
        userResponses.sustainabilityInfluence = selectedSecondaryOption.second
        GreenTraceProviders.userProvider.userLifestyle(userResponses.build())
            ?.let { Log.e("updateLifestyle", it) }
    }

    val primary_options = listOf(
        "In-Store" to UserLifestyle.ShoppingMethod.InStore,
        "Online" to UserLifestyle.ShoppingMethod.Online,
        "Combination of Both" to UserLifestyle.ShoppingMethod.Both
    )
    val secondary_options = listOf(
        "Greatly" to UserLifestyle.Frequency.Always,
        "Moderately" to UserLifestyle.Frequency.Sometimes,
        "Slightly" to UserLifestyle.Frequency.Rarely,
        "Not at All" to UserLifestyle.Frequency.Never,
    )

    var selectedPrimaryOption by if(hasLifestyleResponses) {
            val userLifestyle = GreenTraceProviders.userProvider.userLifestyle()
            val preferenceString = primary_options.find { it.second == userLifestyle.shoppingPreference }?.first ?: ""
            mutableStateOf(preferenceString to userLifestyle.shoppingPreference)
        } else {
            mutableStateOf("" to userResponses.shoppingPreference)
        }

    var selectedSecondaryOption by if(hasLifestyleResponses) {
            val userLifestyle = GreenTraceProviders.userProvider.userLifestyle()
            val preferenceString = secondary_options.find { it.second == userLifestyle.sustainabilityInfluence }?.first ?: ""
            mutableStateOf(preferenceString to userLifestyle.sustainabilityInfluence)
        } else {
            mutableStateOf("" to userResponses.sustainabilityInfluence)
        }
}

class ShoppingQScreen(private val hasLifestyleResponses: Boolean = false,
    private val userResponses: UserLifestyle.Builder) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = rememberScreenModel {
            ShoppingQScreenModel(hasLifestyleResponses, userResponses)
        }

        val scope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),){
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {navigator?.popUntilRoot()}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                }
            }
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "How do you primarily do your shopping?",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                model.primary_options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clickable { model.selectedPrimaryOption = option }
                    ) {
                        RadioButton(
                            selected = model.selectedPrimaryOption == option,
                            onClick = { model.selectedPrimaryOption = option }
                        )
                        Text(text = option.first, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "When shopping, how much do sustainable and ethical practices influence your"
                            +" choice of brands/products?",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                model.secondary_options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clickable { model.selectedSecondaryOption = option }
                    ) {
                        RadioButton(
                            selected = model.selectedSecondaryOption == option,
                            onClick = { model.selectedSecondaryOption = option }
                        )
                        Text(text = option.first, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = {
                            navigator?.pop()
                        },
                        modifier = Modifier.width(150.dp)
                    ) {
                        Text("BACK")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                model.saveResponses()
                                navigator.popUntilRoot()
                                if (navigator.lastItemOrNull !is ProfileScreen) {
                                    navigator.push(MainScreen(false))
                                }
                            }
                        },
                        modifier = Modifier.width(150.dp),
                        enabled = model.selectedPrimaryOption.first.isNotEmpty() && model.selectedSecondaryOption.first.isNotEmpty()
                    ) {
                        Text("COMPLETE")
                    }
                }
            }
        }
    }
}

