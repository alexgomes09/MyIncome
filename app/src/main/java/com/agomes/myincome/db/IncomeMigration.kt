package com.agomes.myincome.db

import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.RealmSchema

/**
 * Created by agomes on 4/13/18.
 */
class IncomeMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {

        val schema: RealmSchema = realm.schema

//        if (oldVersion == 0L) {
//            val incomeSchema : RealmObjectSchema = schema.get(IncomeSchema::class.java.simpleName)!!
//            incomeSchema.addField("test", String::class.java)
//            oldVersion.plus(1)
//        }
//
//        if (oldVersion == 1L) {
//            val incomeSchema : RealmObjectSchema = schema.get(IncomeSchema::class.java.simpleName)!!
//            incomeSchema.removeField("test")
//            oldVersion.plus(1)
//        }
    }
}