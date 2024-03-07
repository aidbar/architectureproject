package com.example.architectureproject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class UserInfo(
    var name: String,
    var bio: String,
    var transportationMethod: String,
    var difficultyWalking: String,
    var diet: String,
    var preference: String,
    var shoppingMethod: String,
    var influence: String
)

var userResponses: UserInfo = UserInfo("", "", "", "", "", "", "", "")

fun createFirestoreUserDocument(userResponses: UserInfo) {
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    if (uid != null ){
        val userDocRef = db.collection("users").document(uid)

        // Create a hashmap of data to update
        val update = hashMapOf<String, Any>(
            "name" to userResponses.name,
            "bio" to userResponses.bio,
            "transportationMethod" to userResponses.transportationMethod,
            "difficultyWalking" to userResponses.difficultyWalking,
            "dietRestriction" to userResponses.diet,
            "shoppingMethod" to userResponses.shoppingMethod,
            "locallySourcedFoodPreference" to userResponses.preference,
            "sustainableShoppingPreference" to userResponses.influence
        )

        // Create the document
        userDocRef.set(update)
            .addOnSuccessListener {
                Log.d(TAG, "User document successfully added!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding user document", e)
            }
    } else {
        Log.e(ContentValues.TAG, "Couldn't find uid of currently authenticated user")
    }
}

class NewAccountSetupScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Column(
            modifier = Modifier
                .fillMaxSize()
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

class ProfileSetupScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            val nameState = remember { mutableStateOf(TextFieldValue()) }
            OutlinedTextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(32.dp),
                singleLine = true,
                placeholder = { Text(text = "Alias") }
            )

            val bioState = remember { mutableStateOf(TextFieldValue()) }
            OutlinedTextField(
                value = bioState.value,
                onValueChange = { bioState.value = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(32.dp),
                placeholder = { Text(text = "Bio") }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    userResponses.name = nameState.value.text
                    userResponses.bio = bioState.value.text
                    navigator?.push(StartQuestionsScreen())
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nameState.value.text.isNotEmpty()
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
                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                color = Color(0xFF009688),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                text = "Welcome to GreenTrace, ${userResponses.name}.\n",
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
                    createFirestoreUserDocument(userResponses)
                    navigator?.push(MainScreen(false))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Skip for now")
            }

            Button(
                onClick = { navigator?.push(TransportationQScreen()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}

class TransportationQScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        var expanded by remember { mutableStateOf(false) }
        val primary_options = listOf(
            "Walking",
            "Cycling",
            "Public Transportation (Bus, Train, Subway)",
            "Personal Vehicle (Car, Motorcycle)",
            "Carpooling",
            "Remote/Work from Home (No Commute)"
        )
        val secondary_options = listOf(
            "Yes", "No"
        )
        var selectedPrimaryOption by remember { mutableStateOf("") }
        var selectedSecondaryOption by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxSize()
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

            primary_options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable { selectedPrimaryOption = option }
                ) {
                    RadioButton(
                        selected = selectedPrimaryOption == option,
                        onClick = { selectedPrimaryOption = option }
                    )
                    Text(text = option, modifier = Modifier.padding(start = 8.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Do you have any difficulty walking or using stairs?",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            secondary_options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable { selectedSecondaryOption = option }
                ) {
                    RadioButton(
                        selected = selectedSecondaryOption == option,
                        onClick = { selectedSecondaryOption = option }
                    )
                    Text(text = option, modifier = Modifier.padding(start = 8.dp))
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
                        userResponses.transportationMethod = selectedPrimaryOption
                        userResponses.difficultyWalking = selectedSecondaryOption
                        navigator?.push(FoodQScreen())
                    },
                    modifier = Modifier.width(150.dp),
                    enabled = selectedPrimaryOption.isNotEmpty() && selectedSecondaryOption.isNotEmpty()
                ) {
                    Text("NEXT")
                }
            }

        }

    }
}

class FoodQScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        var expanded by remember { mutableStateOf(false) }
        val restriction_options = listOf(
            "No Restrictions",
            "Vegetarian (No Meat)",
            "Vegan (No Animal Products)",
            "Pescatarian (Fish, No Other Meat)"
        )
        val frequency_options = listOf(
            "Always",
            "Sometimes",
            "Rarely",
            "Never",
        )
        var selectedRestrictionOption by remember { mutableStateOf("") }
        var selectedFrequencyOption by remember { mutableStateOf("") }
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

            Text(
                text = "Which best describes your typical diet?",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            restriction_options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable { selectedRestrictionOption = option }
                ) {
                    RadioButton(
                        selected = selectedRestrictionOption == option,
                        onClick = { selectedRestrictionOption = option }
                    )
                    Text(text = option, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "How often do you prefer locally sourced food products?",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            frequency_options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable { selectedFrequencyOption = option }
                ) {
                    RadioButton(
                        selected = selectedFrequencyOption == option,
                        onClick = { selectedFrequencyOption = option }
                    )
                    Text(text = option, modifier = Modifier.padding(start = 8.dp))
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
                        userResponses.diet = selectedRestrictionOption
                        userResponses.preference = selectedFrequencyOption
                        navigator?.push(ShoppingQScreen())
                    },
                    modifier = Modifier.width(150.dp),
                    enabled = selectedFrequencyOption.isNotEmpty() && selectedRestrictionOption.isNotEmpty()
                ) {
                    Text("NEXT")
                }
            }
        }
    }
}

class ShoppingQScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        var expanded by remember { mutableStateOf(false) }
        val primary_options = listOf(
            "In-Store",
            "Online",
            "Combination of Both"
        )
        val secondary_options = listOf(
            "Greatly",
            "Moderately",
            "Slightly",
            "Not at All",
        )
        var selectedPrimaryOption by remember { mutableStateOf("") }
        var selectedSecondaryOption by remember { mutableStateOf("") }
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

                primary_options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clickable { selectedPrimaryOption = option }
                    ) {
                        RadioButton(
                            selected = selectedPrimaryOption == option,
                            onClick = { selectedPrimaryOption = option }
                        )
                        Text(text = option, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "When shopping, how much do sustainable and ethical practices influence your"
                            +" choice of brands/products?",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                secondary_options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clickable { selectedSecondaryOption = option }
                    ) {
                        RadioButton(
                            selected = selectedSecondaryOption == option,
                            onClick = { selectedSecondaryOption = option }
                        )
                        Text(text = option, modifier = Modifier.padding(start = 8.dp))
                    }
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
                        userResponses.shoppingMethod = selectedPrimaryOption
                        userResponses.influence = selectedSecondaryOption

                        createFirestoreUserDocument(userResponses)
                        navigator?.push(MainScreen(false))
                    },
                    modifier = Modifier.width(150.dp),
                    enabled = selectedPrimaryOption.isNotEmpty() && selectedSecondaryOption.isNotEmpty()
                ) {
                    Text("COMPLETE")
                }
            }
        }
    }
}

