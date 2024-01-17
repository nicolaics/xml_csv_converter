package xml_csv.xmlcsv

class SearchedMedicine {
    var searchResult = ArrayList<Medicine>()
    var invoiceNo : Long = 0
    var inputtedMedicine : String = ""
    var chosenMedicine = ArrayList<String>()
    var qty : Float = 0.0F
    var unitPriceString : String = ""
}