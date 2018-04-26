package com.agomes.myincome

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.agomes.myincome.db.IncomeMigration
import com.agomes.myincome.util.Constants
import io.realm.Realm
import io.realm.RealmConfiguration
/**
 * Created by agomes on 4/8/18.
 */
class MyIncomeApplication : Application() {

    companion object {

        private lateinit var instance: MyIncomeApplication
        private lateinit var realmConfig: RealmConfiguration

        fun getPreferences(): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(instance)
        }

        fun getRealmInstance(): Realm {
            return Realm.getInstance(realmConfig)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Realm.init(this)

        realmConfig = RealmConfiguration.Builder()
                .name("myIncome.realm")
                .schemaVersion(Constants.relamSchemaVersion)
                .migration(IncomeMigration())
                .build()
    }
}