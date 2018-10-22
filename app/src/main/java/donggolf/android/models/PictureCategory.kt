package donggolf.android.models

import android.net.Uri

data class PictureCategory (
        var title: String? = null,
        var count: Int? = 0,
        var category: Int? = 0,
        var imageUri: String? = null

){
    constructor() : this(category = 0)
}
