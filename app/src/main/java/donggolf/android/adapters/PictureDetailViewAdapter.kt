package kr.co.hamel.android.adapter


import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList


/**
 * Created by dev1 on 2017-01-13.
 */

class PictureDetailViewAdapter : PagerAdapter() {
    // private Context context;
    // private ArrayList<UserFile> data = new ArrayList<UserFile>();
    private val views = ArrayList<View>()

    /*
    public PictureDetailViewAdapter(Context context, ArrayList<UserFile> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {

        UserFile uf = data.get(position);

        ImageView iv = new ImageView(this.context);

        String url = Config.url + uf.getImage_uri();
        ImageLoader.getInstance().displayImage(url, iv, Utils.UILoptionsProfile);

        collection.addView(iv);

        return iv;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
    */

    override fun getItemPosition(`object`: Any): Int {
        val index = views.indexOf(`object`)
        return if (index == -1)
            PagerAdapter.POSITION_NONE
        else
            index
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = views[position]
        container.addView(v)
        return v
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(views[position])
    }

    override fun getCount(): Int {
        return views.size

    }


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    @JvmOverloads
    fun addView(v: View, position: Int = views.size): Int {
        views.add(position, v)
        return position
    }


    fun removeView(pager: ViewPager, v: View): Int {
        return removeView(pager, views.indexOf(v))
    }


    fun removeView(pager: ViewPager, position: Int): Int {

        pager.adapter = null
        views.removeAt(position)
        pager.adapter = this

        return position
    }


    fun getView(position: Int): View {
        return views[position]
    }

}