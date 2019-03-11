package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import donggolf.android.R
import donggolf.android.adapters.ImageAdapter
import donggolf.android.adapters.VideoAdapter
import donggolf.android.base.ImageLoader
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_find_picture_grid.*
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class FindPictureGridActivity() : RootActivity(), AdapterView.OnItemClickListener {
    private lateinit var context: Context

    private var photoList: ArrayList<ImageAdapter.PhotoData> = ArrayList<ImageAdapter.PhotoData>()
    private var videoList: ArrayList<VideoAdapter.VideoData> = ArrayList<VideoAdapter.VideoData>()

    private val selected = LinkedList<String>()

    private var imageUri: Uri? = null

    private var FROM_CAMERA: Int = 100

    private val SELECT_PICTURE: Int = 101

    private var imagePath: String? = ""

    private var displayName: String? = ""

    private var count: Int = 0

    private lateinit var mAuth: FirebaseAuth

    private var selectedImage: Bitmap? = null

    var category = "image"
    private var picCount = 0
    private var limit = 0
    private var nowPicCount = 0

    constructor(parcel: Parcel) : this() {
        imageUri = parcel.readParcelable(Uri::class.java.classLoader)
        FROM_CAMERA = parcel.readInt()
        imagePath = parcel.readString()
        count = parcel.readInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_picture_grid)

        context = this

        var intent = getIntent()
        if (intent.getStringExtra("category") != null) {
            category = intent.getStringExtra("category")
            val bucketName = getIntent().getStringExtra("bucketName")
            val total = getIntent().getIntExtra("total", 0)
            picCount = getIntent().getIntExtra("pic_count", 0)
            limit = getIntent().getIntExtra("limit", 0)
            nowPicCount = getIntent().getIntExtra("nowPicCount", 1)

            if (category == "image" || category == "profile") {

                val resolver = contentResolver
                var cursor: Cursor? = null
                try {
                    val proj = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.ORIENTATION)
                    val idx = IntArray(proj.size)

                    val selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = '" + bucketName + "'"

                    cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, selection, MediaStore.Images.Media.DATE_ADDED + " DESC")
                    println(" cursor : " + cursor.count)
                    if (cursor != null && cursor.moveToFirst()) {
                        idx[0] = cursor.getColumnIndex(proj[0])
                        idx[1] = cursor.getColumnIndex(proj[1])
                        idx[2] = cursor.getColumnIndex(proj[2])
                        idx[3] = cursor.getColumnIndex(proj[3])

                        do {
                            val photoID = cursor.getInt(idx[0])
                            val photoPath = cursor.getString(idx[1])
                            val displayName = cursor.getString(idx[2])
                            val orientation = cursor.getInt(idx[3])
                            if (displayName != null) {
                                val photo = ImageAdapter.PhotoData()
                                photo.photoID = photoID
                                photo.photoPath = photoPath
                                photo.orientation = orientation
                                photoList.add(photo)
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

                selectGV.setOnItemClickListener(this)

                val imageLoader: ImageLoader = ImageLoader(resolver)

                val adapter = ImageAdapter(this, photoList, imageLoader, selected)

                selectGV.adapter = adapter

                imageLoader.setListener(adapter)

                adapter.notifyDataSetChanged()

            } else {
                titleTV.text = "동영상선택"
                val resolver = contentResolver
                var cursor: Cursor? = null
                try {
                    val proj = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                    val idx = IntArray(proj.size)

                    val selection = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = '" + bucketName + "'"

                    cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, selection, null, MediaStore.Video.Media.DATE_ADDED + " DESC")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        println(" cursor : " + cursor.count)
                    } else {
//                        cursor = MediaStore.Video.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null)
                    }
//                    cursor = MediaStore.Images.Media.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, selection, MediaStore.Video.Media.DATE_ADDED + " DESC")
                    if (cursor != null && cursor.moveToFirst()) {
                        idx[0] = cursor.getColumnIndex(proj[0])
                        idx[1] = cursor.getColumnIndex(proj[1])
                        idx[2] = cursor.getColumnIndex(proj[2])
                        idx[3] = cursor.getColumnIndex(proj[3])

                        do {
                            val photoID = cursor.getInt(idx[0])
                            val photoPath = cursor.getString(idx[1])
                            val displayName = cursor.getString(idx[2])
                            val orientation = cursor.getInt(idx[3])
                            if (displayName != null) {
                                val video = VideoAdapter.VideoData()
                                video.videoID = photoID
                                video.videoPath = photoPath
                                video.orientation = orientation
                                videoList.add(video)
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

                selectGV.setOnItemClickListener(this)

                val imageLoader: ImageLoader = ImageLoader(resolver)

                val adapter = VideoAdapter(this, videoList, imageLoader, selected)

                selectGV.setAdapter(adapter)

                imageLoader.setListener(adapter)

                adapter.notifyDataSetChanged()
            }
        }


//        mAuth = FirebaseAuth.getInstance();
//        var cursor: Cursor? = null
//        val resolver = contentResolver
//
//        try {
//            val proj = arrayOf(
//                    MediaStore.Images.Media._ID,
//                    MediaStore.Images.Media.DATA,
//                    MediaStore.Images.Media.DISPLAY_NAME,
//                    MediaStore.Images.Media.ORIENTATION,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME
//            )
//            val idx = IntArray(proj.size)
//
//            cursor = MediaStore.Images.Media.query(
//                    resolver,
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    null,
//                    null,
//                    MediaStore.Images.Media.DATE_ADDED + " DESC"
//            )
//            if (cursor != null && cursor.moveToFirst()) {
//                idx[0] = cursor.getColumnIndex(proj[0])
//                idx[1] = cursor.getColumnIndex(proj[1])
//                idx[2] = cursor.getColumnIndex(proj[2])
//                idx[3] = cursor.getColumnIndex(proj[3])
//                idx[4] = cursor.getColumnIndex(proj[4])
//
//                var photo = ImageAdapter.PhotoData()
//
//                do {
//                    val photoID = cursor.getInt(idx[0])
//                    val photoPath = cursor.getString(idx[1])
//                    val displayName = cursor.getString(idx[2])
//                    val orientation = cursor.getInt(idx[3])
//                    val bucketDisplayName = cursor.getString(idx[4])
//                    if (displayName != null) {
//                        photo = ImageAdapter.PhotoData()
//                        photo.photoID = photoID
//                        photo.photoPath = photoPath
//                        //Log.d("yjs", "name : " + displayName)
//                        photo.displayName = displayName
//                        photo.orientation = orientation
//                        photo.bucketPhotoName = bucketDisplayName
//                        photoList!!.add(photo)
//                    }
//
//                } while (cursor.moveToNext())
//
//                cursor.close()
//            }
//        } catch (ex: Exception) {
//            // Log the exception's message or whatever you like
//        } finally {
//            try {
//                if (cursor != null && !cursor.isClosed) {
//                    cursor.close()
//                }
//            } catch (ex: Exception) {
//            }
//
//        }


        finishBT.setOnClickListener {
            //            try {
//                if (cursor != null && !cursor.isClosed) {
//                    cursor.close()
//                }
//            } catch (ex: Exception) {
//            }
            finish()
        }

        addpostBT.setOnClickListener {

            if (selected != null) {

//                    var bt: Bitmap = Utils.getImage(context.getContentResolver(), selected[0], 10)

                val builder = AlertDialog.Builder(context)
                if (category == "image") {
                    builder.setMessage("사진을 등록하시겠습니까 ?")
                } else if (category == "profile") {
                    builder.setMessage("프로필사진을 변경하시겠습니까 ?")
                } else {
                    builder.setMessage("동영상을 등록하시겠습니까 ?")
                }


                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()


                            val ids = ArrayList<Int>(selected.size)
                            val result = arrayOfNulls<String>(selected.size)
                            val name = arrayOfNulls<String>(selected.size)

                            var idx = 0
                            var idxn = 0

                            if (category == "image" || category == "profile") {

                                for (strPo in selected) {
                                    result[idx++] = photoList[Integer.parseInt(strPo)].photoPath
                                    name[idxn++] = photoList[Integer.parseInt(strPo)].displayName

//                                    Log.d("yjs", "Path : " + photoList[0].photoPath + photoList[0].displayName)
                                }

                                if (category == "profile") {
                                    Toast.makeText(context, "프로필 사진이 변경 되었습니다.", Toast.LENGTH_SHORT).show()
                                }

                                val returnIntent = Intent()
                                returnIntent.putExtra("images", result)
                                returnIntent.putExtra("displayname", name)
                                setResult(RESULT_OK, returnIntent)
//                            try {
//                                if (cursor != null && !cursor.isClosed) {
//                                    cursor.close()
//                                }
//                            } catch (ex: Exception) {
//                            }
                                finish()
                                println("확인time:::::::::::::::::::::::::::${System.currentTimeMillis()}")
                            } else {

                                for (strPo in selected) {
                                    ids.add(videoList[Integer.parseInt(strPo)].videoID)
                                    result[idx] = videoList[Integer.parseInt(strPo)].videoPath
                                    name[idxn] = videoList[Integer.parseInt(strPo)].displayName
                                    idx++
                                    idxn++
                                }

                                val returnIntent = Intent()
                                returnIntent.putExtra("videos", result)
                                returnIntent.putExtra("displayname", name)
                                returnIntent.putExtra("ids", ids)
                                returnIntent.action = "REGION_CHANGE"
                                sendBroadcast(intent)
                                setResult(RESULT_OK, returnIntent)
                                finish()
                            }
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                        })
                val alert = builder.create()
                alert.show()
            }

        }


    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

        if (category == "image" || category == "profile") {

            val strPo = position.toString()

            val photo_id = photoList[position].photoID

            if (photo_id == -1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

                } else {
                    takePhoto()
                }
            } else {
                if (selected.contains(strPo)) {
                    selected.remove(strPo)

                    countTV.text = selected.size.toString()

                    println("---selected.size ${selected.size}")

                    val adapter = selectGV.getAdapter()
                    if (adapter != null) {
                        val f = adapter as ImageAdapter
                        (f as BaseAdapter).notifyDataSetChanged()
                    }

                } else {
                    if (category == "image") {
                        if (count + selected.size > 9) {
                            Toast.makeText(context, "사진은 10개까지 등록가능합니다.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        selected.add(strPo)

                        countTV.text = selected.size.toString()

                        val adapter = selectGV.getAdapter()
                        if (adapter != null) {
                            val f = adapter as ImageAdapter
                            (f as BaseAdapter).notifyDataSetChanged()
                        }
                    } else {
                        if (count + selected.size > 0) {
                            Toast.makeText(context, "프로필 사진변경은 1장만 선택하여 주세요.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        selected.add(strPo)

                        countTV.text = selected.size.toString()

                        val adapter = selectGV.getAdapter()
                        if (adapter != null) {
                            val f = adapter as ImageAdapter
                            (f as BaseAdapter).notifyDataSetChanged()
                        }
                    }

                }
            }
        } else {
            val strPo = position.toString()

            val video_id = videoList[position].videoID


            if (video_id == -1) {

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

                } else {
//                    takeVideo()
                }

                if (selected.contains(strPo)) {
                    selected.remove(strPo)

                    countTV.text = selected.size.toString()

                    val adapter = selectGV.getAdapter()
                    if (adapter != null) {
                        val f = adapter as VideoAdapter
                        (f as BaseAdapter).notifyDataSetChanged()
                    }

                } else {
                    if (selected.size + 1 > 1) {
                        Toast.makeText(context, "동영상은 1개까지 등록가능합니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    selected.add(strPo)

                    countTV.text = selected.size.toString()

                    val adapter = selectGV.getAdapter()
                    if (adapter != null) {
                        val f = adapter as VideoAdapter
                        (f as BaseAdapter).notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {

            // File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            // File photo = new File(dir, System.currentTimeMillis() + ".jpg");

            try {
                val photo = File.createTempFile(
                        System.currentTimeMillis().toString(), /* prefix */
                        ".jpg", /* suffix */
                        storageDir      /* directory */
                )

                //                imageUri = Uri.fromFile(photo);
                imageUri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", photo)
                Log.d("yjs", "Uri : " + imageUri)
                imagePath = photo.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, FROM_CAMERA)

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    companion object CREATOR : Parcelable.Creator<FindPictureGridActivity> {
        override fun createFromParcel(parcel: Parcel): FindPictureGridActivity {
            return FindPictureGridActivity(parcel)
        }

        override fun newArray(size: Int): Array<FindPictureGridActivity?> {
            return arrayOfNulls(size)
        }
    }

}
