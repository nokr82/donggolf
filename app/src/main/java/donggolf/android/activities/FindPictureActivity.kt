package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import donggolf.android.R
import donggolf.android.adapters.CustomVideoFolderArrayAdapter
import donggolf.android.adapters.FindPictureAdapter
import donggolf.android.adapters.ImageAdapter
import donggolf.android.adapters.VideoAdapter
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.PictureCategory
import kotlinx.android.synthetic.main.activity_find_picture.*
import org.json.JSONObject

class FindPictureActivity : RootActivity() {

    private lateinit var context: Context

    private val data: ArrayList<JSONObject> = ArrayList<JSONObject>()
    private val videodata: ArrayList<JSONObject> = ArrayList<JSONObject>()
    private val buckets: ArrayList<String> = ArrayList<String>()
    private val videobuckets: ArrayList<String> = ArrayList<String>()

    private var picCount = 0
    private var limit = 0
    private var nowPicCount = 0
    private var time = 0L

    var pics: MutableList<String> = ArrayList<String>()

    private var adapterData: ArrayList<PictureCategory> = ArrayList<PictureCategory>()

    private lateinit var adapter: CustomGalleryFolderArrayAdapter
    private lateinit var videoadapter: CustomVideoFolderArrayAdapter

    private var videoList: java.util.ArrayList<VideoAdapter.VideoData> = java.util.ArrayList<VideoAdapter.VideoData>()

    private var photoList: ArrayList<ImageAdapter.PhotoData> = ArrayList<ImageAdapter.PhotoData>()

    val SELECT_PICTURE = 101
    val SELECT_VIDEO = 102

    var image = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_picture)

        context = this

        var intent = getIntent()
//        time = intent.getLongExtra("time", 0L)

//        println("startactivirtytime:::::${System.currentTimeMillis()}")

