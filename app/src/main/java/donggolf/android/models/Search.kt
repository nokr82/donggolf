package donggolf.android.models

data class Search (
        var content: String? = null,
        var date: Long? = 0

){
    constructor() : this(date = 0)
}
