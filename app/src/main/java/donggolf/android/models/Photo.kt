package donggolf.android.models

data class Photo (

        var type:String? = null,
        var file:ArrayList<String> = ArrayList<String>()

) {
    constructor() : this(type = "")
}