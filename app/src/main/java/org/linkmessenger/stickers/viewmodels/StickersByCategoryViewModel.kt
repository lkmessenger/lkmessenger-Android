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

class StickersByCategoryViewModel(val repository: StickersRepository) : BaseViewModel() {
    val stickers: MutableLiveData<ArrayList<Sticker>> = MutableLiveData()
    var page: Int = 0
    val limit = 20

    init {
        page = 0
    }

    fun getStickersByCategory(id: Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)

                val response = repository.getStickersByCategory(id = id, page = page, limit = limit)

                if(!response.success){
                    throw java.lang.Exception(response.error?.message)
                }

                if(response.data != null){
                    stickers.postValue(response.data as ArrayList<Sticker>)
                    page++
                }

                loading.postValue(false)
            }catch (e:Exception){
                loading.postValue(false)
                error.postValue(e)
            }
        }
    }
}