package donggolf.android.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_my_post_mng.*

class MyPostMngActivity : RootActivity() {

    var uid : String = ""

    //adapter 3개 연결


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_post_mng)

        uid = intent.getStringExtra("user")

        btn_back.setOnClickListener {
            finish()
        }

        setTabInit()

        myPostTab.setOnClickListener {
            //뷰
            setTabInit()
            myPost_myPostTV.setTextColor(Color.parseColor("#0EDA2F"))
            myPost_myPost_view.visibility = View.VISIBLE
            myPostLV.visibility = View.VISIBLE
        }

        myCommentTab.setOnClickListener {
            //뷰
            setTabInit()
            myPost_commentTV.setTextColor(Color.parseColor("#0EDA2F"))
            myPost_commentPost_view.visibility = View.VISIBLE
            myCommentLV.visibility = View.VISIBLE
        }

        myStorePostTab.setOnClickListener {
            //뷰
            setTabInit()
            myPost_storeTV.setTextColor(Color.parseColor("#0EDA2F"))
            myPost_storePost_view.visibility = View.VISIBLE
            myStorePostLV.visibility = View.VISIBLE
        }
    }

    fun setTabInit() {
        myPost_myPostTV.setTextColor(Color.parseColor("#000000"))
        myPost_commentTV.setTextColor(Color.parseColor("#000000"))
        myPost_storeTV.setTextColor(Color.parseColor("#000000"))

        myPost_myPost_view.visibility = View.INVISIBLE
        myPost_commentPost_view.visibility = View.INVISIBLE
        myPost_storePost_view.visibility = View.INVISIBLE

        myPostLV.visibility = View.GONE
        myCommentLV.visibility = View.GONE
        myStorePostLV.visibility = View.GONE
    }

    /*class MyPostPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

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
    }*/
}
