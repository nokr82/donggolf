package donggolf.android.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.R
import donggolf.android.activities.FindPictureGridActivity
import donggolf.android.activities.ModStatusMsgActivity
import donggolf.android.activities.MutualActivity
import donggolf.android.activities.OtherManageActivity
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_profile_manage.*

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
    private var pimgPaths: ArrayList<String> = ArrayList<String>()
    private var images: ArrayList<Bitmap> = ArrayList()
    private var strPaths: ArrayList<String> = ArrayList<String>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)

        val ctx = context
        if (null != ctx) {
            doSomethingWithContext(ctx)
        }

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()
        val db = FirebaseFirestore.getInstance()

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
            var intent = Intent(activity, FindPictureGridActivity::class.java)
            startActivityForResult(intent, SELECT_PROFILE)
        }

        messageTV.setOnClickListener {
            var intent = Intent(activity, ModStatusMsgActivity::class.java)
            startActivity(intent)
        }

        myNeighbor.setOnClickListener {
            var intent = Intent(activity, MutualActivity::class.java)
            startActivity(intent)
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

                    mngTXPhotoCnt.text = images.size.toString()

                }
            }
        }

    }


    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
    }



}