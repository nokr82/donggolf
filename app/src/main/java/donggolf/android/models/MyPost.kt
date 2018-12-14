package donggolf.android.models

class MyPost (
        var mpTitle : String? = null,
        var mpCont : String? = null,
        var mpNick : String? = null,
        var mpDate : String? = null,
        var mpComment : String? = null,
        var mpType : String? = null
) {
    constructor() : this(mpTitle = "")
}