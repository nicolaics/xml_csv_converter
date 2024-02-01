package xml_csv.xmlcsv

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.File

class SelectCsvAndDatabaseActivity: AppCompatActivity() {
    companion object{
        const val EXT_DATABASE_FILE_NAME = "ext_database_file_name"
        const val EXT_CSV_URI_STR = "ext_csv_uri_str"
    }
    private var databaseFileName = ""
    private lateinit var databaseUri : Uri
    private lateinit var databaseUriString : String

    private var csvFileName = ""
    private lateinit var csvUri : Uri
    private lateinit var csvUriString : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_csv_and_database)

        val selectCsvFileButton = findViewById<Button>(R.id.selectCsvFileButton)
        val selectedCsvFileName = findViewById<TextView>(R.id.selectedCsvTextView)
        val selectedMedicineDatabaseTextView = findViewById<TextView>(R.id.selectedMedicineDatabaseTextView)

        val path = applicationContext.externalMediaDirs.first()

        val searchedFile = path.walk().filter { it.name.endsWith(".sqlite-journal")}

        try {
            for(it in searchedFile) {
                if(it.exists()) {
                    it.delete()
                }
            }
        }
        catch(e: Exception){
        }


        val selectCsvFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        selectCsvFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        selectCsvFileIntent.type = "*/*"

        selectCsvFileButton.setOnClickListener {
            startActivityForResult(selectCsvFileIntent, 24)
        }

        val selectMedicineDatabaseButton = findViewById<Button>(R.id.selectMedicineDatabaseButton)
        val selectMedicineDatabaseIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        selectMedicineDatabaseIntent.addCategory(Intent.CATEGORY_OPENABLE)
        selectMedicineDatabaseIntent.type = "*/*"

        selectMedicineDatabaseButton.setOnClickListener {
            startActivityForResult(selectMedicineDatabaseIntent, 39)
        }

        val continueButton = findViewById<Button>(R.id.continueButton)
        continueButton.setOnClickListener {
            if(selectedCsvFileName.text.isNullOrEmpty() || selectedMedicineDatabaseTextView.text.isNullOrEmpty()) {
                Toast.makeText(applicationContext, "Choose the file first!", Toast.LENGTH_LONG).show()
            }
            else{
                val csvToXmlIntent =
                    Intent(this@SelectCsvAndDatabaseActivity, CsvToXmlActivity::class.java).apply {
                        putExtra(EXT_DATABASE_FILE_NAME, databaseFileName)
                        putExtra(EXT_CSV_URI_STR, csvUriString)
                    }

                startActivity(csvToXmlIntent)
                finish()
            }
        }
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == 24 && resultCode == RESULT_OK) {
            csvUri = resultData?.data!!
            csvUriString = resultData.data!!.toString()

            if (csvUriString.startsWith("content://")) {
                var myCursor: Cursor? = null
                try {
                    myCursor =
                        applicationContext!!.contentResolver.query(csvUri, null, null, null, null)
                    if(myCursor != null && myCursor.moveToFirst()) {
                        csvFileName =
                            myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
                finally {
                    myCursor?.close()
                }
            }

            if(csvFileName.endsWith(".csv")) {
               val selectedCsvFileName = findViewById<TextView>(R.id.selectedCsvTextView)
               selectedCsvFileName.text = csvFileName
            }
            else{
                Toast.makeText(applicationContext,"File format is wrong!", Toast.LENGTH_LONG).show()
            }
        }

        else if (requestCode == 39 && resultCode == RESULT_OK) {
            databaseUri = resultData?.data!!
            databaseUriString = resultData.data!!.toString()

            if (databaseUriString.startsWith("content://")) {
                var myCursor: Cursor? = null
                try {
                    myCursor =
                        applicationContext!!.contentResolver.query(databaseUri, null, null, null, null)
                    if(myCursor != null && myCursor.moveToFirst()) {
                        databaseFileName =
                            myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
                finally {
                    myCursor?.close()
                }
            }

            if(databaseFileName.endsWith(".sqlite")) {
                val selectedMedicineDatabaseTextView = findViewById<TextView>(R.id.selectedMedicineDatabaseTextView)
                selectedMedicineDatabaseTextView.text = databaseFileName
            }
            else{
                Toast.makeText(applicationContext,"File format is wrong!", Toast.LENGTH_LONG).show()
            }
        }
    }
}