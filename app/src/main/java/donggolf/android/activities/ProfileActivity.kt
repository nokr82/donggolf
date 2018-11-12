package donggolf.android.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.*
import android.util.Log
import donggolf.android.R
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : RootActivity() {

    lateinit var context: Context
    val SELECT_PROFILE = 104
    private var pimgPaths: ArrayList<String> = ArrayList<String>()
    private var images: ArrayList<Bitmap> = ArrayList()
    private var strPaths: ArrayList<String> = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_picture_grid)

        context = this

        showProfImg.setOnClickListener {
            var intent = Intent(context, FindPictureGridActivity::class.java)
            startActivityForResult(intent, SELECT_PROFILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SELECT_PROFILE -> {
                    var item = data?.getStringArrayExtra("images")
                    var name = data?.getStringArrayExtra("displayname")

                    for (i in 0..(item!!.size - 1)) {
                        val str = item[i]

                        pimgPaths.add(str)


                        val add_file = Utils.getImage(context.contentResolver, str, 15)

                        if (images?.size == 0) {

                            images?.add(add_file)

                        } else {
                            try {
                                images?.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images?.add(add_file)
                            }

                        }

                    }

                    strPaths.clear()
                    for (i in 0..(name!!.size - 1)) {
                        val str = name[i]

                        if (strPaths != null) {
                            strPaths.add(str)

                            Log.d("yjs", "display " + strPaths.get(0))
                            Log.d("yjs", "display " + strPaths.get(0))
                        } else {
                            strPaths.add(str)
                            Log.d("yjs", "display " + strPaths.get(0))
                        }

                    }

                    var intent = Intent()

                    setResult(RESULT_OK, intent)

                    txPhotoCnt.text = images.size.toString()

                }
            }
        }


    }
}
