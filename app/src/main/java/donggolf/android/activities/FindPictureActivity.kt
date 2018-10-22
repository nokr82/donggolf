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
import donggolf.android.adapters.FindPictureAdapter
import donggolf.android.adapters.ImageAdapter
import donggolf.android.adapters.VideoAdapter
import donggolf.android.base.DataBaseHelper
import donggolf.android.base.RootActivity
import donggolf.android.models.PictureCategory
import kotlinx.android.synthetic.main.activity_find_picture.*
import org.json.JSONObject

class FindPictureActivity : RootActivity() {

        private lateinit var context: Context

        private  var adapterData : ArrayList<PictureCategory> = ArrayList<PictureCategory>()

        private  lateinit var  adapter : FindPictureAdapter

        private var videoList: java.util.ArrayList<VideoAdapter.VideoData> = java.util.ArrayList<VideoAdapter.VideoData>()

        private var photoList: ArrayList<ImageAdapter.PhotoData> = ArrayList<ImageAdapter.PhotoData>()

        val SELECT_PICTURE = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_picture)

        context = this


        var intent = getIntent()

        videoSize()
        photoSize()

        var camera = PictureCategory("카메라",photoList.size,1,null)
        var video = PictureCategory("동영상",videoList.size,2,null)


        adapterData.add(camera)
        adapterData.add(video)

        adapter = FindPictureAdapter(context,R.layout.findpicture_listview_item,adapterData)

        findpictre_listview.adapter = adapter

        adapter.notifyDataSetChanged()

        findpictre_listview.setOnItemClickListener { parent, view, position, id ->

            var item = adapterData.get(position)

            if (item.category == 1){
                Log.d("yjs", "category :" + item.category)
                MoveFindPictureGridActivity()
            }else if (item.category == 2){
                MoveFindVideoActivity()
            }


        }





        btn_finish.setOnClickListener {
            finish()
        }


    }
    fun MoveFindVideoActivity(){
        var intent = Intent(context, FindVideoActivity::class.java);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    fun MoveFindPictureGridActivity(){

        var intent = Intent(context, FindPictureGridActivity::class.java);
        startActivityForResult(intent, SELECT_PICTURE);
//        startActivity(Intent(this,FindPictureGridActivity::class.java))
    }

    fun videoSize(){
        val resolver = contentResolver
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val idx = IntArray(proj.size)

//            cursor = MediaStore.Video.Media.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC")
            cursor = MediaStore.Video.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null)
//            cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);

            println(" cursor : " + cursor.count)

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
                        video =  VideoAdapter.VideoData()
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

    fun photoSize(){
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
                        photo =  ImageAdapter.PhotoData()
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

        if(resultCode == Activity.RESULT_OK) {

            when(requestCode) {
                SELECT_PICTURE -> {
                    var result: String = data!!.getStringExtra("images")

                    var intent = Intent()
                    intent.putExtra("images", result)

                    Log.d("yjs", "findpicture : " + result)

                    setResult(RESULT_OK,intent);
                    finish()

                }
            }

        }

    }

}
