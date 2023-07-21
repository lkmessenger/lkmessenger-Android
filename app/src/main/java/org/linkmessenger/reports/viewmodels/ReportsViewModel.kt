package org.linkmessenger.reports.viewmodels

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.reports.models.Report
import org.linkmessenger.reports.repository.ReportRepository

class ReportsViewModel(val repository: ReportRepository,) : BaseViewModel() {
    val reportTypes: MutableLiveData<ArrayList<Report>> = MutableLiveData()
    val isReported: MutableLiveData<Boolean> = MutableLiveData()

    fun loadReportTypes(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val res = repository.getReportTypes()
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(res.data?.items?.isNotEmpty() == true){
                    reportTypes.postValue(res.data.items)
                    loading.postValue(false)
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun reportPost(postId: Long, reportType:Int){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                loading.postValue(true)
                isReported.postValue(false)
                val res = repository.reportPost(postId, reportType)
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                isReported.postValue(true)
                loading.postValue(false)

            }catch (e: java.lang.Exception){
                error.postValue(e)
            }
        }
    }
}