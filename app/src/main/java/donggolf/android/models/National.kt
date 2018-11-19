package donggolf.android.models

class National (

        var title:String? = null,
        var national:ArrayList<java.util.Map.Entry<String,Long>> = ArrayList<java.util.Map.Entry<String,Long>>(),
        var is_checked:Boolean? = false

){
    constructor() : this(title = "")
}