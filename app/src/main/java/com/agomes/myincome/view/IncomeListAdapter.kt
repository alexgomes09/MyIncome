package com.agomes.myincome.view

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agomes.myincome.R
import com.agomes.myincome.db.IncomeSchema
import io.realm.RealmResults
import org.joda.time.DateTime
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by agomes on 4/14/18.
 */
public class IncomeListAdapter(
        var listOfIncome: RealmResults<IncomeSchema>,
        var fragment: IncomeListFragment,
        val context: Context) : RecyclerView.Adapter<IncomeListAdapter.IncomeViewHolder>() {

    var selectionMode: Boolean = false
    var selectedList: ArrayList<Int> = ArrayList(0)
    var totalAllEarned: Float = 0f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_single_item, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        holder.dateAdded.text = "${DateTime(listOfIncome[position]?.date).toString("EEE, MMM d", Locale.ENGLISH)}"
        holder.startEndTime.text = "${DateTime(listOfIncome[position]?.startTime).toString("hh:mm a")} - ${DateTime(listOfIncome[position]?.endTime).toString("hh:mm a")}"
        holder.salaryMultiplier.text = "$${listOfIncome[position]?.salaryMultiplier}"
        holder.totalErned.text = "$${listOfIncome[position]?.salary.toString()}"

        holder.itemView.isSelected = selectedList.contains(position)

        totalAllEarned += listOfIncome[position]?.salary!!
        Log.v("==TAG==", "IncomeListAdapter.onBindViewHolder " +totalAllEarned)
    }

    override fun getItemCount(): Int {
        return listOfIncome.size
    }

    fun cancelSelection() {
        selectedList.clear()
        notifyDataSetChanged()
        selectionMode = false
    }

    inner class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateAdded = itemView.findViewById<AppCompatTextView>(R.id.date_added)
        val startEndTime = itemView.findViewById<AppCompatTextView>(R.id.start_end_time)
        val salaryMultiplier = itemView.findViewById<AppCompatTextView>(R.id.salary_multiplier)
        val totalErned = itemView.findViewById<AppCompatTextView>(R.id.total_earned)

        init {
            itemView.setOnLongClickListener({
                selectionMode = true
                it.isSelected = true
                selectedList.add(adapterPosition)
                fragment.updateSelectionCounter(selectedList.size)
                true
            })

            itemView.setOnClickListener {
                if (selectionMode && !selectedList.contains(adapterPosition)) {
                    selectedList.add(adapterPosition)
                    itemView.isSelected = true
                    itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                } else if (selectionMode && selectedList.contains(adapterPosition)) {
                    itemView.isSelected = false
                    selectedList.remove(adapterPosition)
                }

                fragment.updateSelectionCounter(selectedList.size)
                if (selectedList.size == 0) {
                    selectionMode = false
                }
            }
        }
    }
}