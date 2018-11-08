package donggolf.android.models

data class TmpContent (
        var id:Int? = null,
        var owner:String? = null,
        var title:String? = null,
        var texts:String? = null,
        var sharp_tag:String? = null,
        // division 0 : 저장안함 , 1 : 저장함
        var division:Int? = null

){
    constructor() : this(owner = "")
}