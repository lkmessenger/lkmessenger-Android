package org.linkmessenger.profile.viewmodel

import androidx.lifecycle.MutableLiveData
import org.linkmessenger.base.BaseViewModel

class ShareProfileViewModel:BaseViewModel() {
    val username: MutableLiveData<String> = MutableLiveData()
    val colorId: MutableLiveData<Int> = MutableLiveData()

    init {
        changeColorId()
    }

    fun setUsername(username:String){
        this.username.postValue(username)
    }
    fun changeColorId(){
        if(this.colorId.value==null){
            this.colorId.postValue(0)
        }else{
            var tmp = this.colorId.value!! + 1
            if(tmp>4){
                tmp = 0
            }
            this.colorId.postValue(tmp)
        }
        username.value?.let {
            setUsername(it)
        }
    }
}