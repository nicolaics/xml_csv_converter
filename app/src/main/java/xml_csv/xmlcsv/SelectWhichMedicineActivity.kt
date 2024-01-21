package xml_csv.xmlcsv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SelectWhichMedicineActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_which_medicine)

        val typeToken = object : TypeToken<ArrayList<SearchedMedicine>>() {}.type
        val selectMedicineList = Gson().fromJson<ArrayList<SearchedMedicine>>(intent.getStringExtra(CsvToXmlActivity.EXT_MEDICINE_RESULT), typeToken)

        val medicineListView = findViewById<ListView>(R.id.medicineListView)
        val finishSelectMedicineButton = findViewById<Button>(R.id.finishSelectMedicineButton)

        val selectMedicineAdapter = SelectMedicineAdapter(applicationContext, selectMedicineList)
        medicineListView.adapter = selectMedicineAdapter

        finishSelectMedicineButton.setOnClickListener {
            var hasChosen = true

            for(iter in selectMedicineList){
                if(iter.chosenMedicine.size != 1){
                    Toast.makeText(applicationContext, "Choose only 1 Medicines", Toast.LENGTH_LONG).show()
                    hasChosen = false
                    break
                }
            }

            if(hasChosen) {
                val data = Gson().toJson(selectMedicineList)

                val sendBackIntent = Intent()
                sendBackIntent.putExtra(CsvToXmlActivity.EXT_MEDICINE_RESULT, data)
                setResult(RESULT_OK, sendBackIntent)
                finish()
            }
        }
    }
}