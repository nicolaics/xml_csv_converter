package xml_csv.xmlcsv

class Invoice{
     var transactionId : Long = 0
     var invoiceNo : Long = 0
     var invoiceDate : String = ""
     var tax1Id : String = ""
     var tax1Code : String = ""
     var tax2Code : String? = null
     var tax1Rate : Int = 0
     var tax2Rate : Int = 0
     var rate : Int = 0
     var inclusiveTax : Int = 0
     var customerIsTaxable : Int = 0
     var cashDiscount : Double = 0.0
     var cashDiscPc : Double? = null
     var invoiceAmount : Double = 0.0
     var freight : Int = 0
     var terms : String = ""
     var fob : String? = null
     var purchaseOrderNo : Int? = null
     var warehouse : String = "CENTRE"
     var description : String = ""
     var shipDate : String = ""
     var deliveryOrder : String? = null
     var fiscalRate : Int? = null
     var taxDate : String = ""
     var customer : String = ""
     var salesman = Salesman()
     var printed : Int = 0
     var shipTo1 : String? = null
     var shipTo2 : String? = null
     var shipTo3 : String? = null
     var shipTo4 : String? = null
     var shipTo5 : String? = null
     var arAccount : Double = 0.0
     var taxFormNumber : Int? = null
     var taxFormCode : String? = null
     var currencyName : String = ""
     var automaticInsertGrouping : String? = null
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