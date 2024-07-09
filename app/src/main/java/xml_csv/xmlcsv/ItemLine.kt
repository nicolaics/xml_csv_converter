package xml_csv.xmlcsv

class ItemLine {
    var barcode : String = ""
    var quantity : Float = 0.0F
    var itemUnit : String? = null
    var unitRatio : Int = 1
    var itemReserved1 : String? = null
    var itemReserved2 : String = ""
    var itemReserved3 : String = ""
    var itemReserved4 : String = ""
    var itemReserved5 : String = ""
    var itemReserved6 : String = ""
    var itemReserved7 : String = ""
    var itemReserved8 : String = ""
    var itemReserved9 : String = ""
    var itemReserved10 : String = ""
    var medicineName : String = ""
    var unitPrice : Double = 0.0
    var itemDiscPc : Double? = null
    var taxCodes : String = ""
    var groupSeq : String = ""
    var soSeq : String = ""
    var brutoUnitPrice : Double = 0.0
    var warehouse : String = "CENTRE"
    var qtyControl : Int = 0
    var doseQ : String = ""
    var doId : String = ""

    fun qtyToString(): String {
        if(this.quantity.rem(1).equals(0.0)) {
            return this.quantity.toInt().toString()
        }
        else {
            return this.quantity.toString()
        }


    }
}