package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import donggolf.android.R
import donggolf.android.adapters.VideoAdapter
import donggolf.android.base.ImageLoader
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_find_video.*
import java.io.File
import java.io.IOException
import java.util.*

class FindVideoActivity() : RootActivity(), AdapterView.OnItemClickListener {
    private lateinit var context: Context

    private var videoList: ArrayList<VideoAdapter.VideoData> = ArrayList<VideoAdapter.VideoData>()


    private val selected = LinkedList<String>()

    private var videoUri: Uri? = null

    private var FROM_VIDEO: Int = 101

    private val SELECT_VIDEO: Int = 102

    private  var videoPath : String? = ""

    private var count: Int = 0

    private lateinit var mAuth: FirebaseAuth


    constructor(parcel: Parcel) : this() {
        videoUri = parcel.readParcelable(Uri::class.java.classLoader)
        FROM_VIDEO = parcel.readInt()
        videoPath = parcel.readString()
        count = parcel.readInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_video)

        context = this

        mAuth = FirebaseAuth.getInstance();

        videofinishBT.setOnClickListener {
            finish()
        }

        val resolver = contentResolver
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME,MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val idx = IntArray(proj.size)

//            cursor = MediaStore.Video.Media.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC")
//            cursor = MediaStore.Video.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null)
//            cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC")
                // println(" cursor : " + cursor.count)
            } else {
                cursor = MediaStore.Video.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null)
            }

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
                        video.displayName = displayName
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



        videoselectGV.setOnItemClickListener(this)

        val imageLoader: ImageLoader = ImageLoader(resolver)

        val adapter = VideoAdapter(this, videoList, imageLoader,selected)

        videoselectGV.setAdapter(adapter)

        imageLoader.setListener(adapter)

        adapter.notifyDataSetChanged()

        addvideoBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("동영상을 등록하시겠습니까 ?")

                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                        val result = arrayOfNulls<String>(selected.size)
                        val name  = arrayOfNulls<String>(selected.size)

                        var idx = 0
                        var idxv = 0

                        for (strPo in selected) {
                            result[idx++] = videoList[Integer.parseInt(strPo)].videoPath
                            name[idxv++] = videoList[Integer.parseInt(strPo)].displayName
                        }

                        val returnIntent = Intent()
                        returnIntent.putExtra("videos", result)
                        returnIntent.putExtra("displayname", name)
                        setResult(RESULT_OK, returnIntent)
                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                    })


            val alert = builder.create()
            alert.show()
        }

    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val strPo = position.toString()

        val video_id = videoList[position].videoID


        if (video_id == -1) {

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

            } else {
                takeVideo()
            }

            if (selected.contains(strPo)) {
                selected.remove(strPo)

                videocountTV.text = selected.size.toString()

                val adapter = videoselectGV.getAdapter()
                if (adapter != null) {
                    val f = adapter as VideoAdapter
                    (f as BaseAdapter).notifyDataSetChanged()
                }

            } else {
                if (selected.size  + 1 > 1) {
                    Toast.makeText(context, "동영상은 1개까지 등록가능합니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                selected.add(strPo)

                videocountTV.text = selected.size.toString()

                val adapter = videoselectGV.getAdapter()
                if (adapter != null) {
                    val f = adapter as VideoAdapter
                    (f as BaseAdapter).notifyDataSetChanged()
                }
            }
        }
    }

    private fun takeVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {

            // File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)

            // File photo = new File(dir, System.currentTimeMillis() + ".jpg");

            try {
                val photo = File.createTempFile(
                        System.currentTimeMillis().toString(), /* prefix */
                        ".jpg", /* suffix */
                        storageDir      /* directory */
                )

                //                imageUri = Uri.fromFile(photo);
                videoUri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", photo)
                videoPath = photo.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, videoPath)
                startActivityForResult(intent, FROM_VIDEO)

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }


    fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(videoUri, flags)
        parcel.writeInt(FROM_VIDEO)
        parcel.writeString(videoPath)
        parcel.writeInt(count)
    }


    companion object CREATOR : Parcelable.Creator<FindVideoActivity> {
        override fun createFromParcel(parcel: Parcel): FindVideoActivity {
            return FindVideoActivity(parcel)
        }

        override fun newArray(size: Int): Array<FindVideoActivity?> {
            return arrayOfNulls(size)
        }
    }


}
