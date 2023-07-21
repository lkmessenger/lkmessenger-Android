package org.linkmessenger.stickers.viewmodels

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.stickers.models.Sticker
import org.linkmessenger.stickers.models.StickersWithCategory
import org.linkmessenger.stickers.repository.StickersRepository

class StickersViewModel(val repository: StickersRepository) : BaseViewModel() {
    val stickersWithCategory: MutableLiveData<ArrayList<StickersWithCategory>?> = MutableLiveData()

    fun getStickersWithCategory(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val response = repository.getStickersWithCategory()

                if(!response.success){
                    throw java.lang.Exception(response.error?.message)
                }

                if(response.data != null){
                    stickersWithCategory.postValue(response.data as ArrayList<StickersWithCategory>)
                    loading.postValue(false)
                }

            }catch (e:Exception){
                error.postValue(e)
                loading.postValue(false)
            }
        }
    }
}