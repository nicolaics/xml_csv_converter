package xml_csv.xmlcsv

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.SAXParserFactory

class XmlToCsvActivity: AppCompatActivity() {
    private var lastInvoice : Long = 0
    private var lastMedicine = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xml_to_csv)

        val uriString = intent.getStringExtra(SelectXmlFileActivity.EXT_URI_STRING)
        val xmlFileName = intent.getStringExtra(SelectXmlFileActivity.EXT_FILE_NAME)

        val fileName = xmlFileName!!.removeSuffix(".xml")

        val csvFileName = "$fileName.csv"

        val xmlUri = Uri.parse(uriString)

        var invoicesList = ArrayList<Invoice>()

        val path = applicationContext.externalMediaDirs.first()

        val databaseFile = File(path, "temp_database.sqlite").path

        val databaseVersionFile = File(path, "Database_Version.txt")
        var databaseVersion = 1

        try {
            databaseVersion = databaseVersionFile.readText().toInt()
            databaseVersion++

            databaseVersionFile.writeText("$databaseVersion")
        }
        catch(e: java.lang.Exception) {
            databaseVersionFile.writeText("$databaseVersion")
        }

        try {
            contentResolver.openInputStream(xmlUri)?.use {inputStream ->
                invoicesList = parse(inputStream, databaseFile, databaseVersion)
            }

            val date = LocalDate.now().toString()

            val dir = File(path, date)
            dir.mkdirs()

            val csvFile = File(dir, csvFileName)

            FileOutputStream(csvFile).apply {
                writeCsv(invoicesList)
            }

            val database = DatabaseHelper(applicationContext, null, databaseFile, databaseVersion)
            database.addData(invoicesList)

            val fileCreatedTextView = findViewById<TextView>(R.id.fileCreatedTextView)
            fileCreatedTextView.text = "File succesfully created at\n${csvFile.path}"
        }
        catch(e: java.lang.Exception){
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            val dateTime = LocalDateTime.now().format(formatter).toString()

            val logFileName = "Log_${dateTime}.txt"

            val logFilePath = File(path, logFileName)
            val stringWriter = StringWriter()
            e.printStackTrace(PrintWriter(stringWriter))

            var exceptionAsString = stringWriter.toString()
            exceptionAsString += "\n\n"
            exceptionAsString += "Last Invoice: ${lastInvoice}\n"
            exceptionAsString += "Last Medicine: ${lastMedicine}\n"

            logFilePath.writeText(exceptionAsString)

            val errorIntent = Intent(this@XmlToCsvActivity, ErrorActivity::class.java).apply{
                putExtra(CsvToXmlActivity.EXT_LOG, logFileName)
                putExtra(CsvToXmlActivity.EXT_ERROR_TYPE, 0)
            }
            startActivity(errorIntent)

            finish()
        }

        val finishButton = findViewById<Button>(R.id.finishButton)
        finishButton.setOnClickListener {
            finish()
        }
    }

    private fun OutputStream.writeCsv(invoicesList : ArrayList<Invoice>){
        val writer = bufferedWriter()
        writer.write("""INVOICE_NO, INVOICE_DATE, INVOICE_AMOUNT, DESCRIPTION, ITEMS_NO, BARCODE, ITEM_NAME, QUANTITY, ITEM_UNIT, UNIT_PRICE""")
        writer.newLine()

        for(i in 0 until invoicesList.size){
            writer.write("NEW")
            writer.newLine()
            writer.write("${invoicesList[i].invoiceNo}, ${invoicesList[i].invoiceDate}, ${invoicesList[i].invoiceAmount}, ${invoicesList[i].description},")

            for(j in 0 until invoicesList[i].itemLines.size){
                if(j == 0) {
                    writer.write("${j + 1}, ${invoicesList[i].itemLines[j].barcode}," +
                            "${invoicesList[i].itemLines[j].medicineName}," +
                            "${invoicesList[i].itemLines[j].quantity}," +
                            "${invoicesList[i].itemLines[j].itemUnit}," +
                            "${invoicesList[i].itemLines[j].unitPrice}")
                }
                else{
                    writer.write(",,,, ${j + 1}," +
                        "${invoicesList[i].itemLines[j].barcode}," +
                            "${invoicesList[i].itemLines[j].medicineName}," +
                            "${invoicesList[i].itemLines[j].quantity}," +
                            "${invoicesList[i].itemLines[j].itemUnit}," +
                            "${invoicesList[i].itemLines[j].unitPrice}")
                }

                writer.newLine()
            }
        }

        writer.write("DONE")
        writer.flush()
        writer.close()
    }

    private fun parse(inputStream: InputStream, databaseFile: String, databaseVersion: Int) : ArrayList<Invoice>{
//    private fun parse(inputStream: InputStream, databaseFile: String, databaseVersion: Int) = CoroutineScope(Dispatchers.Default).async{
        val parserFactory = SAXParserFactory.newInstance()
        val parser = parserFactory.newSAXParser()

        var itemLine = ItemLine()
        var invoice = Invoice()
        var salesman = Salesman()
        val invoices = ArrayList<Invoice>()

        var invoiceWarehouse = false
        var insertInvoice = true

        val defaultHandler = object : DefaultHandler(){
            var currentValue = ""
            var currentElement = false

            override fun startElement(
                uri: String?,
                localName: String?,
                qName: String?,
                attributes: Attributes?
            ) {
                currentElement = true
                currentValue = ""

                if(localName == "SALESINVOICE" && attributes?.getValue("operation") == "Add"){
                    invoice = Invoice()
                    invoiceWarehouse = false
                    insertInvoice = true
                }
                else if(localName == "SALESINVOICE" && attributes?.getValue("operation") != "Add"){
                    insertInvoice = false
                }
                else if(localName == "ITEMLINE" && insertInvoice){
                    itemLine = ItemLine()
                }
                else if(localName == "SALESMANID" && insertInvoice){
                    salesman = Salesman()
                }
            }

            override fun endElement(uri: String?, localName: String?, qName: String?) {
                if(insertInvoice) {
                    currentElement = false

                    if(localName.equals("TRANSACTIONID")){
                        invoice.transactionId = convertLong(currentValue)!!
                    }
                    else if(localName.equals("ITEMNO")) {
                        println("barcode: $currentValue")
                        itemLine.barcode = currentValue
                    }
                    else if(localName.equals("QUANTITY")) {
                        println(currentValue)
                        itemLine.quantity = convertFloat(currentValue)!!
                    }
                    else if(localName.equals("ITEMUNIT")){
                        itemLine.itemUnit = currentValue
                    }
                    else if(localName.equals("UNITRATIO")){
                        itemLine.unitRatio = convertInt(currentValue)?: 1
                    }
                    else if(localName.equals("ITEMRESERVED1")){
                        itemLine.itemReserved1 = currentValue
                    }
                    else if(localName.equals("ITEMOVDESC")){
                        val re = Regex(",")

                        if(currentValue.contains(re)){
                            currentValue = currentValue.replace(",", ".")
                        }

                        lastMedicine = currentValue
                        itemLine.medicineName = currentValue
                    }
                    else if(localName.equals("UNITPRICE")){
                        itemLine.unitPrice = convertDouble(currentValue)?: 0.0
                    }
                    else if(localName.equals("ITEMDISCPC")){
                        itemLine.itemDiscPc = convertDouble(currentValue)
                    }
                    else if(localName.equals("BRUTOUNITPRICE")){
                        itemLine.brutoUnitPrice = convertDouble(currentValue)?: 0.0
                    }
                    else if(localName.equals("QTYCONTROL")){
                        itemLine.qtyControl = convertInt(currentValue)?: 0
                    }
                    else if(localName.equals("INVOICENO")){
                        lastInvoice = convertLong(currentValue)!!
                        invoice.invoiceNo = convertLong(currentValue)!!
                    }
                    else if(localName.equals("INVOICEDATE")){
                        invoice.invoiceDate = currentValue
                    }
                    else if(localName.equals("CASHDISCOUNT")){
                        invoice.cashDiscount = convertDouble(currentValue)?: 0.0
                    }
                    else if(localName.equals("INVOICEAMOUNT")){
                        invoice.invoiceAmount = convertDouble(currentValue)?: 0.0
                    }
                    else if(localName.equals("DESCRIPTION")){
                        invoice.description = currentValue
                    }
                    else if(localName.equals("SHIPDATE")){
                        invoice.shipDate = currentValue
                    }
                    else if(localName.equals("TAXDATE")){
                        invoice.taxDate = currentValue
                    }
                    else if(localName.equals("CUSTOMERID")){
                        invoice.customer = currentValue
                    }
                    else if(localName.equals("LASTNAME")){
                        salesman.setLastName(currentValue)
                    }
                    else if(localName.equals("FIRSTNAME")) {
                        salesman.setFirstName(currentValue)
                    }
                    else if(localName.equals("SALESMANID")) {
                        invoice.salesman = salesman
                    }
                    else if(localName.equals("PRINTED")) {
                        invoice.printed = convertInt(currentValue)?: 1
                    }
                    else if(localName.equals("SHIPTO1")) {
                        invoice.shipTo1 = currentValue
                    }
                    else if(localName.equals("ARACCOUNT")) {
                        invoice.arAccount = convertDouble(currentValue)!!
                    }
                    else if(localName.equals("ITEMLINE")) {
                        invoice.itemLines.add(itemLine)
                    }
                    else if(localName.equals("SALESINVOICE")) {
                        invoices.add(invoice)
                    }
                }
            }

            override fun characters(ch: CharArray, start: Int, length: Int) {
                if(currentElement){
                    currentValue += String(ch, start, length)
                }
            }
        }

        parser.parse(inputStream, defaultHandler)

        val xmlToCsvListView = findViewById<ListView>(R.id.xmlToCsvListView)
        val xmlToCsvListViewAdapter = XmlToCsvListViewAdapter(applicationContext, invoices)

        xmlToCsvListView.adapter = xmlToCsvListViewAdapter

//        CoroutineScope(Dispatchers.Main).launch{
//            val xmlToCsvListView = findViewById<ListView>(R.id.xmlToCsvListView)
//
//            val xmlToCsvListViewAdapter = XmlToCsvListViewAdapter(applicationContext, invoices)
//            xmlToCsvListView.adapter = xmlToCsvListViewAdapter
//        }

        return invoices
//        return@async invoices
    }

    private fun convertInt(text : String) : Int? {
        return try{
            text.toInt()
        }
        catch(e: java.lang.NumberFormatException){
            null
        }
    }

    private fun convertDouble(text : String) : Double? {
        return try {
            text.toDouble()
        }
        catch(e: java.lang.NumberFormatException) {
            null
        }
    }

    private fun convertLong(text : String) : Long? {
        return try {
            text.toLong()
        }
        catch(e: java.lang.NumberFormatException) {
            null
        }
    }

    private fun convertFloat(text : String) : Float? {
        return try {
            text.toFloat()
        }
        catch(e: java.lang.NumberFormatException) {
            null
        }
    }
}