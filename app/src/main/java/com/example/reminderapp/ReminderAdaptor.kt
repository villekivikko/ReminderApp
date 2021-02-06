package com.example.reminderapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.reminderapp.databinding.ReminderItemBinding
import com.example.reminderapp.db.ReminderInfo


class ReminderAdaptor(context: Context, private  val list:List<ReminderInfo>): BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, container: ViewGroup?): View {
        val rowBinding = ReminderItemBinding.inflate(inflater, container, false)

        //set reminder info values to the list item
        rowBinding.txtName.text=list[position].name
        rowBinding.txtDate.text=list[position].date
        rowBinding.txtTime.text=list[position].time
        rowBinding.txtLocation.text=list[position].location
        return  rowBinding.root
    }
    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}
