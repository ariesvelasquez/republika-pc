package ariesvelasquez.com.republikapc.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ariesvelasquez.com.republikapc.model.LoadState
import ariesvelasquez.com.republikapc.utils.SingleLiveEvent

abstract class BaseViewModel : ViewModel() {

    val _loadState = SingleLiveEvent<LoadState<String>>()
    val loadState: SingleLiveEvent<LoadState<String>> get() = _loadState
}