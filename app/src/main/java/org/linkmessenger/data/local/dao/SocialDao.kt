package org.linkmessenger.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.linkmessenger.profile.models.ProfileData

@Dao
interface SocialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelfProfile(profileData: ProfileData)
    @Query("SELECT * FROM profile LIMIT 1")
    suspend fun getSelfProfile() : ProfileData?
}