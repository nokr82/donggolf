package donggolf.android.models

class National (

        var title:String? = null,
        var national:ArrayList<java.util.HashMap<String, Long>> = ArrayList<java.util.HashMap<String, Long>>(),
        var is_checked:Boolean? = false

){
    constructor() : this(title = "")
}