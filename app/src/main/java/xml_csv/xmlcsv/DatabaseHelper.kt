package xml_csv.xmlcsv

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.ListView
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong

class DatabaseHelper (context: Context, factory: SQLiteDatabase.CursorFactory?, databaseName : String, version : Int) :
    SQLiteOpenHelper(context, databaseName, factory, version){
    companion object{
        const val invoiceTable = "Invoice"
        const val customerTable = "Customer"
        const val medicinesTable = "Medicines"
        const val salesmanTable = "Salesman"
        const val eachItemTable = "Each_Item"
        const val bufTable = "Buf"
    }
    // create database
    override fun onCreate(db: SQLiteDatabase?) {
        var command = ("CREATE TABLE " + invoiceTable + " " +
                "(id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                "transaction_id REAL UNIQUE, " +
                "invoice_no REAL UNIQUE, " +
                "invoice_date TEXT, " +
                "cash_discount REAL, " +
                "invoice_amount REAL, " +
                "invoice_description TEXT, " +
                "ship_date TEXT, " +
                "tax_date TEXT, " +
                "customer_id INTEGER, " +
                "salesman_id INTEGER, " +
                "printed INTEGER, " +
                "ship_to_1_id INTEGER, " +
                "ar_account REAL)")
        db?.execSQL(command)

        command = ("CREATE TABLE " + medicinesTable + " " +
                   "(id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                   "barcode REAL UNIQUE, " +
                   "item_unit VARCHAR(16), " +
                   "unit_ratio INTEGER, " +
                   "medicine_name TEXT, " +
                   "unit_price REAL, " +
                    "bruto_unit_price REAL, " +
                   "warehouse_id INTEGER)")
        db?.execSQL(command)

        command = ("CREATE TABLE " + salesmanTable + " " +
                   "(id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                   "last_name TEXT, " +
                   "first_name TEXT UNIQUE)")
        db?.execSQL(command)

        command = ("CREATE TABLE " + customerTable + " " +
                   "(id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                   "customer TEXT UNIQUE)")
        db?.execSQL(command)

        command = ("CREATE TABLE " + eachItemTable + " " +
                   "(id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                   "medicine_id INTEGER, " +
                   "quantity REAL, " +
                   "item_reserved_1 TEXT, " +
                   "item_disc_pc REAL)")
        db?.execSQL(command)

        command = ("CREATE TABLE " + bufTable + " " +
                   "(invoice_id INTEGER NOT NULL, " +
                   "each_item_id INTEGER NOT NULL)")
        db?.execSQL(command)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        var command = "DROP TABLE IF EXISTS Invoice"
        db?.execSQL(command)

        command = "DROP TABLE IF EXISTS Medicines"
        db?.execSQL(command)

        command = "DROP TABLE IF EXISTS Salesman"
        db?.execSQL(command)

        command = "DROP TABLE IF EXISTS Customer"
        db?.execSQL(command)

        command = "DROP TABLE IF EXISTS Each_Item"
        db?.execSQL(command)

        command = "DROP TABLE IF EXISTS Buf"
        db?.execSQL(command)

        onCreate(db)
    }

    fun addData(invoices : ArrayList<Invoice>, listView: ListView, context: Context){
        val dbWrite = this.writableDatabase
        val dbRead = this.readableDatabase

        var invoiceId = 1
        val tempInvoiceNoList = ArrayList<Long>()

        invoices.forEach {
            println(it.invoiceNo)

            val customerValues = ContentValues()
            customerValues.put("customer", it.customer)
            val customerId = insertPartialDataAndGetId(customerValues, customerTable, "customer", arrayOf(it.customer))

            val salesmanValues = ContentValues()
            salesmanValues.put("last_name", it.salesman.getLastName())
            salesmanValues.put("first_name", it.salesman.getFirstName())

            val salesmanId : Int
            if(it.salesman.getLastName() != null) {
                salesmanId = insertPartialDataAndGetId(salesmanValues, salesmanTable, "last_name = ? AND first_name",
                                    arrayOf(it.salesman.getLastName(), it.salesman.getFirstName()))
            }
            else{
                salesmanId = insertPartialDataAndGetId(salesmanValues, salesmanTable, "first_name", arrayOf(it.salesman.getFirstName()))
            }

            var shipToValues = ContentValues()
            shipToValues.put("customer",it.shipTo1)
            val shipTo1Id = insertPartialDataAndGetId(shipToValues, customerTable, "customer", arrayOf(it.shipTo1))

            for(iter in it.itemLines){
                val medicineValues = ContentValues()

                var barcode = iter.barcode
                var medicineName = iter.medicineName
                var unitPrice = iter.unitPrice
                var brutoUnitPrice = iter.brutoUnitPrice

                println("MEDICINES: $medicineName")

                if(iter.barcode.startsWith("R-")){
                    barcode = "R"
                    medicineName = "Resep"
                    unitPrice = 0.0
                    brutoUnitPrice = 0.0
                }

                medicineValues.put("barcode", barcode)
                medicineValues.put("item_unit", iter.itemUnit)
                medicineValues.put("unit_ratio", iter.unitRatio)
                medicineValues.put("medicine_name", medicineName)
                medicineValues.put("unit_price", unitPrice)
                medicineValues.put("bruto_unit_price", brutoUnitPrice)

                val medicineId = insertPartialDataAndGetId(medicineValues, medicinesTable, "barcode", arrayOf(barcode))

                val resultCursor = dbRead.rawQuery("SELECT * FROM Each_Item WHERE medicine_id = ? AND quantity = ?",
                    arrayOf(medicineId.toString(), iter.quantity.toString()))

                resultCursor.moveToFirst()
                if(resultCursor.count == 0){
                    insertEachItem(medicineId, iter)
                }
                else{
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_reserved_1")), iter.itemReserved1)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_disc_pc")), iter.itemDiscPc)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                }

                val cursor = dbRead.rawQuery("SELECT id FROM Each_Item WHERE medicine_id = ? AND quantity = ?",
                                arrayOf(medicineId.toString(), iter.quantity.toString()))
                cursor.moveToFirst()
                val eachItemId = cursor.getInt(0)
                cursor.close()

                val bufValues = ContentValues()
                bufValues.put("each_item_id", eachItemId)
                bufValues.put("invoice_id", invoiceId)
                dbWrite.insert(bufTable, null, bufValues)

                resultCursor.close()
            }

            val invoiceValues = ContentValues()
            invoiceValues.put("id", invoiceId)
            invoiceValues.put("transaction_id", it.transactionId)
            invoiceValues.put("invoice_no", it.invoiceNo)
            invoiceValues.put("invoice_date", it.invoiceDate)
            invoiceValues.put("cash_discount", it.cashDiscount)
            invoiceValues.put("invoice_amount", it.invoiceAmount.roundToLong())
            invoiceValues.put("invoice_description", it.description)
            invoiceValues.put("ship_date", it.shipDate)
            invoiceValues.put("tax_date", it.taxDate)
            invoiceValues.put("customer_id", customerId)
            invoiceValues.put("salesman_id", salesmanId)
            invoiceValues.put("printed", it.printed)
            invoiceValues.put("ship_to_1_id", shipTo1Id)
            invoiceValues.put("ar_account", it.arAccount)

            dbWrite.insert("Invoice", null, invoiceValues)
            invoiceId++

            tempInvoiceNoList.add(it.invoiceNo)

            CoroutineScope(Dispatchers.Main).launch {
                val xmlToCsvListViewAdapter = XmlToCsvListViewAdapter(context, tempInvoiceNoList)
                listView.adapter = xmlToCsvListViewAdapter
            }
        }

        dbWrite.close()
        dbRead.close()
    }

    private fun insertPartialDataAndGetId(values : ContentValues, tableName: String, params : String, search : Array<String?>): Int{
        val dbWrite = this.writableDatabase
        val dbRead = this.readableDatabase

        dbWrite.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE)

        val cursor = dbRead.rawQuery("SELECT id FROM $tableName WHERE $params = ?", search)
        cursor.moveToFirst()

        val id = cursor.getInt(0)
        cursor.close()

        return id
    }

    private fun compare(item1 : Any?, item2 : Any?) : Boolean{
        if(item1 == item2){
            return false
        }
        else{
            return true
        }
    }

    private fun insertEachItem(medicineId : Int, iter: ItemLine){
        val dbWrite = this.writableDatabase

        val eachItemValues = ContentValues()

        eachItemValues.put("medicine_id", medicineId)
        eachItemValues.put("quantity", iter.quantity)
        eachItemValues.put("item_reserved_1", iter.itemReserved1)
        eachItemValues.put("item_disc_pc", iter.itemDiscPc)

        dbWrite.insert(eachItemTable, null, eachItemValues)
    }

    @SuppressLint("Range")
    fun readInvoiceDatabase(invoiceNo : Long) : Invoice{
        val dbInvoice = Invoice()

        val dbRead = this.readableDatabase

        val invoiceCursor = dbRead.rawQuery("SELECT * FROM Invoice WHERE invoice_no = ?", arrayOf(invoiceNo.toString()))
        val additionalCursor = dbRead.rawQuery("SELECT Salesman.last_name, Salesman.first_name, " +
                                                    "Customer.customer FROM Invoice " +
                                                    "JOIN Salesman JOIN Customer " +
                                                    "on Invoice.salesman_id = Salesman.id " +
                                                    "AND Invoice.customer_id = Customer.id " +
                                                    "AND Invoice.ship_to_1_id = Customer.id " +
                                                    "WHERE invoice_no = ?", arrayOf(invoiceNo.toString())
        )

        invoiceCursor.moveToFirst()
        additionalCursor.moveToFirst()

        dbInvoice.transactionId = invoiceCursor.getLong(invoiceCursor.getColumnIndex("transaction_id"))
        dbInvoice.cashDiscount = invoiceCursor.getDouble(invoiceCursor.getColumnIndex("cash_discount"))
        dbInvoice.shipDate = invoiceCursor.getString(invoiceCursor.getColumnIndex("ship_date"))
        dbInvoice.taxDate = invoiceCursor.getString(invoiceCursor.getColumnIndex("tax_date"))
        dbInvoice.customer = additionalCursor.getString(additionalCursor.getColumnIndex("customer"))
        dbInvoice.salesman.setLastName(additionalCursor.getStringOrNull(additionalCursor.getColumnIndex("last_name")))
        dbInvoice.salesman.setFirstName(additionalCursor.getString(additionalCursor.getColumnIndex("first_name")))
        dbInvoice.printed = invoiceCursor.getInt(invoiceCursor.getColumnIndex("printed"))

        dbInvoice.shipTo1 = dbInvoice.customer
        dbInvoice.arAccount = invoiceCursor.getDouble(invoiceCursor.getColumnIndex("ar_account"))

        invoiceCursor.close()
        additionalCursor.close()

        dbRead.close()

        return dbInvoice
    }

    @SuppressLint("Range")
    fun readItemLine(itemLine: ItemLine, invoiceNo : Long) : ItemLine{
        val finalItemLine = itemLine

        var barcode = itemLine.barcode

        if(itemLine.barcode.startsWith("R-")){
            barcode = "R"
        }
        else if(itemLine.barcode == "Not Found"){
            return itemLine
        }

        val dbRead = this.readableDatabase
        val cursor = dbRead.rawQuery("SELECT Medicines.unit_ratio, " +
                "Medicines.bruto_unit_price, Medicines.item_unit, Each_Item.* " +
                "FROM Each_Item JOIN Medicines JOIN Invoice " +
                "JOIN Buf on Buf.each_item_id = Each_Item.id " +
                "AND Buf.invoice_id = Invoice.id AND Each_Item.medicine_id = Medicines.id " +
                "WHERE Invoice.invoice_no = ? AND Medicines.barcode = ?",
            arrayOf(invoiceNo.toString(), barcode))

        cursor.moveToFirst()

        if(cursor.count != 0) {
            finalItemLine.itemUnit = cursor.getString(cursor.getColumnIndex("item_unit"))
            finalItemLine.unitRatio = cursor.getInt(cursor.getColumnIndex("unit_ratio"))
            finalItemLine.itemDiscPc = cursor.getDoubleOrNull(cursor.getColumnIndex("item_disc_pc"))
        }

        cursor.close()
        dbRead.close()

        return finalItemLine
    }
}
