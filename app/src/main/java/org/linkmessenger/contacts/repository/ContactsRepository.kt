package org.linkmessenger.contacts.repository

import org.linkmessenger.request.models.SocialResponse
import org.linkmessenger.request.models.UsersResponse

interface ContactsRepository {
    suspend fun searchUsersByUsername(query:String?, limit:Int, page:Int):SocialResponse<UsersResponse>
}