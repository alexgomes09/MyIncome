package com.agomes.myincome.db

import io.realm.RealmObject

/**
 * Created by agomes on 4/13/18.
 */
open class IncomeSchema(
        var date: Long? = null, // saved date
        var startTime: Long? = null,
        var endTime: Long? = null,
        var salary: Float? = null, //total Salary
        var salaryMultiplier: Float? = null//salary set at the time of multiplication
) : RealmObject()