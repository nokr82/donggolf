package donggolf.android.models

class National (

        var id:String? = null,
        var title:String? = null,
        var is_checked:Boolean? = false

){
    constructor() : this(title = "")
}