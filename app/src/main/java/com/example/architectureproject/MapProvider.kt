package com.example.architectureproject

import com.example.architectureproject.tracking.Transportation

interface MapProvider {
    fun computeDistance(stops: List<Transportation.Stop>): Float =
        stops.windowed(2).fold(0f) { acc, cur ->
            acc + computeDistance(cur[0], cur[1])
        }
    fun computeDistance(src: Transportation.Stop, dst: Transportation.Stop): Float
}