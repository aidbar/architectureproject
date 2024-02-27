package com.example.architectureproject

/***
 * IMPORTANT: This is a placeholder class. It will be replaced with the community data model class when it is ready. After that, this file will be deleted.
 */

/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * [CommunityDataModelPlaceholderClass] is the data class to represent the CommunityDataModelPlaceholderClass text and imageResourceId
 */
data class CommunityDataModelPlaceholderClass(
    @StringRes val nameStringResourceId: Int,
    @StringRes val locationStringResourceId: Int,
    @DrawableRes val imageResourceId: Int
)

class Datasource() {
    fun loadCommunities(): List<CommunityDataModelPlaceholderClass> {
        return listOf<CommunityDataModelPlaceholderClass>(
            CommunityDataModelPlaceholderClass(R.string.community1, R.string.locationTest, R.drawable.image1),
            CommunityDataModelPlaceholderClass(R.string.community2, R.string.locationTest, R.drawable.image1),
            CommunityDataModelPlaceholderClass(R.string.community3, R.string.locationTest, R.drawable.image1),
            CommunityDataModelPlaceholderClass(R.string.community4, R.string.locationTest, R.drawable.image1),
            CommunityDataModelPlaceholderClass(R.string.community5, R.string.locationTest,R.drawable.image1),
            CommunityDataModelPlaceholderClass(R.string.community6, R.string.locationTest, R.drawable.image1),
            CommunityDataModelPlaceholderClass(R.string.community7, R.string.locationTest, R.drawable.image1),
            CommunityDataModelPlaceholderClass(R.string.community8, R.string.locationTest, R.drawable.image1),
            CommunityDataModelPlaceholderClass(R.string.community9, R.string.locationTest, R.drawable.image1),
            CommunityDataModelPlaceholderClass(R.string.community10, R.string.locationTest, R.drawable.image1))
    }
}