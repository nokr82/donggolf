package donggolf.android.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.internal.InternalTokenResult
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.ProfileAction
import donggolf.android.activities.*
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.FirebaseFirestoreUtils.Companion.db
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.Content
import donggolf.android.models.Photo
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_findid.*
import kotlinx.android.synthetic.main.activity_mod_status_msg.*
import kotlinx.android.synthetic.main.activity_profile_manage.*
import java.lang.Exception

class InfoFragment : Fragment(){

    var ctx: Context? = null

    lateinit var txUserName: TextView
    lateinit var  txUserRegion: TextView
    lateinit var messageTV:LinearLayout
    lateinit var hashtagTV:TextView
    lateinit var chatcountTV:TextView
    lateinit var postcountTV:TextView
    lateinit var friendcountTV:TextView
    lateinit var tv_CONSEQUENCES:LinearLayout
    lateinit var addProfImg:ImageView

    private var mAuth: FirebaseAuth? = null

    val SELECT_PROFILE = 104
    val SELECT_STATUS = 105
    val MODIFY_NAME = 106
    val MODIFY_TAG = 107
    val REGION_CHANGE = 108
    private var pimgPaths: ArrayList<String> = ArrayList<String>()
    private var images: ArrayList<ByteArray> = ArrayList()
    private var smimages: ArrayList<ByteArray> = ArrayList()
    private var strPaths: ArrayList<String> = ArrayList<String>()

    lateinit var db : FirebaseFirestore

