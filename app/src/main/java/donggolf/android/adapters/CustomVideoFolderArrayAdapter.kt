package donggolf.android.adapters

import android.content.Context
import android.database.Cursor
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.base.ImageLoader
import donggolf.android.base.Utils
import org.json.JSONObject

class CustomVideoFolderArrayAdapter(context: Context, textViewResourceId: Int, data: List<JSONObject>) : ArrayAdapter<JSONObject>(context, textViewResourceId, data) {

    private var data = data

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (data!!.size == 0) {
//            convertView = View.inflate(context, R.layout.no_item_list_row, null);

            convertView = View.inflate(context, R.layout.item_custom_gallery_folder, null);

            var tv: TextView = convertView.findViewById(R.id.text);
            tv.setText("결과값이 없습니다.");

            return convertView
        } else if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_custom_gallery_folder, null)
        }
        //        ((ImageView) convertView.findViewById(R.id.img)).setImageResource(R.drawable.noimage);

        val o = data[position]

        convertView!!.tag = position

        var bucketName = Utils.getString(o, "bucketName")

        (convertView.findViewById(R.id.bucketName) as TextView).setText(Utils.getString(o, "bucketName"))

        var total = Utils.getInt(o,"total")
        var image = Utils.getString(o,"image")
        // total
        if (total == -1) {
            val selection = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = '" + bucketName + "'"
            val totalProj = arrayOf(MediaStore.Video.Media._ID)

            val resolver = context!!.contentResolver
            var cursor: Cursor? = null
            try {
//                cursor = MediaStore.Video.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, totalProj)
//                cursor = MediaStore.Images.Media.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, totalProj, selection, null)
                cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, totalProj, selection, null, MediaStore.Video.Media.DATE_ADDED + " DESC")
                if (cursor != null && cursor.moveToFirst()) {
                    total = cursor.count
                    o.put("total", total)
                }
            } finally {
                if (cursor != null && !cursor.isClosed) {
                    cursor.close()
                }
            }
        }
        (convertView.findViewById(R.id.total) as TextView).setText(total.toString())

        val iv = convertView.findViewById(R.id.img) as ImageView

        // 이미지
        if (image == "") {
            val selection = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = '" + bucketName + "'"

            // image
            val proj = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME)
            val idx = IntArray(proj.size)

            val resolver = context!!.contentResolver
            var cursor: Cursor? = null
            try {
                cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, selection, null, MediaStore.Video.Media.DATE_ADDED + " DESC limit 1")
//                cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, selection, MediaStore.Images.Media.DATE_ADDED + " DESC limit 1")
                if (cursor != null && cursor.moveToFirst()) {
                    idx[0] = cursor.getColumnIndex(proj[0])
                    idx[1] = cursor.getColumnIndex(proj[1])
                    idx[2] = cursor.getColumnIndex(proj[2])
//                    idx[3] = cursor.getColumnIndex(proj[3])

                    val photoID = cursor.getInt(idx[0])
                    var photoPath = cursor.getString(idx[1])
                    val displayName = cursor.getString(idx[2])
//                    val orientation = cursor.getInt(idx[3])

                    val bitmap = ThumbnailUtils.createVideoThumbnail(photoPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
                    val thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 360, 480)

//                    val bitmap = Utils.getImage(resolver, photoPath, 200)
//                    val thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 360, 480)

                    if (bitmap != null) {
                        iv.setImageBitmap(bitmap)
                    }
                }
            } finally {
                if (cursor != null && !cursor.isClosed) {
                    cursor.close()
                }
            }
        } else {
//            iv.setImageBitmap(o.getBitmap())
        }
        return convertView
    }
}
