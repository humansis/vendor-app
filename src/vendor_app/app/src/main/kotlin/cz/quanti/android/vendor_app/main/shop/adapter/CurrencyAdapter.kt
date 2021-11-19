package cz.quanti.android.vendor_app.main.shop.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import cz.quanti.android.vendor_app.R

class CurrencyAdapter(context: Context) : SpinnerAdapter,
    ArrayAdapter<String>(context, R.layout.item_currency) {

    fun init(firstCurrencies: List<String>) {
        addAll(firstCurrencies)
        notifyDataSetChanged()
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)

        (view as TextView).setTextColor(Color.BLACK)
        view.setBackgroundColor(Color.WHITE)

        return view
    }
}
