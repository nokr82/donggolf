package donggolf.android.models

class Mate (

        var visitCnt:Long? = 0,
        var standBy:String? = null,
        var blocking:String? = null,
        var mate1:String? = null,
        var agree:Boolean = false

) {
    constructor() : this(standBy = "")
}