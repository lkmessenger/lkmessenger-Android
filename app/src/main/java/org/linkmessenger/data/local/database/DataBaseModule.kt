package org.linkmessenger.data.local.database

import org.koin.dsl.module
import org.linkmessenger.data.local.dao.PostsDao
import org.linkmessenger.data.local.dao.PostsDaoImpl

val databaseModule = module {
    factory { providePostsDao() }
    single { SocialDatabase.getDatasClient(get()) }
}
fun providePostsDao(): PostsDao {
    return PostsDaoImpl()
}