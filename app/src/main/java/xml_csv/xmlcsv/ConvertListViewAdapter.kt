package xml_csv.xmlcsv

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ConvertListViewAdapter(val context: Context, val invoices: ArrayList<Long>) : BaseAdapter() {
    override fun getCount(): Int {
        return invoices.size
    }

    override fun getItem(p0: Int): Any {
        return invoices.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val generatedView: View = inflater.inflate(R.layout.convert_adapter, null)

        val convertingTextView = generatedView.findViewById<TextView>(R.id.xmlToCsvConvertingTextView)

        convertingTextView.text = "Converting INVOICE NO: ${invoices.get(p0)}"

        return  generatedView
    }
}