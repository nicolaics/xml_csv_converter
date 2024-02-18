package xml_csv.xmlcsv

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Xml
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class CsvToXmlActivity: AppCompatActivity() {
    companion object{
        const val EXT_MEDICINE_RESULT = "ext_medicine_result"
        const val EXT_LOG = "ext_log"
        const val EXT_ERROR_TYPE = "ext_error_type"
        const val EXT_MEDICINE_NOT_FOUND = "ext_medicine_not_found"
    }

    private var selection = 0
    private var searchResult = ArrayList<Medicine>()
    private var selectMedicineList = ArrayList<SearchedMedicine>()
    private var invoicesList = ArrayList<Invoice>()
    private var medicineNotFoundList = ArrayList<Medicine>()

    private var lastInvoice : Long = 0
    private var lastMedicine = ""

    private lateinit var csvToXmlFinishButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csv_to_xml)

        val csvUriString = intent.getStringExtra(SelectCsvAndDatabaseActivity.EXT_CSV_URI_STR)

        val csvUri = Uri.parse(csvUriString)

        val path = applicationContext.externalMediaDirs.first()

        val databaseFile = File(path, "temp_database.sqlite").path
        val databaseVersionFile = File(path, "Database_Version.txt")

        val databaseVersion = databaseVersionFile.readText().toInt()

        csvToXmlFinishButton = findViewById<Button>(R.id.finishCsvToXmlButton)
        csvToXmlFinishButton.isEnabled = false

        val convertListView = findViewById<ListView>(R.id.csvToXmlListView)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                contentResolver.openInputStream(csvUri)?.use {
                        inputStream -> invoicesList = readCsv(inputStream, databaseFile, databaseVersion, convertListView)
                }

                if(selectMedicineList.size > 0) {
                    val data = Gson().toJson(selectMedicineList)

                    val selectWhichMedicineIntent =
                        Intent(
                            this@CsvToXmlActivity,
                            SelectWhichMedicineActivity::class.java
                        ).apply {
                            putExtra(EXT_MEDICINE_RESULT, data)
                        }
                    startActivityForResult(selectWhichMedicineIntent, 1000)
                }
                else {
                    if(medicineNotFoundList.size != 0) {
                        val notFoundData = Gson().toJson(medicineNotFoundList)

                        val errorIntent =
                            Intent(this@CsvToXmlActivity, ErrorActivity::class.java).apply {
                                putExtra(EXT_MEDICINE_NOT_FOUND, notFoundData)
                                putExtra(EXT_ERROR_TYPE, 1)
                            }
                        startActivity(errorIntent)
                        finish()
                    }
                    else {
                        val xmlFile = createXmlFile(path)

                        withContext(Dispatchers.Main) {
                            val xmlFileCreatedTextView = findViewById<TextView>(R.id.xmlFileCreatedTextView)
                            xmlFileCreatedTextView.text = "File created at:\n${xmlFile.path}"

                            csvToXmlFinishButton.isEnabled = true
                        }
                    }
                }
            }
            catch(e: java.lang.Exception) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                val dateTime = LocalDateTime.now().format(formatter).toString()

                val logFileName = "Log_${dateTime}.txt"

                val logFilePath = File(path, logFileName)
                val stringWriter = StringWriter()
                e.printStackTrace()
                e.printStackTrace(PrintWriter(stringWriter))

                var exceptionAsString = stringWriter.toString()
                exceptionAsString += "\n\n"
                exceptionAsString += "Last Invoice: ${lastInvoice}\n"
                exceptionAsString += "Last Medicine: ${lastMedicine}\n"

                logFilePath.writeText(exceptionAsString)

                val errorIntent = Intent(this@CsvToXmlActivity, ErrorActivity::class.java).apply {
                    putExtra(EXT_LOG, logFileName)
                    putExtra(EXT_ERROR_TYPE, 0)
                }
                startActivity(errorIntent)

                finish()
            }

            csvToXmlFinishButton.setOnClickListener {
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if(requestCode == 1000 && resultCode == RESULT_OK){
            val path = applicationContext.externalMediaDirs.first()

            val typeToken = object : TypeToken<ArrayList<SearchedMedicine>>() {}.type
            val finalSelectMedicineList = Gson().fromJson<ArrayList<SearchedMedicine>>(
                resultData!!.getStringExtra(
                    EXT_MEDICINE_RESULT), typeToken)

            finalSelectMedicineList.forEach {
                for(i in invoicesList){
                    if(i.invoiceNo == it.invoiceNo){
                        val itemLine = ItemLine()

                        for(chosenIndex in 0 until it.searchResult.size){
                            if(it.searchResult[chosenIndex].medicineName == it.chosenMedicine[0]){
                                itemLine.barcode = it.searchResult[chosenIndex].barcode
                                itemLine.medicineName = it.searchResult[chosenIndex].medicineName
                                itemLine.quantity = it.qty
                                itemLine.itemUnit = it.searchResult[chosenIndex].itemUnit
                                itemLine.itemReserved1 = it.searchResult[chosenIndex].itemReserved1
                                itemLine.qtyControl = it.searchResult[chosenIndex].quantityControl

                                val unitPrice : Double = try{
                                    it.unitPriceString.toDouble()
                                }
                                catch(e_ : Exception){
                                    it.searchResult[chosenIndex].unitPrice
                                }

                                itemLine.unitPrice = unitPrice
                                itemLine.brutoUnitPrice = unitPrice

                                i.itemLines.add(itemLine)

                                break
                            }
                        }

                        break
                    }
                }
            }

            if(medicineNotFoundList.size != 0){
                val notFoundData = Gson().toJson(medicineNotFoundList)

                val errorIntent = Intent(this@CsvToXmlActivity, ErrorActivity::class.java).apply{
                    putExtra(EXT_MEDICINE_NOT_FOUND, notFoundData)
                    putExtra(EXT_ERROR_TYPE, 1)
                }
                startActivity(errorIntent)
                finish()
            }

            CoroutineScope(Dispatchers.IO).launch {
                val xmlFile = createXmlFile(path)

                withContext(Dispatchers.Main){
                    val xmlFileCreatedTextView = findViewById<TextView>(R.id.xmlFileCreatedTextView)
                    xmlFileCreatedTextView.text = "File created at:\n${xmlFile.path}"

                    csvToXmlFinishButton.isEnabled = true
                }
            }
        }
    }

    private fun readCsv(inputStream: InputStream, databaseName : String, databaseVersion : Int, listView : ListView) : ArrayList<Invoice> {
        val reader = inputStream.bufferedReader()
        val bufferedReader = BufferedReader(reader)
        val csvParser = CSVParser(
            bufferedReader,
            CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim()
        )

        val tempInvoicesList = ArrayList<Invoice>()
        val tempInvoiceNoList = ArrayList<Long>()

        val db = DatabaseHelper(applicationContext, null, databaseName, databaseVersion)

        var invoice = Invoice()
        var invoiceCount = 0
        var invoiceNo : Long = 0

        for(csvRecord in csvParser) {
            if(csvRecord.get("INVOICE_NO") == "NEW"){
                invoiceNo = 0

                if(invoiceCount != 0) {
                    tempInvoicesList.add(invoice)
                }
                continue
            }
            else if(csvRecord.get("INVOICE_NO") == "DONE"){
                tempInvoicesList.add(invoice)
                break
            }
            else if(csvRecord.get("INVOICE_NO").isNotEmpty()){
                invoiceNo = csvRecord.get("INVOICE_NO").toLong()
//                println("Invoice No: $invoiceNo")

                val invoiceDate = csvRecord.get("INVOICE_DATE")
                val description = csvRecord.get("DESCRIPTION")

                val itemLine = readItemLine(csvRecord, invoiceNo, databaseName, databaseVersion)

                invoice = db.readInvoiceDatabase(invoiceNo)

                invoice.invoiceNo = invoiceNo
                invoice.invoiceDate = invoiceDate
                invoice.description = description

                if(searchResult.size == 1) {
                    invoice.itemLines.add(itemLine)
                }

                invoiceCount++

                tempInvoiceNoList.add(0, invoiceNo)

                CoroutineScope(Dispatchers.Main).launch {
                    val convertListViewAdapter = ConvertListViewAdapter(applicationContext, tempInvoiceNoList)
                    listView.adapter = convertListViewAdapter
                }
            }
            else if(csvRecord.get("INVOICE_NO").isEmpty()){
                val itemLine = readItemLine(csvRecord, invoiceNo, databaseName, databaseVersion)

                if(searchResult.size == 1) {
                    invoice.itemLines.add(itemLine)
                }
            }
        }

        return tempInvoicesList
    }

    private fun readItemLine(csvRecord : CSVRecord, invoiceNo : Long, databaseName: String, databaseVersion: Int) : ItemLine{
//        println(invoiceNo)
        lastInvoice = invoiceNo

        val medicineDatabaseFileName = intent.getStringExtra(SelectCsvAndDatabaseActivity.EXT_DATABASE_FILE_NAME)

        val path = applicationContext.externalMediaDirs.first()
        val medicineDatabaseFile = File(path, medicineDatabaseFileName!!).path

        val barcode = csvRecord.get("BARCODE")

//        println("BARCODE: $barcode")

        val qty = csvRecord.get("QUANTITY").toFloat()
        val itemUnit = csvRecord.get("ITEM_UNIT")
        val medicineName = csvRecord.get("ITEM_NAME").toUpperCase()
//        val unitPriceString = csvRecord.get("UNIT_PRICE")

//        println("MEDICINE: $medicineName")
        lastMedicine = medicineName

        var selectedMedicineName = ""
        var selectedBarcode = ""
        var selectedItemUnit : String? = null
        var selectedQtyControl = 0
        var selectedItemReserved1 : String? = null
        var selectedPrice = 0.0
        var isFound = true

        if(barcode.startsWith("R-")){
            selectedMedicineName = medicineName
            selectedBarcode = barcode
            selectedItemUnit = itemUnit
            selectedQtyControl = 0
            selectedItemReserved1 = null
            selectedPrice = 0.0
        }
        else{
            // Find medicine in the database
            val medicineDb = MedicineDatabaseHelper(applicationContext, null, medicineDatabaseFile)
            searchResult = medicineDb.readMedicineDatabase(medicineName, applicationContext, invoiceNo)

            isFound = true

            if(searchResult.size == 1){
                selectedBarcode = searchResult[selection].barcode

                if(selectedBarcode == "Not Found"){
                    isFound = false
                }

                selectedMedicineName = searchResult[selection].medicineName
                selectedItemUnit = searchResult[selection].itemUnit
                selectedQtyControl = searchResult[selection].quantityControl
                selectedItemReserved1 = searchResult[selection].itemReserved1
                selectedPrice = searchResult[selection].unitPrice
            }
            else if(searchResult.size > 1){
                val searchedMedicine = SearchedMedicine()
                searchedMedicine.searchResult = searchResult
                searchedMedicine.invoiceNo = invoiceNo
                searchedMedicine.inputtedMedicine = medicineName
                searchedMedicine.qty = qty

                selectMedicineList.add(searchedMedicine)
            }

            medicineDb.close()
        }

        val itemLine = ItemLine()

        if(isFound) {
            itemLine.barcode = selectedBarcode
            itemLine.medicineName = selectedMedicineName
            itemLine.quantity = qty
            itemLine.itemUnit = selectedItemUnit
            itemLine.itemReserved1 = selectedItemReserved1
            itemLine.qtyControl = selectedQtyControl
            itemLine.unitPrice = selectedPrice
            itemLine.brutoUnitPrice = selectedPrice
        }
        else{
            itemLine.barcode = selectedBarcode
            itemLine.medicineName = selectedMedicineName

            return itemLine
        }

        val invoiceDb = DatabaseHelper(applicationContext, null, databaseName, databaseVersion)
        val finalItemLine = invoiceDb.readItemLine(itemLine, invoiceNo)

        invoiceDb.close()

        return finalItemLine
    }

    private fun replaceSpecialChars(vrb : String?) : String?{
        if(vrb != null) {
            var str = vrb
            val re = Regex("[&<>\"\']")

            if(vrb.contains(re)) {
                str = str.replace("&", "&amp;")
                str = str.replace("<", "&lt;")
                str = str.replace(">", "&gt;")
                str = str.replace("\"", "&quot;")
                str = str.replace("\'", "&apos;")

                return str
            }
            else {
                return vrb
            }
        }

        return vrb
    }

    // TODO: Change the destination folder for saving the xml file
    // TODO: Create new folder special for saving XML file
    // TODO: change the file name to IMPORTED_DATE-OF-INVOICE
    private fun createXmlFile(path: File) : File{
        val xmlSerializer = Xml.newSerializer()
        val invoiceDate = invoicesList[0].invoiceDate
        val dir = File(path, "IMPORTED")

        if(!dir.exists()) {
            dir.mkdirs()
        }

        val xmlFile = File(dir, "IMPORTED_${invoiceDate}.xml")

        val xmlFileOutputStream = xmlFile.outputStream()

        xmlSerializer.setOutput(xmlFileOutputStream, "UTF-8")
        xmlSerializer.startDocument(null, true)
        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)

        xmlSerializer.startTag(null, "NMEXML")
        xmlSerializer.attribute(null, "EximID", "619")
        xmlSerializer.attribute(null, "BranchCode", "00001")
        xmlSerializer.attribute(null, "ACCOUNTANTCOPYID", "")

        xmlSerializer.startTag(null, "TRANSACTIONS")
        xmlSerializer.attribute(null, "OnError", "CONTINUE")

        invoicesList.forEach {
//            println("INVOICENO = ${it.invoiceNo}")

            xmlSerializer.startTag(null, "SALESINVOICE")
            xmlSerializer.attribute(null, "operation", "Add")
            xmlSerializer.attribute(null, "REQUESTID", "1")
            
            xmlTagText("TRANSACTIONID", it.transactionId.toString(), xmlSerializer)

            var itemCount = 1

            for(item in it.itemLines){
//                println("MEDICINES = ${item.medicineName}")
                if(item.barcode == "Not Found"){
                    val notFoundMedicine = Medicine()

                    notFoundMedicine.medicineName = item.medicineName
                    notFoundMedicine.barcode = item.barcode
                    notFoundMedicine.invoiceNo = it.invoiceNo

                    medicineNotFoundList.add(notFoundMedicine)

                    continue
                }

                val re = Regex(".")

                if(item.medicineName.contains(re)){
                    item.medicineName = item.medicineName.replace(".", ",")
                }

                if(item.itemReserved1?.contains(re) == true){
                    item.itemReserved1 = item.itemReserved1?.replace(".", ",")
                }

                item.medicineName = replaceSpecialChars(item.medicineName)!!
                item.itemReserved1 = replaceSpecialChars(item.itemReserved1)

                it.invoiceAmount += (item.unitPrice * item.quantity)

                xmlSerializer.startTag(null, "ITEMLINE")
                xmlSerializer.attribute(null, "operation", "Add")

                xmlTagText("KeyID", itemCount.toString(), xmlSerializer)
                xmlTagText("ITEMNO", item.barcode, xmlSerializer)
                xmlTagText("QUANTITY", item.quantity.toString(), xmlSerializer)
                xmlTagText("ITEMUNIT", item.itemUnit, xmlSerializer)
                xmlTagText("UNITRATIO", item.unitRatio.toString(), xmlSerializer)
                xmlTagText("ITEMRESERVED1", item.itemReserved1, xmlSerializer)
                xmlTagText("ITEMRESERVED2", item.itemReserved2, xmlSerializer)
                xmlTagText("ITEMRESERVED3", item.itemReserved3, xmlSerializer)
                xmlTagText("ITEMRESERVED4", item.itemReserved4, xmlSerializer)
                xmlTagText("ITEMRESERVED5", item.itemReserved5, xmlSerializer)
                xmlTagText("ITEMRESERVED6", item.itemReserved6, xmlSerializer)
                xmlTagText("ITEMRESERVED7", item.itemReserved7, xmlSerializer)
                xmlTagText("ITEMRESERVED8", item.itemReserved8, xmlSerializer)
                xmlTagText("ITEMRESERVED9", item.itemReserved9, xmlSerializer)
                xmlTagText("ITEMRESERVED10", item.itemReserved10, xmlSerializer)
                xmlTagText("ITEMOVDESC", item.medicineName, xmlSerializer)
                xmlTagText("UNITPRICE", item.unitPrice.roundToLong().toString(), xmlSerializer)

                if(item.itemDiscPc != null){
                    xmlTagText("ITEMDISCPC", item.itemDiscPc?.roundToInt().toString(), xmlSerializer)
                }
                else{
                    xmlTagText("ITEMDISCPC", null, xmlSerializer)
                }

                xmlTagText("TAXCODES", item.taxCodes, xmlSerializer)
                xmlTagText("GROUPSEQ", item.groupSeq, xmlSerializer)
                xmlTagText("SOSEQ", item.soSeq, xmlSerializer)
                xmlTagText("BRUTOUNITPRICE", item.brutoUnitPrice.roundToLong().toString(), xmlSerializer)
                xmlTagText("WAREHOUSEID", item.warehouse, xmlSerializer)
                xmlTagText("QTYCONTROL", item.qtyControl.toString(), xmlSerializer)
                xmlTagText("DOSEQ", item.doseQ, xmlSerializer)
                xmlTagText("DOID", item.doId, xmlSerializer)
                
                xmlSerializer.endTag(null, "ITEMLINE")

                itemCount++
            }

            xmlTagText("INVOICENO", it.invoiceNo.toString(), xmlSerializer)
            xmlTagText("INVOICEDATE", it.invoiceDate, xmlSerializer)
            xmlTagText("TAX1ID", it.tax1Id, xmlSerializer)
            xmlTagText("TAX1CODE", it.tax1Code, xmlSerializer)
            xmlTagText("TAX2CODE", it.tax2Code, xmlSerializer)
            xmlTagText("TAX1RATE", it.tax1Rate.toString(), xmlSerializer)
            xmlTagText("TAX2RATE", it.tax2Rate.toString(), xmlSerializer)
            xmlTagText("RATE", it.rate.toString(), xmlSerializer)
            xmlTagText("INCLUSIVETAX", it.inclusiveTax.toString(), xmlSerializer)
            xmlTagText("CUSTOMERISTAXABLE", it.customerIsTaxable.toString(), xmlSerializer)
            xmlTagText("CASHDISCOUNT", it.cashDiscount.roundToInt().toString(), xmlSerializer)

            if(it.cashDiscPc != null) {
                xmlTagText("CASHDISCPC", it.cashDiscPc?.roundToInt().toString(), xmlSerializer)
            }
            else{
                xmlTagText("CASHDISCPC", null, xmlSerializer)
            }

            xmlTagText("INVOICEAMOUNT", it.invoiceAmount.roundToLong().toString(), xmlSerializer)
            xmlTagText("FREIGHT", it.freight.toString(), xmlSerializer)
            xmlTagText("TERMSID", it.terms, xmlSerializer)
            xmlTagText("FOB", it.fob, xmlSerializer)

            if(it.purchaseOrderNo != null) {
                xmlTagText("PURCHASEORDERNO", it.purchaseOrderNo.toString(), xmlSerializer)
            }
            else{
                xmlTagText("PURCHASEORDERNO", null, xmlSerializer)
            }

            xmlTagText("WAREHOUSEID", it.warehouse, xmlSerializer)

            val re = Regex(".")

            if(it.description.contains(re)){
                it.description = it.description.replace(".", ",")
            }

            xmlTagText("DESCRIPTION", it.description, xmlSerializer)
            xmlTagText("SHIPDATE", it.shipDate, xmlSerializer)
            xmlTagText("DELIVERYORDER", it.deliveryOrder, xmlSerializer)

            if(it.fiscalRate != null) {
                xmlTagText("FISCALRATE", it.fiscalRate.toString(), xmlSerializer)
            }
            else{
                xmlTagText("FISCALRATE", null, xmlSerializer)
            }

            xmlTagText("TAXDATE", it.taxDate, xmlSerializer)
            xmlTagText("CUSTOMERID", it.customer, xmlSerializer)
            
            xmlSerializer.startTag(null, "SALESMANID")
            xmlTagText("LASTNAME", it.salesman.getLastName(), xmlSerializer)
            xmlTagText("FIRSTNAME", it.salesman.getFirstName(), xmlSerializer)
            xmlSerializer.endTag(null, "SALESMANID")
            
            xmlTagText("PRINTED", it.printed.toString(), xmlSerializer)
            xmlTagText("SHIPTO1", it.shipTo1, xmlSerializer)
            xmlTagText("SHIPTO2", it.shipTo2, xmlSerializer)
            xmlTagText("SHIPTO3", it.shipTo3, xmlSerializer)
            xmlTagText("SHIPTO4", it.shipTo4, xmlSerializer)
            xmlTagText("SHIPTO5", it.shipTo5, xmlSerializer)

            val decimal = it.arAccount.toString().split('.')[1]

            if(decimal != "0") {
                xmlTagText("ARACCOUNT", it.arAccount.toString(), xmlSerializer)
            }
            else{
                xmlTagText("ARACCOUNT", it.arAccount.roundToLong().toString(), xmlSerializer)
            }

            if(it.taxFormNumber != null) {
                xmlTagText("TAXFORMNUMBER", it.taxFormNumber.toString(), xmlSerializer)
            }
            else{
                xmlTagText("TAXFORMNUMBER", null, xmlSerializer)
            }

            xmlTagText("TAXFORMCODE", it.taxFormCode, xmlSerializer)
            xmlTagText("CURRENCYNAME", it.currencyName, xmlSerializer)
            xmlTagText("AUTOMATICINSERTGROUPING", it.automaticInsertGrouping, xmlSerializer)

            xmlSerializer.endTag(null, "SALESINVOICE")
        }

        xmlSerializer.endTag(null, "TRANSACTIONS")
        xmlSerializer.endTag(null, "NMEXML")
        xmlSerializer.endDocument()
        xmlSerializer.flush()

        xmlFileOutputStream.close()

        return xmlFile
    }
    
    private fun xmlTagText(tag: String, text: String?, xmlSerializer: XmlSerializer){
        xmlSerializer.startTag(null, tag)

        if(text != null) {
            xmlSerializer.text(text)
        }

        xmlSerializer.endTag(null, tag)
    }
}