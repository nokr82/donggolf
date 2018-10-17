package donggolf.android.models

data class Info (

        var phone:String? = null,
        var nick:String? = null,
        var sex:String? = null,
        var agree:Boolean = false

) {
    constructor() : this(phone = "")
}