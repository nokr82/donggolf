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
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.actions.PostAction
import donggolf.android.adapters.ImageAdapter
import donggolf.android.base.*
import donggolf.android.models.Photo
import kotlinx.android.synthetic.main.activity_find_picture_grid.*
import kotlinx.android.synthetic.main.activity_select_profile_img.*
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.lang.reflect.Member
import java.util.*

class SelectProfileImgActivity() : RootActivity(), AdapterView.OnItemClickListener {

    private lateinit var context: Context

    private var photoList: ArrayList<ImageAdapter.PhotoData> = ArrayList<ImageAdapter.PhotoData>()

    private val selected = LinkedList<String>()

    private var imageUri: Uri? = null

    private var FROM_CAMERA: Int = 100

    private val SELECT_PICTURE: Int = 101

    private var imagePath: String? = ""

    private var displaynamePaths: String? = ""

    private var count: Int = 0

    private lateinit var mAuth: FirebaseAuth

    private var selectedImage: Bitmap? = null

    constructor(parcel: Parcel) : this() {
        imageUri = parcel.readParcelable(Uri::class.java.classLoader)
        FROM_CAMERA = parcel.readInt()
        imagePath = parcel.readString()
        count = parcel.readInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_profile_img)

        context = this

        mAuth = FirebaseAuth.getInstance()

        val resolver = contentResolver
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
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
                        photo.displayName = displayName
                        //Log.d("yjs", "name : " + displayName)
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

        profileGV.setOnItemClickListener(this)

        val imageLoader: ImageLoader = ImageLoader(resolver)

        val adapter = ImageAdapter(this, photoList, imageLoader, selected)

        profileGV.setAdapter(adapter)

        imageLoader.setListener(adapter)

        adapter.notifyDataSetChanged()

        /*profileSelFinLL.setOnClickListener {
            finish()
        }*/

        profileSelFinLL.setOnClickListener {

            if (selected != null) {

//                    var bt: Bitmap = Utils.getImage(context.getContentResolver(), selected[0], 10)

                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("사진을 등록하시겠습니까 ?")

                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()

                            /*val result = arrayOfNulls<String>(selected.size)
                            val name = arrayOfNulls<String>(selected.size)

                            var idx = 0
                            var idxn = 0

                            for (strPo in selected) {
                                result[idx++] = photoList[Integer.parseInt(strPo)].photoPath
                                name[idxn++] = photoList[Integer.parseInt(strPo)].displayName
                            }
                            */
                            displaynamePaths = photoList[1].displayName
                            uploadProfileImage()

                            /*val returnIntent = Intent()
//                            returnIntent.putExtra("images", result)
//                            returnIntent.putExtra("displayname", name)
                            setResult(RESULT_OK, returnIntent)
                            finish()*/
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                            finish()
                        })
                val alert = builder.create()
                alert.show()
            }

        }
    }

    fun uploadProfileImage() {

        var bt: Bitmap = Utils.getImage(context.contentResolver, displaynamePaths, 800)
        println("displaynamePaths $displaynamePaths")
        println("이미지 ::: ${ByteArrayInputStream(Utils.getByteArray(bt))}")
        println("bt ------ $bt")

        val params = RequestParams()

        params.put("files",  ByteArrayInputStream(Utils.getByteArray(bt)))
        params.put("type", "image")
        params.put("member_id",PrefUtils.getIntPreference(context, "member_id"))

        MemberAction.update_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                setResult(RESULT_OK,intent)
                finish()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }
            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                if (errorResponse != null)
                    println(errorResponse!!.getString("message"))
            }
        })
    }


    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val strPo = position.toString()

        val photo_id = photoList[position].photoID

        if (photo_id == -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

            }/* else {
                takePhoto()
            }*/
        } else {
            if (selected.contains(strPo)) {
                selected.remove(strPo)

                val adapter = selectGV.getAdapter()
                if (adapter != null) {
                    val f = adapter as ImageAdapter
                    (f as BaseAdapter).notifyDataSetChanged()
                }

            } else {
                if (count + selected.size > 1) {
                    //Toast.makeText(context, "사진은 10개까지 등록가능합니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                selected.add(strPo)


                val adapter = profileGV.getAdapter()
                if (adapter != null) {
                    val f = adapter as ImageAdapter
                    (f as BaseAdapter).notifyDataSetChanged()
                }
            }
        }
    }

    /*private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {

            // File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            // File photo = new File(dir, System.currentTimeMillis() + ".jpg");

            try {
                val photo = File.createTempFile(
                        System.currentTimeMillis().toString(), *//* prefix *//*
                        ".jpg", *//* suffix *//*
                        storageDir      *//* directory *//*
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
    }*/

    companion object CREATOR : Parcelable.Creator<SelectProfileImgActivity> {
        override fun createFromParcel(parcel: Parcel): SelectProfileImgActivity {
            return SelectProfileImgActivity(parcel)
        }

        override fun newArray(size: Int): Array<SelectProfileImgActivity?> {
            return arrayOfNulls(size)
        }
    }
}