    lateinit var imgl : ArrayList<ByteArray>
    lateinit var imgs : ArrayList<ByteArray>
    var lastN : Long = 0
    lateinit var nick : String
    lateinit var sex : String
    lateinit var sTag : ArrayList<String>
    lateinit var statusMessage : String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)

        val ctx = context
        if (null != ctx) {
            doSomethingWithContext(ctx)
        }

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()
        db = FirebaseFirestore.getInstance()

        println("currentUser======$currentUser")

        return inflater.inflate(R.layout.activity_profile_manage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //탭 호스트 글자색 변경같이 눌렀을 때 변경되는 것
        txUserName = view.findViewById(R.id.txUserName)
        txUserRegion = view.findViewById(R.id.txUserRegion)
        messageTV = view.findViewById(R.id.messageTV)
        hashtagTV = view.findViewById(R.id.hashtagTV)
        chatcountTV = view.findViewById(R.id.chatcountTV)
        postcountTV = view.findViewById(R.id.postcountTV)
        friendcountTV = view.findViewById(R.id.friendcountTV)
        tv_CONSEQUENCES = view.findViewById(R.id.tv_CONSEQUENCES)
        addProfImg = view.findViewById(R.id.addProfImg)

        val nick: String = PrefUtils.getStringPreference(context,"nick")

        txUserName.setText(nick)

        tv_CONSEQUENCES.setOnClickListener {
            var intent: Intent = Intent(activity, OtherManageActivity::class.java)
            startActivity(intent)
        }

        addProfImg.setOnClickListener {
            //var intent = Intent(activity, FindPictureGridActivity::class.java)
            var intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, SELECT_PROFILE)
        }

        imgProfile.setOnClickListener {
            var intent = Intent(activity, ViewProfileListActivity::class.java)
            intent.putExtra("album", images)
            startActivity(intent)
        }

        messageTV.setOnClickListener {
            var intent = Intent(activity, ModStatusMsgActivity::class.java)
            startActivityForResult(intent, SELECT_STATUS)
        }

        myNeighbor.setOnClickListener {
            var intent = Intent(activity, MutualActivity::class.java)
            startActivity(intent)
        }

        btnNameModi.setOnClickListener {
            var intent = Intent(activity, ProfileNameModifActivity::class.java)
            startActivityForResult(intent, MODIFY_NAME)
        }

        tv_CONSEQUENCES.setOnClickListener {
            var itt = Intent(activity, OtherManageActivity::class.java)
            startActivity(itt)
        }

        prfhashtagLL.setOnClickListener {
            var intent = Intent(activity, ProfileTagChangeActivity::class.java)
            startActivityForResult(intent, MODIFY_TAG)
        }

        myNeighbor.setOnClickListener {
            var intent = Intent(activity, MutualActivity::class.java)
            startActivity(intent)
        }

        setRegion.setOnClickListener {
            var intent = Intent(activity, AreaRangeActivity::class.java)
            startActivityForResult(intent, REGION_CHANGE)
        }

    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SELECT_PROFILE -> {
                    /*var item = data?.getStringArrayExtra("images")
                    var name = data?.getStringArrayExtra("displayname")

                    for (i in 0..(item!!.size - 1)) {
                        val str = item[i]

                        pimgPaths.add(str)


                        val add_file = Utils.getImage(context?.contentResolver, str, 15)

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

                    activity?.setResult(Activity.RESULT_OK, intent)

                    mngTXPhotoCnt.text = images.size.toString()*/
                    var uri = data?.data
                    try {

                        var bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)

                        var bo = BitmapFactory.Options()
                        bo.inSampleSize = 4
                        var tmpImg = BitmapFactory.decodeFile(context?.contentResolver.toString(), bo)

                        var smallBmImg = Bitmap.createScaledBitmap(tmpImg, 50, 50, true)
                        var resizedimg = Utils.getByteArray(smallBmImg)
//                        images.add(bitmap)
                        //bitmap image to byteArray image
                        var bytearray_ = Utils.getByteArray(bitmap)

                        images.add(bytearray_)
                        smimages.add(resizedimg)

                        val nowTime = System.currentTimeMillis()

                        var imgPaths : String
                        var imagsPaths : String
                        var imgpath : String
                        var photo = Photo()

                        var image_path = "imgl/" + nowTime + ".png"
                        var images_path = "imgs/" + nowTime + ".png"

                        photo.type = "photo"
//                        photo.file = imgpath

                        var uid = PrefUtils.getStringPreference(context, "uid")

                        ProfileAction.viewContent(uid) { success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                            statusMessage = data!!.get("state_msg") as String

                            imgl = data!!.get("imgl") as ArrayList<ByteArray>
                            imgs = data!!.get("imgs") as ArrayList<ByteArray>
                            lastN = data!!.get("last") as Long
                            nick = data!!.get("nick") as String
                            sex = data!!.get("sex") as String
                            sTag = data!!.get("sharpTag") as ArrayList<String>

                        }
                        imgl = images
                        imgs = smimages

                        //이미지 firebase로 전송
                        //사실 그냥 uri를 putFile해도 됨
                        /*
                            UploadTask uploadTask;
                            uploadTask = storageRef.putFile(file);
                        */
                        //근데 여러 사진 보내야되니까
//                        val item = Users(images, smimages, lastN, nick, sex, sTag, statusMessage)

//                        FirebaseFirestoreUtils.save("users", uid, item) {
//                            if (it) {
//
//                            } else {
//
//                            }
//                        }


                        //imgProfile.setImageBitmap(bitmap)
                        imgProfile.background = ShapeDrawable(OvalShape())
                        imgProfile.clipToOutline = true


                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                    mngTXPhotoCnt.setText(images.size.toString())

                }
                SELECT_STATUS -> {
                    var sttsMsg = data?.getStringExtra("status_message")
                    infoStatusMsg.setText(sttsMsg)
                }
                MODIFY_NAME -> {
                    var newNick = data?.getStringExtra("newNick")
                    txUserName.setText(newNick)
                }
                MODIFY_TAG -> {
                    var newTag = data?.getStringArrayExtra("newTags")
                    var tmp :String = ""

                    for (i in 0..(newTag!!.size-1)){

                        tmp += newTag.get(i) + " "
                    }
                    hashtagTV.setText(tmp)
                }
                REGION_CHANGE -> {
                    var newRG1 = data!!.getStringExtra("RG1")
                    var newRG2 = data!!.getStringExtra("RG2")
                    var newRG3 = data!!.getStringExtra("RG3")

                    txUserRegion.setText(newRG1+","+newRG2+","+newRG3)
                }
            }
        }

    }

    fun imageTransmission() {

    }


    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
    }



}