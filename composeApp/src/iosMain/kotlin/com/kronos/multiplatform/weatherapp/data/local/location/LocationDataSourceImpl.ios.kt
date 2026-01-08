package com.kronos.multiplatform.weatherapp.data.local.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class LocationDataSourceImpl : LocationDataSource {

    private val manager = CLLocationManager()
    private var continuation: CancellableContinuation<LocationModel?>? = null

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {

        @OptIn(ExperimentalForeignApi::class)
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = (didUpdateLocations.lastOrNull() as? CLLocation) ?: return
            manager.stopUpdatingLocation()

            val coordinate = location.coordinate
            val model = coordinate.useContents {
                LocationModel(latitude = latitude, longitude = longitude)
            }

            continuation?.resume(model)
            continuation = null
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            continuation?.resume(null)
            continuation = null
        }
    }

    override suspend fun getCurrentLocation(): LocationModel? =
        suspendCancellableCoroutine { cont ->
            continuation = cont
            manager.delegate = delegate
            manager.requestWhenInUseAuthorization()
            manager.startUpdatingLocation()
        }

    override suspend fun isLocationEnabled(): Boolean {
        return CLLocationManager.locationServicesEnabled()
    }
}
