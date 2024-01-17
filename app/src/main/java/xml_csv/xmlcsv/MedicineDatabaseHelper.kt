package xml_csv.xmlcsv

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class MedicineDatabaseHelper (context: Context, factory: SQLiteDatabase.CursorFactory?, databaseName : String) :
    SQLiteOpenHelper(context, databaseName, factory, 1){
    // create database
    override fun onCreate(db: SQLiteDatabase?) {
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
    }

    @SuppressLint("Range")
    fun readMedicineDatabase(medicineName: String, context: Context, invoiceNo: Long) : ArrayList<Medicine>{
        val dbRead = this.readableDatabase

        val medicineLists = ArrayList<Medicine>()

        val firstSearchCursor = dbRead.rawQuery("SELECT * FROM Obat WHERE medicine_name = ? COLLATE NOCASE", arrayOf(medicineName))

        firstSearchCursor.moveToFirst()

        // tidak ada nama yang sama persis
        if(firstSearchCursor.count == 0) {
            val medicineNameOnly = medicineName.split(' ')[0]

            val searchMedicineName = "%$medicineNameOnly%"

            val secondSearchCursor = dbRead.rawQuery("SELECT * FROM Obat WHERE medicine_name LIKE ? COLLATE NOCASE", arrayOf(searchMedicineName))

            secondSearchCursor.moveToFirst()

            if(secondSearchCursor.count > 0) {
                for(i in 0 until secondSearchCursor.count) {
                    val medicine = Medicine()
                    medicine.barcode = secondSearchCursor.getString(secondSearchCursor.getColumnIndex("barcode"))
                    medicine.medicineName = secondSearchCursor.getString(secondSearchCursor.getColumnIndex("medicine_name"))
                    medicine.quantityControl =
                        secondSearchCursor.getInt(secondSearchCursor.getColumnIndex("quantity_control"))
                    medicine.unitPrice = secondSearchCursor.getDouble(secondSearchCursor.getColumnIndex("unit_price"))
                    medicine.itemUnit = secondSearchCursor.getString(secondSearchCursor.getColumnIndex("item_unit"))
                    medicine.itemReserved1 =
                        secondSearchCursor.getString(secondSearchCursor.getColumnIndex("item_reserved_1"))

                    medicineLists.add(medicine)

                    secondSearchCursor.moveToNext()
                }
            }
            else{
                val medicine = Medicine()
                medicine.barcode = "Not Found"
                medicine.medicineName = medicineName
                medicine.invoiceNo = invoiceNo

                medicineLists.add(medicine)
//                Toast.makeText(context, "Medicine not found: ${medicineName}\nfor Invoice No: ${invoiceNo}!", Toast.LENGTH_LONG).show()
            }

            secondSearchCursor.close()
        }
        // ketemu obat yang namanya sama persis
        else if(firstSearchCursor.count == 1){
            val medicine = Medicine()
            medicine.barcode = firstSearchCursor.getString(firstSearchCursor.getColumnIndex("barcode"))
            medicine.medicineName = firstSearchCursor.getString(firstSearchCursor.getColumnIndex("medicine_name"))
            medicine.quantityControl =
                firstSearchCursor.getInt(firstSearchCursor.getColumnIndex("quantity_control"))
            medicine.unitPrice = firstSearchCursor.getDouble(firstSearchCursor.getColumnIndex("unit_price"))
            medicine.itemUnit = firstSearchCursor.getString(firstSearchCursor.getColumnIndex("item_unit"))
            medicine.itemReserved1 =
                firstSearchCursor.getString(firstSearchCursor.getColumnIndex("item_reserved_1"))

            medicineLists.add(medicine)
        }

        firstSearchCursor.close()
        dbRead.close()

        return medicineLists
    }
}
