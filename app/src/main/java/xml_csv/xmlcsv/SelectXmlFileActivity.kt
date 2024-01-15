package xml_csv.xmlcsv

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.Toast

class SelectXmlFileActivity: AppCompatActivity() {
    companion object {
        const val EXT_URI_STRING = "EXT_URI_STRING"
        const val EXT_FILE_NAME = "EXT_FILE_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_xml_file)

        val selectFileButton = findViewById<Button>(R.id.selectXmlFileButton)

        val selectFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        selectFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        selectFileIntent.type = "*/*"

        selectFileButton.setOnClickListener {
            startActivityForResult(selectFileIntent, 17)
        }
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == 17 && resultCode == RESULT_OK) {
            val xmlUri : Uri = resultData?.data!!
            val xmlUriString : String = resultData.data!!.toString()
            var xmlFileName = ""

            if (xmlUriString.startsWith("content://")) {
                var myCursor: Cursor? = null
                try {
                    myCursor =
                        applicationContext!!.contentResolver.query(xmlUri, null, null, null, null)
                    if(myCursor != null && myCursor.moveToFirst()) {
                        xmlFileName =
                            myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
                finally {
                    myCursor?.close()
                }
            }

            if(xmlFileName.endsWith(".xml")) {
                val xmlToCsvIntent =
                    Intent(this@SelectXmlFileActivity, XmlToCsvActivity::class.java).apply {
                        putExtra(EXT_URI_STRING, xmlUriString)
                        putExtra(EXT_FILE_NAME, xmlFileName)
                    }
                startActivity(xmlToCsvIntent)

                finish()
            }
            else{
                Toast.makeText(applicationContext,"File format is wrong!", Toast.LENGTH_LONG).show()
            }
        }
    }
}

