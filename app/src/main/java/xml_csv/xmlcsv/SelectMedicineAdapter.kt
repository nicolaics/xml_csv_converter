package xml_csv.xmlcsv

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

class SelectMedicineAdapter(val context: Context, var selectMedicineList : ArrayList<SearchedMedicine>) : BaseAdapter() {
    override fun getCount(): Int {
        return selectMedicineList.size
    }

    override fun getItem(p0: Int): Any {
        return selectMedicineList.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val generatedView: View = inflater.inflate(R.layout.select_each_medicine, null)

        val inputtedMedicineTextView = generatedView.findViewById<TextView>(R.id.inputtedMedicineTextView)
        val invoiceNoTextView = generatedView.findViewById<TextView>(R.id.invoiceNoTextView)
        val medicineRadioButtonListView = generatedView.findViewById<ListView>(R.id.medicineRadioButtonListView)

        var medicineName = selectMedicineList[p0].inputtedMedicine

        val re = Regex(".")

        if(medicineName.contains(re)){
            medicineName = medicineName.replace(".", ",")
        }

        inputtedMedicineTextView.text = "You inputted: ${medicineName}"
        invoiceNoTextView.text = "Invoice No: ${selectMedicineList[p0].invoiceNo}"


        val medicineRadioButtonAdaptor = MedicineRadioButtonAdaptor(context, selectMedicineList[p0].searchResult,
                                                                    selectMedicineList, p0)
        medicineRadioButtonListView.adapter = medicineRadioButtonAdaptor

        return generatedView
    }
}