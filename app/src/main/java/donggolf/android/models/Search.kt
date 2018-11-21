package donggolf.android.models

data class Search (
        var content: String? = null,
        var date: Long? = 0
        //,var uid : String? = null
){
    constructor() : this(date = 0)
}
