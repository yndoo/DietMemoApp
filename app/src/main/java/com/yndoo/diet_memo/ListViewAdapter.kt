package com.yndoo.diet_memo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.w3c.dom.Text

class ListViewAdapter(val List: MutableList<DataModel>): BaseAdapter() {
    override fun getCount(): Int {
        return List.size
    }

    override fun getItem(position: Int): Any {
        return List[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null){

            convertView = LayoutInflater.from(parent?.context).inflate(R.layout.listview_item, parent, false)

        }
        val date = convertView?.findViewById<TextView>(R.id.listViewDateArea)
        val workout = convertView?.findViewById<TextView>(R.id.listViewMemoArea1)
        val diet = convertView?.findViewById<TextView>(R.id.listViewMemoArea2)

        date!!.text = List[position].date
        workout!!.text = List[position].workout
        diet!!.text = List[position].dietmemo

        return convertView!!
    }
}