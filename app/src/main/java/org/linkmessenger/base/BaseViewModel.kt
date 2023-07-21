package org.linkmessenger.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.linkmessenger.request.models.ErrorData

open class BaseViewModel:ViewModel() {
    val error:MutableLiveData<Exception?> = MutableLiveData()
    val loading:MutableLiveData<Boolean> = MutableLiveData()
}