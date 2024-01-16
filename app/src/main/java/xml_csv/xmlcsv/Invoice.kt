package xml_csv.xmlcsv

class Invoice{
     var transactionId : Long = 0
     var invoiceNo : Long = 0
     var invoiceDate : String = ""
     var tax1Id : String = "T"
     var tax1Code : String = "T"
     var tax2Code : String = ""
     var tax1Rate : Int = 11
     var tax2Rate : Int = 0
     var rate : Int = 1
     var inclusiveTax : Int = 1
     var customerIsTaxable : Int = 1
     var cashDiscount : Double = 0.0
     var cashDiscPc : Double? = null
     var invoiceAmount : Double = 0.0
     var freight : Int = 0
     var terms : String = "C.O.D"
     var fob : String = ""
     var purchaseOrderNo : Int? = null
     var warehouse : String = "CENTRE"
     var description : String = ""
     var shipDate : String = ""
     var deliveryOrder : String = ""
     var fiscalRate : Int = 1
     var taxDate : String = ""
     var customer : String = ""
     var salesman = Salesman()
     var printed : Int = 0
     var shipTo1 : String? = null
     var shipTo2 : String = ""
     var shipTo3 : String = ""
     var shipTo4 : String = ""
     var shipTo5 : String = ""
     var arAccount : Double = 110302.0
     var taxFormNumber : Int? = null
     var taxFormCode : String = ""
     var currencyName : String = "IDR"
     var automaticInsertGrouping : String = ""
     var itemLines = ArrayList<ItemLine>()

//    fun setTransactionId(transactionId : Double){
//        this.transactionId = transactionId
//    }
//
//    fun setInvoiceNo(invoiceNo : Double){
//        this.invoiceNo = invoiceNo
//    }
//
//    fun setInvoiceDate(invoiceDate : String){
//        this.invoiceDate = invoiceDate
//    }
}