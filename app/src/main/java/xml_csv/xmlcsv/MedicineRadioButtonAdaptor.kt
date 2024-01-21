package xml_csv.xmlcsv

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import androidx.core.content.ContextCompat

class MedicineRadioButtonAdaptor(val context: Context, val searchedResult : ArrayList<Medicine>,
                                 var selectMedicineList : ArrayList<SearchedMedicine>, val index : Int ) : BaseAdapter() {
    override fun getCount(): Int {
        return searchedResult.size
    }

    override fun getItem(p0: Int): Any {
        return searchedResult.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val generatedView: View = inflater.inflate(R.layout.medicine_radio_button, null)

        val medicineCheckBox = generatedView.findViewById<CheckBox>(R.id.medicineCheckBox)

        medicineCheckBox.text = searchedResult[p0].medicineName

        val chosenMedicine = medicineCheckBox.text.toString()

        when(context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                medicineCheckBox.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            else -> medicineCheckBox.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        medicineCheckBox.setOnCheckedChangeListener { button, isChecked ->
            if(isChecked){
                println(medicineCheckBox.text)
                selectMedicineList[index].chosenMedicine.add(chosenMedicine)
            }
            else{
                selectMedicineList[index].chosenMedicine.remove(chosenMedicine)
            }
        }

        return generatedView
    }
}