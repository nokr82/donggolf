package donggolf.android.models

import java.io.Serializable

data class PhotoData (

        var photoID: Int = 0,
        var photoPath: String? = null,
        var bucketPhotoName: String? = null,
        var orientation: Int = 0

) : Serializable{
    constructor() : this(photoID = 0)
}