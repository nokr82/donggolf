package donggolf.android.models

class Users (
        var imgl : String? = null,
        var imgs : String? = null,
        var last : Long = 0,
        var nick : String? = null,
        var sex : String? = null,
        var sharpTag : ArrayList<String> = ArrayList<String>(),
        var state_msg : String? = null
){
    constructor() : this(imgl = "")
}