//        videoSize()
//        photoSize()

        if (intent.getStringExtra("image") != null) {
            image = intent.getStringExtra("image")
            if (image == "image" || image == "profile") {

                val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val c = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
                if (c.count > 0) {
                    c.moveToFirst()
                    do {
                        val bucketDisplayName = c.getString(c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))

                        if (!buckets.contains(bucketDisplayName)) {
                            buckets.add(bucketDisplayName)

                            val jsonObject = JSONObject()
                            jsonObject.put("bucketName", bucketDisplayName)
                            jsonObject.put("total", -1)
                            data.add(jsonObject)

                        }
                    } while (c.moveToNext())
                }

                c.close()
                adapter = CustomGalleryFolderArrayAdapter(context, R.layout.item_custom_gallery_folder, data)
                findpictre_listview.setAdapter(adapter)

            } else {

                titleTV.setText("동영상선택")
                val projection = arrayOf(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val c = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
                if (c.count > 0) {
                    c.moveToFirst()
                    do {
                        val bucketDisplayName = c.getString(c.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))

                        if (!videobuckets.contains(bucketDisplayName)) {
                            videobuckets.add(bucketDisplayName)

                            val jsonObject = JSONObject()
                            jsonObject.put("bucketName", bucketDisplayName)
                            jsonObject.put("total", -1)
                            videodata.add(jsonObject)

                        }
                    } while (c.moveToNext())
                }

                c.close()
                // println("videodata${videodata}")
                videoadapter = CustomVideoFolderArrayAdapter(context, R.layout.item_custom_gallery_folder, videodata)
                findpictre_listview.setAdapter(videoadapter)

            }
        }

        findpictre_listview.setOnItemClickListener { parent, view, position, id ->
            if (image == "image") {
                val item = data.get(position)
                val bucketName = Utils.getString(item, "bucketName")
                val total = Utils.getInt(item, "total")

                val intent = Intent(context, FindPictureGridActivity::class.java)
                intent.putExtra("bucketName", bucketName)
                intent.putExtra("total", total)
                intent.putExtra("pic_count", picCount)
                intent.putExtra("limit", limit)
                intent.putExtra("nowPicCount", nowPicCount)
                intent.putExtra("category", "image")
//                intent.putExtra("time", time)
                startActivityForResult(intent, SELECT_PICTURE)

            }else if (image=="profile"){
                val item = data.get(position)
                val bucketName = Utils.getString(item, "bucketName")
                val total = Utils.getInt(item, "total")

                val intent = Intent(context, FindPictureGridActivity::class.java)
                intent.putExtra("bucketName", bucketName)
                intent.putExtra("total", total)
                intent.putExtra("pic_count", picCount)
                intent.putExtra("limit", limit)
                intent.putExtra("nowPicCount", nowPicCount)
                intent.putExtra("category", "profile")
//                intent.putExtra("time", time)
                startActivityForResult(intent, SELECT_PICTURE)
            }else {
                val item = videodata.get(position)
                val bucketName = Utils.getString(item, "bucketName")
                val total = Utils.getInt(item, "total")

                val intent = Intent(context, FindPictureGridActivity::class.java)
                intent.putExtra("bucketName", bucketName)
                intent.putExtra("total", total)
                intent.putExtra("pic_count", picCount)
                intent.putExtra("limit", limit)
                intent.putExtra("nowPicCount", nowPicCount)
                intent.putExtra("category", "video")
                startActivityForResult(intent, SELECT_VIDEO)

            }
        }



        finishLL.setOnClickListener {
            finish()
        }


    }

    fun MoveFindVideoActivity() {
        var intent = Intent(context, FindVideoActivity::class.java);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    fun MoveFindPictureGridActivity() {

        var intent = Intent(context, FindPictureGridActivity::class.java);
        startActivityForResult(intent, SELECT_PICTURE);
//        startActivity(Intent(this,FindPictureGridActivity::class.java))
    }

    fun videoSize() {
        val resolver = contentResolver
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val idx = IntArray(proj.size)

//            cursor = MediaStore.Video.Media.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC")
            cursor = MediaStore.Video.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null)
//            cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);

            // println(" cursor : " + cursor.count)

            if (cursor != null && cursor.moveToFirst()) {

                idx[0] = cursor.getColumnIndex(proj[0])
                idx[1] = cursor.getColumnIndex(proj[1])
                idx[2] = cursor.getColumnIndex(proj[2])
                idx[3] = cursor.getColumnIndex(proj[3])

                var video = VideoAdapter.VideoData()

                do {
                    val videoID = cursor.getInt(idx[0])
                    val videoPath = cursor.getString(idx[1])
                    val displayName = cursor.getString(idx[2])
                    val bucketDisplayName = cursor.getString(idx[3])

                    if (displayName != null) {
                        video = VideoAdapter.VideoData()
                        video.videoID = videoID
                        video.videoPath = videoPath
                        video.bucketVideoName = bucketDisplayName
                        videoList!!.add(video)
                    }

                } while (cursor.moveToNext())

                cursor.close()
            }
        } catch (ex: Exception) {
            // Log the exception's message or whatever you like
        } finally {
            try {
                if (cursor != null && !cursor.isClosed) {
                    cursor.close()
                }
            } catch (ex: Exception) {
            }

        }
    }

    fun photoSize() {
        val resolver = contentResolver
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.ORIENTATION, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val idx = IntArray(proj.size)

            cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")
            if (cursor != null && cursor.moveToFirst()) {
                idx[0] = cursor.getColumnIndex(proj[0])
                idx[1] = cursor.getColumnIndex(proj[1])
                idx[2] = cursor.getColumnIndex(proj[2])
                idx[3] = cursor.getColumnIndex(proj[3])
                idx[4] = cursor.getColumnIndex(proj[4])

                var photo = ImageAdapter.PhotoData()

                do {
                    val photoID = cursor.getInt(idx[0])
                    val photoPath = cursor.getString(idx[1])
                    val displayName = cursor.getString(idx[2])
                    val orientation = cursor.getInt(idx[3])
                    val bucketDisplayName = cursor.getString(idx[4])
                    if (displayName != null) {
                        photo = ImageAdapter.PhotoData()
                        photo.photoID = photoID
                        photo.photoPath = photoPath
                        photo.orientation = orientation
                        photo.bucketPhotoName = bucketDisplayName
                        photoList!!.add(photo)
                    }

                } while (cursor.moveToNext())

                cursor.close()
            }
        } catch (ex: Exception) {
            // Log the exception's message or whatever you like
        } finally {
            try {
                if (cursor != null && !cursor.isClosed) {
                    cursor.close()
                }
            } catch (ex: Exception) {
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                SELECT_PICTURE -> {
                    var item = data?.getStringArrayExtra("images")
                    var name = data?.getStringArrayExtra("displayname")

                    var intent = Intent()
                    intent.putExtra("images", item)
                    intent.putExtra("displayname", name)

                    //Log.d("yjs", "findpicture : " + item)

                    setResult(RESULT_OK, intent)
                    finish()

                }

                SELECT_VIDEO -> {
                    var ids = data?.getIntegerArrayListExtra("ids")
                    var item = data?.getStringArrayExtra("videos")
                    var name = data?.getStringArrayExtra("displayname")

                    var intent = Intent()
                    intent.putExtra("videos", item)
                    intent.putExtra("displayname", name)
                    intent.putExtra("ids", ids)

                    //Log.d("yjs", "findpicture : " + item)
                    //Log.d("yjs", "video_ids : " + ids)

                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

        }

    }

}
