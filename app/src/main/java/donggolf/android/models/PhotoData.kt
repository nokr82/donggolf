package donggolf.android.models

data class PhotoData (

        var photoID: Int = 0,
        var photoPath: String? = null,
        var bucketPhotoName: String? = null,
        var orientation: Int = 0

) {
    constructor() : this(photoID = 0)
}