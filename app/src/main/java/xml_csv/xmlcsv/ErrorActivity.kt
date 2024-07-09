package xml_csv.xmlcsv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ErrorActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        val errorOkButton = findViewById<Button>(R.id.errorOkButton)
        val errorType = intent.getIntExtra(CsvToXmlActivity.EXT_ERROR_TYPE, 0)

        val errorBodyTextView = findViewById<TextView>(R.id.errorBodyTextView)
        val errorTitleTextView = findViewById<TextView>(R.id.errorTitleTextView)

        if(errorType == 0) {
            val logFileName = intent.getStringExtra(CsvToXmlActivity.EXT_LOG)

            errorTitleTextView.text = "Something Error!"
            errorBodyTextView.text = "Check log at:\n$logFileName"
        }
        else if(errorType == 1){
           errorTitleTextView.text = "Medicine Not Found!"

            val typeToken = object : TypeToken<ArrayList<Medicine>>() {}.type
            val medicineNotFound = Gson().fromJson<ArrayList<Medicine>>(intent.getStringExtra(CsvToXmlActivity.EXT_MEDICINE_NOT_FOUND), typeToken)

            medicineNotFound.forEach{
                errorBodyTextView.append("Invoice No: ${it.invoiceNo}\nMedicine Name: ${it.medicineName}\n\n")
            }
        }
        else if(errorType == 2){
            errorTitleTextView.text = "Please check these medicines again"

            val typeToken = object : TypeToken<Map<Long, String>>() {}.type
            val medicineNotFound = Gson().fromJson<Map<Long, String>>(intent.getStringExtra(CsvToXmlActivity.EXT_MEDICINE_NOT_FOUND), typeToken)

            medicineNotFound.forEach{
                errorBodyTextView.append("Invoice No: ${it.key}\nMedicine Name: ${it.value}\n\n")
            }
        }

        errorOkButton.setOnClickListener {
            finish()
        }
    }
}