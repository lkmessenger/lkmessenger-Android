package org.linkmessenger.reports.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Report(
        val id:Int,
        val title: String?,
        val description: String?
) : Parcelable

data class ReportResponse(
        var items : ArrayList<Report>
)