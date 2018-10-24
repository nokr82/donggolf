package donggolf.android.models

data class Content (

        var createAt:Long? = null,
        var updatedAt:Long? = null,
        var updatedCnt:Int? = null,
        var owner:String? = null,
        var region:Region? = null,
        var title:String?= null,
        var texts:ArrayList<String> = ArrayList<String>(),
        var door_image:String? = null,
        var deleted:Boolean? = false,
        var deletedAt:Long? = null,
        var chargecnt:Int? = 0,
        var charge_user:String? = null,
        var heart_user:Boolean? = false,
        var looker:Int? = 0,
        var exclude_looker:Boolean? = false,
        var sharp_tag:ArrayList<String> = ArrayList<String>()

){
    constructor() : this(createAt = 0)
}