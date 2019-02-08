package donggolf.android.models

class FriendCategory(
        var cateTitle :String = "",
        var memberCount : String = "",
        var newPost : Boolean = true,
        var newChat : Boolean = true,
        var newLogin : Boolean = true,
        var newFrdPub : Boolean = true
) {
    constructor() : this(cateTitle = "")
}