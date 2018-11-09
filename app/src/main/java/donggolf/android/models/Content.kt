package donggolf.android.models

data class Content (

        var createAt:Long? = null,
        var updatedAt:Long? = null,
        var updatedCnt:Long? = null,
        var owner:String? = null,
        var region:ArrayList<String> = ArrayList<String>(),
        var title:String? = null,
        var texts:ArrayList<Any> = ArrayList<Any>(),
        var door_image:String? = null,
        var deleted:Boolean? = false,
        var deletedAt:Long? = null,
        var chargecnt:Long? = null,
        var charge_user:ArrayList<String> = ArrayList<String>(),
        var heart_user:Boolean? = false,
        var looker:Long? = null,
        var exclude_looker:ArrayList<String> = ArrayList<String>(),

        var sharp_tag:ArrayList<String> = ArrayList<String>()

){
    constructor() : this(createAt = 0)
}