package com.kronos.multiplatform.weatherapp.core.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.location.LOCATION
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import dev.icerock.moko.permissions.storage.STORAGE
import kotlinx.coroutines.launch

class PermissionViewModel(
    val controller: PermissionsController
) : ParentViewModel(){

    var stateStorage by mutableStateOf(PermissionState.NotDetermined)
    var stateLocation by mutableStateOf(PermissionState.NotDetermined)
    var stateNotification by mutableStateOf(PermissionState.NotDetermined)

    init {
        viewModelScope.launch {
            stateStorage = controller.getPermissionState(Permission.STORAGE)
            stateLocation = controller.getPermissionState(Permission.LOCATION)
            stateNotification = controller.getPermissionState(Permission.REMOTE_NOTIFICATION)
        }
    }

    fun provideOrRequestStoragePermission() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.STORAGE)
                stateStorage = PermissionState.Granted
            } catch(e: DeniedAlwaysException) {
                stateStorage = PermissionState.Denied
            } catch(e: DeniedException) {
                stateStorage = PermissionState.Denied
            } catch(e: RequestCanceledException) {
                e.printStackTrace()
            }
        }
    }

    fun provideOrRequestLocationPermission() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.LOCATION)
                stateLocation = PermissionState.Granted
            } catch(e: DeniedAlwaysException) {
                e.printStackTrace()
                stateLocation = PermissionState.Denied
            } catch(e: DeniedException) {
                e.printStackTrace()
                stateLocation = PermissionState.Denied
            } catch(e: RequestCanceledException) {
                e.printStackTrace()
            }
        }
    }

    fun provideOrRequestNotificationPermission() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.REMOTE_NOTIFICATION)
                stateNotification = PermissionState.Granted
            } catch(e: DeniedAlwaysException) {
                stateNotification = PermissionState.Denied
            } catch(e: DeniedException) {
                stateNotification = PermissionState.Denied
            } catch(e: RequestCanceledException) {
                e.printStackTrace()
            }
        }
    }

}