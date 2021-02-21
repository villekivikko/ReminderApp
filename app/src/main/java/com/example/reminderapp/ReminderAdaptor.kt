package com.example.reminderapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.reminderapp.databinding.ReminderItemBinding


class ReminderAdaptor(context: Context, private  val list:List<ReminderInfo>): BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, container: ViewGroup?): View {
        val rowBinding = ReminderItemBinding.inflate(inflater, container, false)

        //set reminder info values to the list item
        rowBinding.txtMessage.text=list[position].message
        rowBinding.txtReminderTime.text=list[position].reminder_time
        rowBinding.txtCreationTime.text=list[position].creation_time
        rowBinding.txtLocationX.text=list[position].location_x.toString()
        rowBinding.txtLocationY.text=list[position].location_y.toString()
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
