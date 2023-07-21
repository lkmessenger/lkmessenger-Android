package org.linkmessenger.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.linkmessenger.data.local.dao.SocialDao
import org.linkmessenger.profile.models.ProfileData

@Database(entities = arrayOf(ProfileData::class), version = 9, exportSchema = false)
abstract class SocialDatabase : RoomDatabase() {

    abstract fun socialDao() : SocialDao

    companion object {

        @Volatile
        private var INSTANCE: SocialDatabase? = null

        fun getDatasClient(context: Context) : SocialDatabase {

            if (INSTANCE != null) return INSTANCE!!

            synchronized(this) {

                INSTANCE = Room
                    .databaseBuilder(context, SocialDatabase::class.java, "social_db")
                    .fallbackToDestructiveMigration()
                    .build()

                return INSTANCE!!

            }
        }

    }

}