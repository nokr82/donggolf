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
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import donggolf.android.R
import donggolf.android.adapters.ImageAdapter
import donggolf.android.base.ImageLoader
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_select_profile_img.*
import java.io.File
import java.io.IOException
import java.util.*

class SelectProfileImgActivity() : RootActivity(), AdapterView.OnItemClickListener {

    private lateinit var context: Context

    private var photoList: ArrayList<ImageAdapter.PhotoData> = ArrayList<ImageAdapter.PhotoData>()

//    private val selected = LinkedList<String>()

    private var imageUri: Uri? = null

    private var FROM_CAMERA: Int = 100

    private val SELECT_PICTURE: Int = 101

    private var imagePath: String? = ""

    private var displayName: String? = ""

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

        //앨범에 있는 사진들 로드
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

        val imageLoader = ImageLoader(resolver)

    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val builder = AlertDialog.Builder(context)
        var photo = photoList.get(position)
        builder
                .setMessage("사진을 등록하시겠습니까 ?")

                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()

                    val returnIntent = Intent()
//                    returnIntent.putExtra("profImg", photo)
                    setResult(RESULT_OK, returnIntent)
                    finish()
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })
        val alert = builder.create()
        alert.show()
    }

    //직접 사진찍어서 올리는 것
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

                imageUri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", photo)
                imagePath = photo.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, FROM_CAMERA)

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    companion object CREATOR : Parcelable.Creator<SelectProfileImgActivity> {
        override fun createFromParcel(parcel: Parcel): SelectProfileImgActivity {
            return SelectProfileImgActivity(parcel)
        }

        override fun newArray(size: Int): Array<SelectProfileImgActivity?> {
            return arrayOfNulls(size)
        }
    }
}
