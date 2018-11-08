package donggolf.android.models

data class Text (

        var type:String? = null,
        var text:String? = null

) {
    constructor() : this(type = "")
}