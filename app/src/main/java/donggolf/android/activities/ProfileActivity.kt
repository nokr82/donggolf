package donggolf.android.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.*
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.R
import donggolf.android.actions.ProfileAction
import donggolf.android.base.FirebaseFirestoreUtils.Companion.db
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : RootActivity() {

    lateinit var context: Context
    val SELECT_PROFILE = 104
    private var pimgPaths: ArrayList<String> = ArrayList<String>()
    private var images: ArrayList<Bitmap> = ArrayList()
    private var strPaths: ArrayList<String> = ArrayList<String>()

    private var mAuth: FirebaseAuth? = null

    var wimgl : String = ""
    var wimgs : String = ""
    var wlast : Long = 0
    var wnick : String = ""
    var wsex : String = ""
    var wTags : ArrayList<String> = ArrayList<String>()
    var wstmsg : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        context = this

        var wid = intent.getStringExtra("writerID")
        println("wid============$wid")

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()

        /*ProfileAction.viewContent(wid){ success, data, exception ->
            if (success){
                wimgl = data!!.get("imgl") as String
                wimgs = data!!.get("imgs") as String
                wlast = data!!.get("last") as Long
                wnick = data!!.get("nick") as String
                wsex = data!!.get("sex") as String
                wTags = data!!.get("sharpTag") as ArrayList<String>
                wstmsg = data!!.get("state_msg") as String
            }
        }*/

        txUserName.setText(wnick)
        statusMessage.setText(wstmsg)

        click_chat.setOnClickListener {
            btn_frd_cc1.setBackgroundColor(R.drawable.btn_frd_cancel)
        }

        click_post.setOnClickListener {
            btn_frd_cc2.setBackgroundColor(R.drawable.btn_frd_cancel)
        }

        click_friend.setOnClickListener {
            btn_frd_cc3.setBackgroundColor(R.drawable.btn_frd_cancel)
        }

        /*showProfImg.setOnClickListener {
            var intent = Intent(context, FindPictureGridActivity::class.java)
            startActivityForResult(intent, SELECT_PROFILE)
        }*/
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
