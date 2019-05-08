package donggolf.android.adapters

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore.Images
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.base.ImageLoader
import donggolf.android.base.Utils
import org.json.JSONObject

/**
 * Created by dev1 on 2018-02-08.
 */

class CustomGalleryFolderArrayAdapter(context: Context, textViewResourceId: Int, data: List<JSONObject>) : ArrayAdapter<JSONObject>(context, textViewResourceId, data) {

    private var data = data

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (data.size == 0) {
//            convertView = View.inflate(context, R.layout.no_item_list_row, null);

            convertView = View.inflate(context, R.layout.item_custom_gallery_folder, null)

            var tv:TextView = convertView.findViewById(R.id.text)
            tv.text = "결과값이 없습니다."

            return convertView
        } else if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_custom_gallery_folder, null)
        }
        //        ((ImageView) convertView.findViewById(R.id.img)).setImageResource(R.drawable.noimage);

        val o = data[position]

        convertView!!.tag = position

        val bucketName = Utils.getString(o, "bucketName")

        (convertView.findViewById(R.id.bucketName) as TextView).text = Utils.getString(o, "bucketName")

        var total = Utils.getInt(o,"total")
        val image = Utils.getString(o,"image")
        // total
        if (total == -1) {
            val selection = Images.Media.BUCKET_DISPLAY_NAME + " = '" + bucketName + "'"
            val totalProj = arrayOf(Images.Media._ID)

            val resolver = context.contentResolver
            var cursor: Cursor? = null
            try {
                cursor = Images.Media.query(resolver, Images.Media.EXTERNAL_CONTENT_URI, totalProj, selection, null)
                if (cursor != null && cursor.moveToFirst()) {
                    total = cursor.count
                    o.put("total", total)
                }
            } catch (e:Exception) {
                o.put("total", 0)
            } finally {
                if (cursor != null && !cursor.isClosed) {
                    cursor.close()
                }
            }
        }
        (convertView.findViewById(R.id.total) as TextView).text = total.toString()

        val iv = convertView.findViewById(R.id.img) as ImageView

        // 이미지
        if (image == "") {
            val selection = Images.Media.BUCKET_DISPLAY_NAME + " = '" + bucketName + "'"

            // image
            val proj = arrayOf(Images.Media._ID, Images.Media.DATA, Images.Media.DISPLAY_NAME, Images.Media.ORIENTATION)
            val idx = IntArray(proj.size)

            val resolver = context.contentResolver
            var cursor: Cursor? = null
            try {
                cursor = Images.Media.query(resolver, Images.Media.EXTERNAL_CONTENT_URI, proj, selection, Images.Media.DATE_ADDED + " DESC limit 1")
                if (cursor != null && cursor.moveToFirst()) {
                    idx[0] = cursor.getColumnIndex(proj[0])
                    idx[1] = cursor.getColumnIndex(proj[1])
                    idx[2] = cursor.getColumnIndex(proj[2])
                    idx[3] = cursor.getColumnIndex(proj[3])

                    val photoID = cursor.getInt(idx[0])
                    var photoPath = cursor.getString(idx[1])
                    val displayName = cursor.getString(idx[2])
                    val orientation = cursor.getInt(idx[3])

                    if (displayName != null) {
                        val imageLoader = ImageLoader(resolver)
                        imageLoader.setListener(this)

                        val thumbProj = arrayOf(Images.Thumbnails.DATA)

                        val mini = Images.Thumbnails.queryMiniThumbnail(resolver, photoID.toLong(), Images.Thumbnails.MINI_KIND, thumbProj)
                        if (mini != null && mini.moveToFirst()) {
                            photoPath = mini.getString(mini.getColumnIndex(thumbProj[0]))
                        }

                        /*
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        // BitmapFactory.decodeFile(path, options);
                        options.inJustDecodeBounds = false;
                        options.inSampleSize = 1;
                        if (options.outWidth > 96) {
                            int ws = options.outWidth / 96 + 1;
                            if (ws > options.inSampleSize) {
                                options.inSampleSize = ws;
                            }
                        }
                        if (options.outHeight > 96) {
                            int hs = options.outHeight / 96 + 1;
                            if (hs > options.inSampleSize) {
                                options.inSampleSize = hs;
                            }
                        }
                        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
                        */

                        val bitmap = Utils.getImage(resolver, photoPath, 200)

                        if (bitmap != null) {
                            iv.setImageBitmap(bitmap)
                        }
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
