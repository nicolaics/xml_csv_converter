package xml_csv.xmlcsv

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import kotlin.math.roundToLong

class DatabaseHelper (context: Context, factory: SQLiteDatabase.CursorFactory?, databaseName : String, version : Int) :
    SQLiteOpenHelper(context, databaseName, factory, version){
    companion object{
        const val invoiceTable = "Invoice"
        const val termsTable = "Terms"
        const val warehouseTable = "Warehouse"
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
                "tax_1_id VARCHAR(8), " +
                "tax_1_code VARCHAR(8), " +
                "tax_2_code VARCHAR(8), " +
                "tax_1_rate INTEGER, " +
                "tax_2_rate INTEGER, " +
                "rate INTEGER, " +
                "inclusive_tax INTEGER, " +
                "customer_is_taxable INTEGER, " +
                "cash_discount REAL, " +
                "cash_disc_pc REAL, " +
                "invoice_amount REAL, " +
                "freight INTEGER, " +
                "terms_id INTEGER, " +
                "fob TEXT, " +
                "purchase_order_no INTEGER, " +
                "warehouse_id INTEGER, " +
                "invoice_description TEXT, " +
                "ship_date TEXT, " +
                "delivery_order TEXT, " +
                "fiscal_rate INTEGER, " +
                "tax_date TEXT, " +
                "customer_id INTEGER, " +
                "salesman_id INTEGER, " +
                "printed INTEGER, " +
                "ship_to_1_id INTEGER, " +
                "ship_to_2_id INTEGER, " +
                "ship_to_3_id INTEGER, " +
                "ship_to_4_id INTEGER, " +
                "ship_to_5_id INTEGER, " +
                "ar_account REAL, " +
                "tax_form_number INTEGER, " +
                "tax_form_code VARCHAR(8), " +
                "currency_name VARCHAR(8), " +
                "automatic_insert_grouping TEXT)")
        db?.execSQL(command)

        command = ("CREATE TABLE " + termsTable + " " +
                   "(id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                   "terms TEXT UNIQUE)")
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

        command = ("CREATE TABLE " + warehouseTable + " " +
                   "(id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                   "warehouse TEXT UNIQUE)")
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
                   "item_reserved_2 TEXT, " +
                   "item_reserved_3 TEXT, " +
                   "item_reserved_4 TEXT, " +
                   "item_reserved_5 TEXT, " +
                   "item_reserved_6 TEXT, " +
                   "item_reserved_7 TEXT, " +
                   "item_reserved_8 TEXT, " +
                   "item_reserved_9 TEXT, " +
                   "item_reserved_10 TEXT, " +
                   "item_disc_pc REAL, " +
                   "tax_codes VARCHAR(8), " +
                   "group_seq TEXT, " +
                   "so_seq TEXT, " +
                   "quantity_control INTEGER, " +
                   "dose_q TEXT, " +
                   "do_id TEXT)")
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

        command = "DROP TABLE IF EXISTS Terms"
        db?.execSQL(command)

        command = "DROP TABLE IF EXISTS Warehouse"
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

    fun addData(invoices : ArrayList<Invoice>){
        val dbWrite = this.writableDatabase
        val dbRead = this.readableDatabase

        var invoiceId = 1

        invoices.forEach {
            println(it.invoiceNo)
            val termsValues = ContentValues()
            termsValues.put("terms", it.terms)
            val termsId = insertPartialDataAndGetId(termsValues, termsTable, "terms", arrayOf(it.terms))

            val warehouseInvoiceValues = ContentValues()
            warehouseInvoiceValues.put("warehouse", it.warehouse)
            val warehouseInvoiceId = insertPartialDataAndGetId(warehouseInvoiceValues, warehouseTable, "warehouse", arrayOf(it.warehouse))

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

            val shipTo2Id : Int
            val shipTo3Id : Int
            val shipTo4Id : Int
            val shipTo5Id : Int

            if(it.shipTo2 != null) {
                shipToValues = ContentValues()
                shipToValues.put("customer", it.shipTo2)
                shipTo2Id = insertPartialDataAndGetId(
                    shipToValues,
                    customerTable, "customer",
                    arrayOf(it.shipTo2)
                )
            }
            else{
                shipTo2Id = 0
            }

            if(it.shipTo3 != null) {
                shipToValues = ContentValues()
                shipToValues.put("customer", it.shipTo3)
                shipTo3Id = insertPartialDataAndGetId(
                    shipToValues,
                    customerTable, "customer",
                    arrayOf(it.shipTo3)
                )
            }
            else{
                shipTo3Id = 0
            }

            if(it.shipTo4 != null) {
                shipToValues = ContentValues()
                shipToValues.put("customer", it.shipTo4)
                shipTo4Id = insertPartialDataAndGetId(
                    shipToValues,
                    customerTable, "customer",
                    arrayOf(it.shipTo4)
                )
            }
            else{
                shipTo4Id = 0
            }

            if(it.shipTo5 != null) {
                shipToValues = ContentValues()
                shipToValues.put("customer", it.shipTo5)
                shipTo5Id = insertPartialDataAndGetId(
                    shipToValues,
                    customerTable, "customer",
                    arrayOf(it.shipTo5)
                )
            }
            else{
                shipTo5Id = 0
            }

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

                val warehouseMedicineValues = ContentValues()
                warehouseMedicineValues.put("warehouse", iter.warehouse)
                val warehouseMedicineId = insertPartialDataAndGetId(warehouseMedicineValues, warehouseTable,
                    "warehouse", arrayOf(iter.warehouse))

                medicineValues.put("warehouse_id", warehouseMedicineId)

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
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_reserved_2")), iter.itemReserved2)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_reserved_3")), iter.itemReserved3)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_reserved_4")), iter.itemReserved4)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_reserved_5")), iter.itemReserved5)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_reserved_6")), iter.itemReserved6)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_reserved_7")), iter.itemReserved7)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_reserved_8")), iter.itemReserved8)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_reserved_9")), iter.itemReserved9)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_reserved_10")), iter.itemReserved10)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("item_disc_pc")), iter.itemDiscPc)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("tax_codes")), iter.taxCodes)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("group_seq")), iter.groupSeq)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("so_seq")), iter.soSeq)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("quantity_control")), iter.qtyControl)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("dose_q")), iter.doseQ)){
                        insertEachItem(medicineId, iter)
                        continue
                    }
                    if(!compare(resultCursor.getStringOrNull(resultCursor.getColumnIndex("do_id")), iter.doId)){
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
            invoiceValues.put("tax_1_id", it.tax1Id)
            invoiceValues.put("tax_1_code", it.tax1Code)
            invoiceValues.put("tax_2_code", it.tax2Code)
            invoiceValues.put("tax_1_rate", it.tax1Rate)
            invoiceValues.put("tax_2_rate", it.tax2Rate)
            invoiceValues.put("rate", it.rate)
            invoiceValues.put("inclusive_tax", it.inclusiveTax)
            invoiceValues.put("customer_is_taxable", it.customerIsTaxable)
            invoiceValues.put("cash_discount", it.cashDiscount)
            invoiceValues.put("cash_disc_pc", it.cashDiscPc)
            invoiceValues.put("invoice_amount", it.invoiceAmount.roundToLong())
            invoiceValues.put("freight", it.freight)
            invoiceValues.put("terms_id", termsId)
            invoiceValues.put("fob", it.fob)
            invoiceValues.put("purchase_order_no", it.purchaseOrderNo)
            invoiceValues.put("warehouse_id", warehouseInvoiceId)
            invoiceValues.put("invoice_description", it.description)
            invoiceValues.put("ship_date", it.shipDate)
            invoiceValues.put("delivery_order", it.deliveryOrder)
            invoiceValues.put("fiscal_rate", it.fiscalRate)
            invoiceValues.put("tax_date", it.taxDate)
            invoiceValues.put("customer_id", customerId)
            invoiceValues.put("salesman_id", salesmanId)
            invoiceValues.put("printed", it.printed)
            invoiceValues.put("ship_to_1_id", shipTo1Id)
            invoiceValues.put("ship_to_2_id", shipTo2Id)
            invoiceValues.put("ship_to_3_id", shipTo3Id)
            invoiceValues.put("ship_to_4_id", shipTo4Id)
            invoiceValues.put("ship_to_5_id", shipTo5Id)
            invoiceValues.put("ar_account", it.arAccount)
            invoiceValues.put("tax_form_number", it.taxFormNumber)
            invoiceValues.put("tax_form_code", it.taxFormCode)
            invoiceValues.put("currency_name", it.currencyName)
            invoiceValues.put("automatic_insert_grouping", it.automaticInsertGrouping)

            dbWrite.insert("Invoice", null, invoiceValues)
            invoiceId++
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
        eachItemValues.put("item_reserved_2", iter.itemReserved2)
        eachItemValues.put("item_reserved_3", iter.itemReserved3)
        eachItemValues.put("item_reserved_4", iter.itemReserved4)
        eachItemValues.put("item_reserved_5", iter.itemReserved5)
        eachItemValues.put("item_reserved_6", iter.itemReserved6)
        eachItemValues.put("item_reserved_7", iter.itemReserved7)
        eachItemValues.put("item_reserved_8", iter.itemReserved8)
        eachItemValues.put("item_reserved_9", iter.itemReserved9)
        eachItemValues.put("item_reserved_10", iter.itemReserved10)
        eachItemValues.put("item_disc_pc", iter.itemDiscPc)
        eachItemValues.put("tax_codes", iter.taxCodes)
        eachItemValues.put("group_seq", iter.groupSeq)
        eachItemValues.put("so_seq", iter.soSeq)
        eachItemValues.put("quantity_control", iter.qtyControl)
        eachItemValues.put("dose_q", iter.doseQ)
        eachItemValues.put("do_id", iter.doId)

        dbWrite.insert(eachItemTable, null, eachItemValues)
    }

    @SuppressLint("Range")
    fun readInvoiceDatabase(invoiceNo : Long) : Invoice{
        val dbInvoice = Invoice()

        val dbRead = this.readableDatabase

        val invoiceCursor = dbRead.rawQuery("SELECT * FROM Invoice WHERE invoice_no = ?", arrayOf(invoiceNo.toString()))
        val additionalCursor = dbRead.rawQuery("SELECT Terms.terms, Warehouse.warehouse, " +
                                                    "Salesman.last_name, Salesman.first_name, " +
                                                    "Customer.customer FROM Invoice JOIN Terms JOIN Warehouse " +
                                                    "JOIN Salesman JOIN Customer " +
                                                    "on Invoice.terms_id = terms.id " +
                                                    "AND Invoice.warehouse_id = Warehouse.id " +
                                                    "AND Invoice.salesman_id = Salesman.id " +
                                                    "AND Invoice.customer_id = Customer.id " +
                                                    "WHERE invoice_no = ?", arrayOf(invoiceNo.toString())
        )

        invoiceCursor.moveToFirst()
        additionalCursor.moveToFirst()

        dbInvoice.transactionId = invoiceCursor.getLong(invoiceCursor.getColumnIndex("transaction_id"))
        dbInvoice.tax1Id = invoiceCursor.getString(invoiceCursor.getColumnIndex("tax_1_id"))
        dbInvoice.tax1Code = invoiceCursor.getString(invoiceCursor.getColumnIndex("tax_1_code"))
        dbInvoice.tax2Code = invoiceCursor.getStringOrNull(invoiceCursor.getColumnIndex("tax_2_code"))
        dbInvoice.tax1Rate = invoiceCursor.getInt(invoiceCursor.getColumnIndex("tax_1_rate"))
        dbInvoice.tax2Rate = invoiceCursor.getInt(invoiceCursor.getColumnIndex("tax_2_rate"))
        dbInvoice.rate = invoiceCursor.getInt(invoiceCursor.getColumnIndex("rate"))
        dbInvoice.inclusiveTax = invoiceCursor.getInt(invoiceCursor.getColumnIndex("inclusive_tax"))
        dbInvoice.customerIsTaxable = invoiceCursor.getInt(invoiceCursor.getColumnIndex("customer_is_taxable"))
        dbInvoice.cashDiscount = invoiceCursor.getDouble(invoiceCursor.getColumnIndex("cash_discount"))
        dbInvoice.cashDiscPc = invoiceCursor.getDoubleOrNull(invoiceCursor.getColumnIndex("cash_disc_pc"))
        dbInvoice.freight = invoiceCursor.getInt(invoiceCursor.getColumnIndex("freight"))
        dbInvoice.terms = additionalCursor.getString(additionalCursor.getColumnIndex("terms"))
        dbInvoice.fob = invoiceCursor.getStringOrNull(invoiceCursor.getColumnIndex("fob"))
        dbInvoice.purchaseOrderNo = invoiceCursor.getIntOrNull(invoiceCursor.getColumnIndex("purchase_order_no"))
        dbInvoice.warehouse = additionalCursor.getString(additionalCursor.getColumnIndex("warehouse"))
        dbInvoice.shipDate = invoiceCursor.getString(invoiceCursor.getColumnIndex("ship_date"))
        dbInvoice.deliveryOrder = invoiceCursor.getStringOrNull(invoiceCursor.getColumnIndex("delivery_order"))
        dbInvoice.fiscalRate = invoiceCursor.getIntOrNull(invoiceCursor.getColumnIndex("fiscal_rate"))
        dbInvoice.taxDate = invoiceCursor.getString(invoiceCursor.getColumnIndex("tax_date"))
        dbInvoice.customer = additionalCursor.getString(additionalCursor.getColumnIndex("customer"))
        dbInvoice.salesman.setLastName(additionalCursor.getStringOrNull(additionalCursor.getColumnIndex("last_name")))
        dbInvoice.salesman.setFirstName(additionalCursor.getString(additionalCursor.getColumnIndex("first_name")))
        dbInvoice.printed = invoiceCursor.getInt(invoiceCursor.getColumnIndex("printed"))

        val shipTo = ArrayList<Int?>()
        shipTo.add(invoiceCursor.getIntOrNull(invoiceCursor.getColumnIndex("ship_to_1_id")))
        shipTo.add(invoiceCursor.getIntOrNull(invoiceCursor.getColumnIndex("ship_to_2_id")))
        shipTo.add(invoiceCursor.getIntOrNull(invoiceCursor.getColumnIndex("ship_to_3_id")))
        shipTo.add(invoiceCursor.getIntOrNull(invoiceCursor.getColumnIndex("ship_to_4_id")))
        shipTo.add(invoiceCursor.getIntOrNull(invoiceCursor.getColumnIndex("ship_to_5_id")))

        dbInvoice.arAccount = invoiceCursor.getDouble(invoiceCursor.getColumnIndex("ar_account"))
        dbInvoice.taxFormNumber = invoiceCursor.getIntOrNull(invoiceCursor.getColumnIndex("tax_form_number"))
        dbInvoice.taxFormCode = invoiceCursor.getStringOrNull(invoiceCursor.getColumnIndex("tax_form_code"))
        dbInvoice.currencyName = invoiceCursor.getString(invoiceCursor.getColumnIndex("currency_name"))
        dbInvoice.automaticInsertGrouping = invoiceCursor.getStringOrNull(invoiceCursor.getColumnIndex("automatic_insert_grouping"))

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
                "Medicines.bruto_unit_price, Medicines.item_unit, Each_Item.*, " +
                "Warehouse.warehouse FROM Each_Item JOIN Medicines JOIN Invoice " +
                "JOIN Warehouse JOIN Buf on Buf.each_item_id = Each_Item.id " +
                "AND Buf.invoice_id = Invoice.id AND Each_Item.medicine_id = Medicines.id " +
                "AND Warehouse.id = Medicines.warehouse_id " +
                "WHERE Invoice.invoice_no = ? AND Medicines.barcode = ?",
            arrayOf(invoiceNo.toString(), barcode))

        cursor.moveToFirst()

        if(cursor.count != 0) {
            finalItemLine.itemUnit = cursor.getString(cursor.getColumnIndex("item_unit"))
            finalItemLine.unitRatio = cursor.getInt(cursor.getColumnIndex("unit_ratio"))
            finalItemLine.itemReserved2 = cursor.getStringOrNull(cursor.getColumnIndex("item_reserved_2"))
            finalItemLine.itemReserved3 = cursor.getStringOrNull(cursor.getColumnIndex("item_reserved_3"))
            finalItemLine.itemReserved4 = cursor.getStringOrNull(cursor.getColumnIndex("item_reserved_4"))
            finalItemLine.itemReserved5 = cursor.getStringOrNull(cursor.getColumnIndex("item_reserved_5"))
            finalItemLine.itemReserved6 = cursor.getStringOrNull(cursor.getColumnIndex("item_reserved_6"))
            finalItemLine.itemReserved7 = cursor.getStringOrNull(cursor.getColumnIndex("item_reserved_7"))
            finalItemLine.itemReserved8 = cursor.getStringOrNull(cursor.getColumnIndex("item_reserved_8"))
            finalItemLine.itemReserved9 = cursor.getStringOrNull(cursor.getColumnIndex("item_reserved_9"))
            finalItemLine.itemReserved10 = cursor.getStringOrNull(cursor.getColumnIndex("item_reserved_10"))
            finalItemLine.itemDiscPc = cursor.getDoubleOrNull(cursor.getColumnIndex("item_disc_pc"))
            finalItemLine.taxCodes = cursor.getStringOrNull(cursor.getColumnIndex("tax_codes"))
            finalItemLine.groupSeq = cursor.getStringOrNull(cursor.getColumnIndex("group_seq"))
            finalItemLine.soSeq = cursor.getStringOrNull(cursor.getColumnIndex("so_seq"))
            finalItemLine.warehouse = cursor.getString(cursor.getColumnIndex("warehouse"))
            finalItemLine.doseQ = cursor.getStringOrNull(cursor.getColumnIndex("dose_q"))
            finalItemLine.doId = cursor.getStringOrNull(cursor.getColumnIndex("do_id"))
        }

        cursor.close()
        dbRead.close()

        return finalItemLine
    }
}
