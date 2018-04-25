package com.agomes.myincome.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agomes.myincome.MyIncomeApplication
import com.agomes.myincome.R
import com.agomes.myincome.db.IncomeSchema
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_list.*


/**
 * Created by agomes on 4/14/18.
 */
class IncomeListFragment : Fragment() {

    private lateinit var incomeListAdapter: IncomeListAdapter
    private lateinit var mContext: Context
    private lateinit var listOfIncome: RealmResults<IncomeSchema>
    private var realm = MyIncomeApplication.getRealmInstance()
    private var totalAllEarned: Float = 0f
    private var totalHoursWorked: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listOfIncome = realm.where(IncomeSchema::class.java).findAll().sort("date")

        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(mContext)
        incomeListAdapter = com.agomes.myincome.view.IncomeListAdapter(listOfIncome, this, mContext)
        recycler_view.adapter = incomeListAdapter

        calculateTotalHourAndEarned(listOfIncome)

        showEmptyHolder(listOfIncome.size == 0)

        selection_count.setOnClickListener {
            incomeListAdapter.cancelSelection()
            selection_count.visibility = View.GONE
        }

        listOfIncome.addChangeListener(object : RealmChangeListener<RealmResults<IncomeSchema>> {
            override fun onChange(t: RealmResults<IncomeSchema>) {
                calculateTotalHourAndEarned(t)
            }
        })

        delete_all.setOnClickListener {
            if (incomeListAdapter.selectionMode) {
                //delete single
                realm.executeTransaction {
                    incomeListAdapter.selectedList.sortedDescending().forEach({
                        listOfIncome.deleteFromRealm(it)
                    })
                }

                Realm.compactRealm(realm.configuration)
                selection_count.performClick()
                showEmptyHolder(listOfIncome.size <= 0)
                incomeListAdapter.notifyDataSetChanged()
            } else {
                //delete all
                AlertDialog.Builder(mContext)
                        .setTitle(R.string.app_name)
                        .setMessage("Would you like to delete all record?")
                        .setPositiveButton("Yes", { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                            realm.executeTransaction {
                                listOfIncome.deleteAllFromRealm()
                                incomeListAdapter.notifyDataSetChanged()
                                showEmptyHolder(true)
                            }
                        })
                        .setNegativeButton("No", { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        })
                        .create()
                        .show()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onDestroy() {
        super.onDestroy()
        listOfIncome.removeAllChangeListeners()
    }

    fun calculateTotalHourAndEarned(data: RealmResults<IncomeSchema>) {
        totalAllEarned = 0F
        totalHoursWorked = 0
        data.forEachIndexed { index, incomeSchema ->
            totalAllEarned += incomeSchema.salary!!
            totalHoursWorked += incomeSchema.endTime?.minus(incomeSchema.startTime!!)!!
        }

        total_hours_worked.text = "Total: ${org.joda.time.Period(totalHoursWorked).hours}hr ${org.joda.time.Period(totalHoursWorked).minutes}min"
        total_salary_earned.text = "$%.2f".format(totalAllEarned)
    }

    fun updateSelectionCounter(value: Int) {
        if (selection_count.visibility == View.GONE) {
            selection_count.visibility = View.VISIBLE
        }

        if (value == 0) {
            selection_count.visibility = View.GONE
        }

        selection_count.setText("CANCEL $value SELECTION")
    }

    fun showEmptyHolder(show: Boolean) {
        if (show) {
            empty_view.visibility = View.VISIBLE
            delete_all.visibility = View.GONE
            selection_count.visibility = View.GONE
        } else {
            empty_view.visibility = View.GONE
            delete_all.visibility = View.VISIBLE
        }
    }
}