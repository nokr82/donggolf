package donggolf.android.models

data class TmpContent (
        var id:Int? = null,
        var owner:String? = null,
        var title:String? = null,
        var texts:String? = null,
        var sharp_tag:String? = null

){
    constructor() : this(owner = "")
}