//Adapted from https://medium.com/@jpmtech/jetpack-compose-display-a-photo-picker-6bcb9b357a3a

package com.example.architectureproject

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * This is an image selector used in the "Create a new community" pop-up.
 */

//class ImageSelector {
    @Composable
    fun ImageSelectorComponent() {
        var selectedImages by remember {
            mutableStateOf<List<Uri?>>(emptyList())
        }
        val buttonText = "Select an image"

        val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri -> selectedImages = listOf(uri) }
        )

        fun launchPhotoPicker() {
                singlePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
        }

        Column(
            //modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                launchPhotoPicker()
            }) {
                Text(buttonText)
            }

            ImageLayoutView(selectedImages = selectedImages)
        }
    }

    @Composable
    fun ImageLayoutView(selectedImages: List<Uri?>) {
        LazyRow {
            items(selectedImages) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

//}