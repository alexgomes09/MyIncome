package com.agomes.myincome.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agomes.myincome.MyIncomeApplication
import com.agomes.myincome.R
import com.agomes.myincome.db.IncomeSchema
import io.realm.Realm
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

        showEmptyHolder(listOfIncome.size == 0)

        selection_count.setOnClickListener {
            incomeListAdapter.cancelSelection()
            selection_count.visibility = View.GONE
        }

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

    fun updateSelectionCounter(value: Int) {
        if (selection_count.visibility == View.GONE) {
            selection_count.visibility = View.VISIBLE
        }

        if (value == 0) {
            selection_count.visibility = View.GONE
        }

        selection_count.setText("CANCEL ${value} SELECTION")
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