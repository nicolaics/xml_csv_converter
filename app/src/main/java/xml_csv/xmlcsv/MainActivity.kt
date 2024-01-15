package xml_csv.xmlcsv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val choiceEditText = findViewById<EditText>(R.id.choiceEditText)
        val nextButton = findViewById<Button>(R.id.nextButton)

        nextButton.setOnClickListener {
            try {
                val choice = choiceEditText.text.toString().toInt()

                if(choice == 1){
                    val selectXmlFileActivityIntent = Intent(this, SelectXmlFileActivity::class.java)
                    startActivity(selectXmlFileActivityIntent)
                }
                else if(choice == 2){
                    val selectCsvFileActivityIntent = Intent(this, SelectCsvAndDatabaseActivity::class.java)
                    startActivity(selectCsvFileActivityIntent)
                }
                else{
                    Toast.makeText(this, "Choice Invalid!", Toast.LENGTH_LONG).show()
                }

                choiceEditText.text.clear()
            }
            catch(e: Exception){
                Toast.makeText(this, "Choice Invalid!", Toast.LENGTH_LONG).show()
            }
        }
    }
}