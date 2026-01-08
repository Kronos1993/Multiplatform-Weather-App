package com.kronos.multiplatform.weatherapp.data.local.location

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class LocationDataSourceImpl(
    private val context: Context
) : LocationDataSource {

    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun getCurrentLocation(): LocationModel? = suspendCoroutine { cont ->
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val location = task.result
                    if (location != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                val city = addresses?.firstOrNull()?.locality
                                cont.resume(LocationModel(location.latitude, location.longitude, city, current = true))
                            } catch (e: Exception) {
                                cont.resume(LocationModel(location.latitude, location.longitude, null, current = false))
                            }
                        }
                    } else {
                        cont.resume(null)
                    }
                } else {
                    cont.resume(null)
                }
            }
    }

    override suspend fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}