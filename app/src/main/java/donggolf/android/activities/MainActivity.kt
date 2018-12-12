package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.adapters.MainAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.fragment.ChatFragment
import donggolf.android.fragment.FreeFragment
import donggolf.android.fragment.FushFragment
import donggolf.android.fragment.InfoFragment
import donggolf.android.models.Content
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : FragmentActivity() {//fragment 를 쓰려면 fragmentActivity()를 extends

    private lateinit var context: Context


    private val SELECT_PICTURE: Int = 101

    val user = HashMap<String, Any>()

    internal lateinit var pagerAdapter: PagerAdapter

    companion object {
        const val TAG = "MainActivity"
    }

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//
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



        context = this



        homeRL.setOnClickListener {
            frags.currentItem = 0
        }

        chatRL.setOnClickListener {
            frags.currentItem = 1
        }

        noticeRV.setOnClickListener {
            frags.currentItem = 2
        }

        infoRL.setOnClickListener {
            frags.currentItem = 3
        }

        areaLL.setOnClickListener {
            MoveAreaRangeActivity()
        }

        marketIV.setOnClickListener {
            MoveMarketMainActivity()
        }

        friendsLL.setOnClickListener {
            var intent = Intent(context, FriendSearchActivity::class.java)
            intent.putExtra("tUser", user)
            startActivity(intent)
        }

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
                2 -> {
                    fragment = FushFragment()
                    fragment.arguments = args
                    return fragment
                }
                else -> {
                    fragment = InfoFragment()
                    fragment.arguments = args
                    return fragment
                }
            }
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return ""
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }


}
