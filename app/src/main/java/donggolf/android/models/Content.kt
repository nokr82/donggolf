package donggolf.android.models

data class Content (

        var created:Long? = null,
        var updated:Long? = null,
        var updatedCnt:Int? = null,
        var owner:Int? = null,
        var title:String? = null,
        var text:String? = null,
        var door_image:String? = null,
        var deleted:Boolean? = false,
        var deletedAt:Long? = null
){
    constructor() : this(title = "")
}