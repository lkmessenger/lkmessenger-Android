package org.linkmessenger

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.linkmessenger.data.local.database.databaseModule
import org.linkmessenger.modules.appModule
import org.linkmessenger.request.networkModule

fun init(myApp:Application){
    initRealm(myApp)
    startKoin{
        androidLogger()
        androidContext(myApp)
        modules(appModule, networkModule, databaseModule)
    }
}
private fun initRealm(myApp: Application) {
    Realm.init(myApp.applicationContext)
    val config = RealmConfiguration.Builder()
        .name("social")
        .deleteRealmIfMigrationNeeded()
        .build()
    Realm.setDefaultConfiguration(config);
}