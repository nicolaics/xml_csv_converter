package xml_csv.xmlcsv

class Salesman {
    private var lastName : String? = null
    private var firstName : String = ""

    fun setLastName(lastName: String?){
        this.lastName = lastName
    }

    fun setFirstName(firstName: String){
        this.firstName = firstName
    }

    fun getLastName() : String?{
        return this.lastName
    }

    fun getFirstName() : String{
        return this.firstName
    }
}