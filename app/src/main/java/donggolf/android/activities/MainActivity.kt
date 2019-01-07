package donggolf.android.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.iid.FirebaseInstanceId
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.MemberAction
import donggolf.android.adapters.MainAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import donggolf.android.fragment.ChatFragment
import donggolf.android.fragment.FreeFragment
import donggolf.android.fragment.FushFragment
import donggolf.android.fragment.InfoFragment
import donggolf.android.models.Content
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity : FragmentActivity() {//fragment 를 쓰려면 fragmentActivity()를 extends

    private lateinit var context: Context

    private var progressDialog: ProgressDialog? = null

    private val SELECT_PICTURE: Int = 101

    private val BACK_PRESSED_TERM:Long = 1000 * 2
    private var backPressedTime: Long = -1

    val user = HashMap<String, Any>()

    internal lateinit var pagerAdapter: PagerAdapter

    companion object {
        const val TAG = "MainActivity"
    }

    private var mAuth: FirebaseAuth? = null

    var is_push = false
    var market_id = -1
    var content_id = -1
    var friend_id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.context = this

        is_push = intent.getBooleanExtra("is_push", false)
        market_id = intent.getIntExtra("market_id", -1)
        content_id = intent.getIntExtra("content_id", -1)
        friend_id = intent.getIntExtra("friend_id", -1)

        if(is_push) {
            handlePush()
        }

        pagerAdapter = PagerAdapter(getSupportFragmentManager())
        frags.adapter = pagerAdapter
        pagerAdapter.notifyDataSetChanged()
        frags.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {

                when (position) {
                    0 -> {
                    }
                    1 -> {
                    }
                    2 -> {
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })




        homeRL.setOnClickListener {
            frags.currentItem = 0
        }

        chatRL.setOnClickListener {
            frags.currentItem = 1
        }

        noticeRV.setOnClickListener {
//            frags.currentItem = 2
            var intent = Intent(context, AlarmActivity::class.java)
            startActivity(intent)
        }

        infoRL.setOnClickListener {
            frags.currentItem = 2
        }

        areaLL.setOnClickListener {
            MoveAreaRangeActivity()
        }

        marketIV.setOnClickListener {
            MoveMarketMainActivity()
        }

        friendsLL.setOnClickListener {
            var intent = Intent(context, FriendSearchActivity::class.java)
            //intent.putExtra("tUser", user)
            startActivity(intent)
        }

        updateToken()

    }

    fun MoveAddPostActivity(){
        var intent = Intent(context, AddPostActivity::class.java);
        intent.putExtra("category",1)
        startActivityForResult(intent, SELECT_PICTURE);
    }

    fun MoveMainDetailActivity(id : String){
        var intent: Intent = Intent(this, MainDetailActivity::class.java)
        intent.putExtra("id",id)
        startActivity(intent)
    }

    fun MoveAreaRangeActivity(){
        var intent: Intent = Intent(this, AreaRangeActivity::class.java)
        intent.putExtra("region_type", "content_filter")
        startActivity(intent)
    }

    fun MoveMarketMainActivity(){
        var intent: Intent = Intent(this, MarketMainActivity::class.java)
        startActivity(intent)
    }


    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun updateUI(currentUser: FirebaseUser?) {
//        mAuth!!.signInWithCustomToken(mCustomToken)
//                .addOnCompleteListener(this) { task ->
//                    if (task.isSuccessful) {
//                        // Sign in success, update UI with the signed-in user's information
//                        Log.d(TAG, "signInWithCustomToken:success")
//                        val user = mAuth!!.getCurrentUser()
//                        updateUI(user)
//                    } else {
//                        // If sign in fails, display a message to the user.
//                        Log.w(TAG, "signInWithCustomToken:failure", task.exception)
//                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
//                        updateUI(null)
//                    }
//                }
    }

    class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(i: Int): Fragment {

            var fragment: Fragment

            val args = Bundle()
            when (i) {
                0 -> {
                    fragment = FreeFragment()
                    fragment.arguments = args

                    return fragment
                }
                1 -> {
                    fragment = ChatFragment()
                    fragment.arguments = args

                    return fragment
                }
//                2 -> {
//                    fragment = FushFragment()
//                    fragment.arguments = args
//                    return fragment
//                }
                else -> {
                    fragment = InfoFragment()
                    fragment.arguments = args
                    return fragment
                }
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return ""
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }

    private fun updateToken() {
        val params = RequestParams()
        val member_id = PrefUtils.getIntPreference(context, "member_id", -1)
        val member_token = FirebaseInstanceId.getInstance().token

        if (member_id == -1 || null == member_token || "" == member_token || member_token.length < 1) {
            return
        }
        params.put("member_id", member_id)
        params.put("token", member_token)
        params.put("device", Config.device)

        MemberAction.regist_token(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                try {
                    val result = response!!.getString("result")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONArray?) {
                super.onSuccess(statusCode, headers, response)
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseString: String?) {}

            private fun error() {

                if (progressDialog != null) {
                    Utils.alert(context, "조회중 장애가 발생하였습니다.")
                }
            }

            override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>?,
                    responseString: String?,
                    throwable: Throwable
            ) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

//                val member_id = PrefUtils.getIntPreference(context, "member_id")
//                LogAction.log(javaClass.toString(), member_id, responseString)

                throwable.printStackTrace()
                error()
            }

            override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>?,
                    throwable: Throwable,
                    errorResponse: JSONObject?
            ) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>?,
                    throwable: Throwable,
                    errorResponse: JSONArray?
            ) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onStart() {
                // show dialog
                if (progressDialog != null) {

                    progressDialog!!.show()
                }
            }

            override fun onFinish() {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

//        progressDialog!!.dismiss()
    }

    fun handlePush() {

        if(content_id > 0) {
            // 게시글 관련 푸쉬
            var intent = Intent(context, MainDetailActivity::class.java)
            intent.putExtra("id", content_id.toString())
            startActivity(intent)
        } else if (market_id > 0) {
            // 마켓 관련 푸쉬
            var intent = Intent(context, GoodsDetailActivity::class.java)
            intent.putExtra("product_id", market_id)
            startActivity(intent)
        } else if (friend_id > 0) {
            var intent = Intent(context, RequestFriendActivity::class.java)
            intent.putExtra("type","waiting")
            startActivity(intent)
        }

    }

    override fun onBackPressed() {

        if (System.currentTimeMillis() - backPressedTime < BACK_PRESSED_TERM) {
            finish()
        } else {
            Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
            backPressedTime = System.currentTimeMillis()
        }

    }

}
