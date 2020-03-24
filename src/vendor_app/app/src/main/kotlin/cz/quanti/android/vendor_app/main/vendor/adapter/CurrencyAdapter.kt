package cz.quanti.android.vendor_app.main.vendor.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.utils.Constants

class CurrencyAdapter(context: Context) : SpinnerAdapter, ArrayAdapter<String>(context, R.layout.item_currency) {

    fun init(firstCurrencies: List<String>) {
        val currencies: MutableList<String> = Constants.SUPPORTED_CURRENCIES.filter {
            it.currencyCode != null
        }.map {
            it.currencyCode
        }.toMutableList()

        currencies.sort()
        currencies.removeAll(firstCurrencies)

        addAll(firstCurrencies)
        addAll(currencies)
        notifyDataSetChanged()
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = super.getDropDownView(position, convertView, parent)

        (view as TextView)?.setTextColor(Color.BLACK)
        view.setBackgroundColor(Color.WHITE)

        return view
    }
}
