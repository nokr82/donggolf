package donggolf.android.models

data class ImagesPath (

        var id:Int? = null,
        var owner:String? = null,
        var path:String? = null

) {

    constructor() : this(owner = "")
}