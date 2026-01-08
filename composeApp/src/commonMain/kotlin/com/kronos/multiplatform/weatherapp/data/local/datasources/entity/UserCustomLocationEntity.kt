package com.kronos.multiplatform.weatherapp.data.local.datasources.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "USER_CUSTOM_LOCATION"/*,
    indices = [
        Index(value = ["ID"], unique = true),
        Index(value = ["TRACKING_NUMBER"], unique = true)
    ]*/
)
data class UserCustomLocationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID") val id: Long,
    @ColumnInfo(name = "CITY_NAME") val cityName: String,
    @ColumnInfo(name = "TEMP_C")var tempC:Double = 0.0,
    @ColumnInfo(name = "ICON")var icon:String = "",
    @ColumnInfo(name = "IS_CURRENT", defaultValue = "0") val isCurrent: Boolean,
    @ColumnInfo(name = "IS_SELECTED", defaultValue = "0") val isSelected:Boolean,
    @ColumnInfo(name = "LATITUD", defaultValue = "0") val lat:Double,
    @ColumnInfo(name = "LONGITUD", defaultValue = "0") val lon:Double,
)

