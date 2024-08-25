package com.rahulrav.camera.control

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q

/** The location permissions we need. */
val locationPermissions: Set<String> = setOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)

/** Defer requesting background location until necessary. */
val backgroundLocationPermissions: Set<String>
    get() =
        when {
            SDK_INT >= Q -> setOf(ACCESS_BACKGROUND_LOCATION)
            else -> emptySet()
        }
