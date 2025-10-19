package com.kronos.multiplatform.weatherapp.core.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class ParentViewModel(
) : ViewModel() {

    var loading by mutableStateOf<Boolean>(false)
    var message by mutableStateOf<HashMap<String, String>?>(null)
    private var _refreshing = MutableStateFlow(false)
    var refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _limit = MutableStateFlow<Int>(50)
    val limit = _limit.asStateFlow()

    private val _offset = MutableStateFlow<Int>(0)
    val offset = _offset.asStateFlow()

    internal var _total = MutableStateFlow<Long>(0)
    val total = _total.asStateFlow()

    fun setLimit(i: Int) {
        _limit.value = i
    }

    fun setOffset(i: Int) {
        _offset.value = i
    }

    fun setRefreshing(value: Boolean) {
        _refreshing.value = value
    }

    init {
        loading = false
        message = HashMap()
    }

    fun log(item: String,exception: Exception? = null) {
        viewModelScope.launch {
            Logger.i(messageString = item, throwable = exception,tag = this::class.simpleName.toString())
        }
    }
}