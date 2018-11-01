package donggolf.android.models

import java.io.Serializable

class MutualFriendData (

    var profileImg: Serializable? = null,
    var nickName:String? = null,
    var firstiage:Serializable? = null,
    var condition:String? = null

){
    constructor() : this(nickName = null)
}