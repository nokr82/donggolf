package donggolf.android.models

import android.graphics.Bitmap

/**
 * Created by dev1 on 2018-02-08.
 */

class Folder {
    var bucketName: String? = null
    var total = -1
    var bitmap: Bitmap? = null





    override fun toString(): String {
        return "Folder [bucketName=$bucketName, total=$total, bitmap=$bitmap]"
    }

}

