package donggolf.android.adapters

import android.app.Activity
import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.nostra13.universalimageloader.core.ImageLoader
import donggolf.android.R
import donggolf.android.base.Utils
import java.util.*

class ProfileSlideViewAdapter(activity: Activity, imagePaths: ArrayList<String>, selected : LinkedList<String>) : PagerAdapter() {

    private val _activity: Activity = activity
    private val _imagePaths: ArrayList<String> = imagePaths
    private lateinit var inflater: LayoutInflater


    override fun getCount(): Int {
        return this._imagePaths.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as RelativeLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imgDisplay: ImageView

        inflater = _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container, false)

        imgDisplay = viewLayout.findViewById(R.id.imgDisplay)
        imgDisplay.scaleType = ImageView.ScaleType.FIT_CENTER

        ImageLoader.getInstance().displayImage(_imagePaths.get(position), imgDisplay, Utils.UILoptionsProfile)

        (container as ViewPager).addView(viewLayout)

        return viewLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as RelativeLayout)

    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}