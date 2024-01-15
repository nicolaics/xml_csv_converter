package xml_csv.xmlcsv

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
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

        val medicineListTextView = generatedView.findViewById<TextView>(R.id.listTextView)
        val okButton = generatedView.findViewById<Button>(R.id.okButton)
        val chooseMedicineEditText = generatedView.findViewById<EditText>(R.id.chooseMedicineEditText)
        val inputtedMedicineTextView = generatedView.findViewById<TextView>(R.id.inputtedMedicineTextView)
        val invoiceNoTextView = generatedView.findViewById<TextView>(R.id.invoiceNoTextView)
        val choiceSavedTextView = generatedView.findViewById<TextView>(R.id.choiceSavedTextView)

        inputtedMedicineTextView.text = "You inputted: ${selectMedicineList[p0].inputtedMedicine}"
        invoiceNoTextView.text = "Invoice No: ${selectMedicineList[p0].invoiceNo}"

        var itemCount = 1

        selectMedicineList[p0].searchResult.forEach {
            medicineListTextView.append("${itemCount}. ${it.medicineName}\n\n")
            itemCount++
        }

        okButton.setOnClickListener {
            val choice = chooseMedicineEditText.text.toString().toInt()

            if(choice in 1 until itemCount) {
                chooseMedicineEditText.setTextColor(ContextCompat.getColor(context, R.color.green))

                choiceSavedTextView.text = "Saved:\n$choice"

                selectMedicineList[p0].choice = (choice - 1)
            }
            else{
                chooseMedicineEditText.setTextColor(ContextCompat.getColor(context, R.color.dark_grey))
                choiceSavedTextView.text = ""
                Toast.makeText(context, "Choice invalid!", Toast.LENGTH_SHORT).show()
            }
        }

        return generatedView
    }
}