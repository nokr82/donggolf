package donggolf.android.models

data class VideoPath (

        var id :Int? = null,
        var owner:String? = null,
        var path:String? = null

) {
    constructor() : this(owner = "")
}