package org.linkmessenger.reports.repository

import org.linkmessenger.reports.models.ReportResponse
import org.linkmessenger.request.api.SocialApi
import org.linkmessenger.request.models.DefaultResponse
import org.linkmessenger.request.models.ReportPostParams
import org.linkmessenger.request.models.SocialResponse

class ReportRepositoryImpl(private val socialApi: SocialApi): ReportRepository {
    private val TAG:String = this.javaClass.toString()
    override suspend fun getReportTypes(): SocialResponse<ReportResponse> {
        return socialApi.getReportTypes()
    }

    override suspend fun reportPost(postId: Long, typeId: Int): SocialResponse<DefaultResponse> {
        return socialApi.reportPost(ReportPostParams(postId, typeId))
    }

}