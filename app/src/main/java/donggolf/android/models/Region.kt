package donggolf.android.models

data class Region (

        var wriedAt:Long? = null,
        var region:String? = null,
        var link:String? = null

) {
    constructor() : this(wriedAt = 0)
}