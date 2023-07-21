package org.linkmessenger.reports.repository


import org.linkmessenger.reports.models.ReportResponse
import org.linkmessenger.request.models.DefaultResponse
import org.linkmessenger.request.models.SocialResponse

interface ReportRepository {
    suspend fun getReportTypes():SocialResponse<ReportResponse>
    suspend fun reportPost(postId: Long, typeId: Int):SocialResponse<DefaultResponse>
}