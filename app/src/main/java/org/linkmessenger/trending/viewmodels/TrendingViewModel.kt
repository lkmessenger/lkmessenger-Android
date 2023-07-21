package org.linkmessenger.trending.viewmodels

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.profile.repository.ProfileRepository
import org.linkmessenger.request.models.MessageRequest
import org.linkmessenger.request.models.SubscribeParams
import org.linkmessenger.request.models.TrendingParams

class TrendingViewModel(val repository: ProfileRepository) : BaseViewModel() {
    val trendToday: MutableLiveData<ArrayList<ProfileData>> = MutableLiveData()
    val trendWeek: MutableLiveData<ArrayList<ProfileData>> = MutableLiveData()
    val trendMonth:MutableLiveData<ArrayList<ProfileData>> = MutableLiveData()
    val trendAll: MutableLiveData<ArrayList<ProfileData>> = MutableLiveData()

    var trendTodayPage = 0
    var trendWeekPage = 0
    var trendMonthPage = 0
    var trendAllPage = 0

    var limit = 20

    fun loadToday(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val res = repository.getTrendings(TrendingParams("day",  trendTodayPage, limit))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(res.data != null){
                    trendToday.postValue(res.data as ArrayList<ProfileData>)
                    trendTodayPage++
                }

            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun loadWeek(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val res = repository.getTrendings(TrendingParams("week",  trendWeekPage, limit))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(res.data != null){
                    trendWeek.postValue(res.data as ArrayList<ProfileData>)
                    trendWeekPage++
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun loadMonth(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val res = repository.getTrendings(TrendingParams("month", trendMonthPage, limit))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(res.data != null){
                    trendMonth.postValue(res.data as ArrayList<ProfileData>)
                    trendMonthPage++
                }

            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun loadAll(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val res = repository.getTrendings(TrendingParams("all", trendAllPage, limit))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(res.data != null){
                    trendAll.postValue(res.data as ArrayList<ProfileData>)
                    trendAllPage++
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun subscribe(userId: Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.subscribe(SubscribeParams(userId))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }
        }
    }

    fun unSubscribe(userId:Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.unSubscribe(SubscribeParams(userId))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }
        }
    }
}