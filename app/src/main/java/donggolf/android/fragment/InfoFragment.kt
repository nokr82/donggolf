package donggolf.android.fragment

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.R
import donggolf.android.activities.AreaRangeActivity
import donggolf.android.activities.OtherManageActivity
import donggolf.android.adapters.MainAdapter
import donggolf.android.base.PrefUtils

class InfoFragment : Fragment(){

    var ctx: Context? = null

    lateinit var txUserName: TextView
    lateinit var  txUserRegion: TextView
    lateinit var messageTV:TextView
    lateinit var hashtagTV:TextView
    lateinit var chatcountTV:TextView
    lateinit var postcountTV:TextView
    lateinit var friendcountTV:TextView
    lateinit var tv_CONSEQUENCES:LinearLayout

    private var mAuth: FirebaseAuth? = null



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

        val nick: String = PrefUtils.getStringPreference(context,"nick")

        txUserName.setText(nick)

        tv_CONSEQUENCES.setOnClickListener {
            var intent: Intent = Intent(activity, OtherManageActivity::class.java)
            startActivity(intent)
        }



    }


    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
    }



}