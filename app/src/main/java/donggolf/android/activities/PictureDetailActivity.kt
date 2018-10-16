package donggolf.android.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader
import donggolf.android.R
import donggolf.android.base.RootActivity
import donggolf.android.fragment.Fragment1
import donggolf.android.fragment.Fragment2
import donggolf.android.fragment.Fragment3
import kotlinx.android.synthetic.main.activity_picture_detail.*

class PictureDetailActivity : FragmentActivity() {


    internal lateinit var pagerAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_detail)

        pagerAdapter = PagerAdapter(getSupportFragmentManager())
        viewpager.adapter = pagerAdapter
        pagerAdapter.notifyDataSetChanged()

        /*val iv = ImageView(context)

        val url = Config.url + tr.getImage_uri()
        ImageLoader.getInstance().displayImage(url, iv, Utils.UILoptions)

        if (insertFirstIndex) {
            pagerAdapter.addView(iv, i)
        } else {
            pagerAdapter.addView(iv)
        }
        */
        btn_finish.setOnClickListener {
            finish()
        }

    }

    class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(i: Int): Fragment {
            val fragment: Fragment
            val args = Bundle()
            when (i) {
                0 -> {
                    fragment = Fragment1()
                    fragment.arguments = args
                    return fragment
                }
                1 -> {
                    fragment = Fragment2()
                    fragment.arguments = args
                    return fragment
                }
                else -> {
                    fragment = Fragment3()
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

        override fun destroyItem(viewPager: ViewGroup, position: Int, `object`: Any) {
            //
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

    }

}
