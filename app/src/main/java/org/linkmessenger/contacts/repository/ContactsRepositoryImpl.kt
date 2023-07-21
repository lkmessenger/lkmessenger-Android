package org.linkmessenger.contacts.repository

import org.linkmessenger.request.api.SocialApi
import org.linkmessenger.request.models.SocialResponse
import org.linkmessenger.request.models.UsersResponse

class ContactsRepositoryImpl(val socialApi: SocialApi):ContactsRepository {
    override suspend fun searchUsersByUsername(query: String?, limit:Int, page:Int): SocialResponse<UsersResponse> {
        return socialApi.searchUsersByUsername(query, limit, page)
    }
}