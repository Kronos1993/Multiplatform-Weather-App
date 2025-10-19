package com.kronos.multiplatform.weatherapp.data.local.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume


actual class LocationDataSourceImpl : NSObject(), LocationDataSource, CLLocationManagerDelegateProtocol {

    private val manager = CLLocationManager()
    private var continuation: CancellableContinuation<LocationModel?>? = null

    override suspend fun getCurrentLocation(): LocationModel? =
        suspendCancellableCoroutine { cont ->
            continuation = cont
            manager.delegate = this
            manager.requestWhenInUseAuthorization()
            manager.startUpdatingLocation()
        }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = (didUpdateLocations.lastOrNull() as? CLLocation) ?: return
        manager.stopUpdatingLocation()

        // FORMA CORRECTA - Usar coordinate directamente
        val coordinate = location.coordinate
        val locationDescription = location.description
        val latLon = parseLatLonFromDescription(locationDescription.orEmpty())

        val model1 = LocationModel(
            latitude = latLon.first,
            longitude = latLon.second
        )

        val model = LocationModel(
            latitude = location.horizontalAccuracy,
            longitude = location.verticalAccuracy
        )
        continuation?.resume(model)
        continuation = null
    }

    private fun parseLatLonFromDescription(description: String): Pair<Double, Double> {
        try {
            val regex = """<([+-]?\d+\.\d+), ([+-]?\d+\.\d+)>""".toRegex()
            val matchResult = regex.find(description)
            if (matchResult != null) {
                val (lat, lon) = matchResult.destructured
                return Pair(lat.toDouble(), lon.toDouble())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(0.0, 0.0)
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        continuation?.resume(null)
        continuation = null
    }

    override suspend fun isLocationEnabled(): Boolean {
        return CLLocationManager.locationServicesEnabled()
    }

}
