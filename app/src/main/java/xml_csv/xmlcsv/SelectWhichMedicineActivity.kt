package xml_csv.xmlcsv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
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
            selectMedicineList.forEach {
                println(it.choice)
            }
            val data = Gson().toJson(selectMedicineList)

            val sendBackIntent = Intent()
            sendBackIntent.putExtra(CsvToXmlActivity.EXT_MEDICINE_RESULT, data)
            setResult(RESULT_OK, sendBackIntent)
            finish()
        }
    }
}