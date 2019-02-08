package donggolf.android.models

data class ImagesPath (

        var id:Int? = null,
        var owner:String? = null,
        var path:String? = null,
        var type:Int? = null

) {

    constructor() : this(owner = "")
}