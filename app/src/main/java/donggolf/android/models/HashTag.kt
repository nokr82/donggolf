package donggolf.android.models

data class HashTag (

        var id :Int? = null,
        var owner:String? = null,
        var tags:String? = null

) {
    constructor() : this(owner = "")
}