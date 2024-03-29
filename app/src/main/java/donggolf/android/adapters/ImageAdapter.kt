package donggolf.android.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import donggolf.android.R
import donggolf.android.base.ImageLoader
import java.util.*
import kotlin.collections.ArrayList


open class ImageAdapter(context: Context, data:ArrayList<PhotoData>, imageLoader: ImageLoader, selected : LinkedList<String>) : BaseAdapter() {

    private lateinit var item: ViewHolder
    var mContext:Context = context

    internal var photoList: ArrayList<PhotoData> = data

    private val imageLoader: ImageLoader = imageLoader

    private val selected: LinkedList<String> = selected


    class PhotoData {
        var photoID: Int = 0
        var photoPath: String? = null
        var displayName: String? = null
        var bucketPhotoName: String? = null
        var orientation: Int = 0
    }


    override fun getView(position: Int, convertView: View?, parent : ViewGroup?): View {
        var holder: ViewHolder = ViewHolder();

        var retView: View

        if (convertView == null) {

            retView = View.inflate(this.mContext, R.layout.findpicture_gridview_item, null)
            holder.picture_grid_click = retView.findViewById<TextView>(R.id.picture_grid_click);
            holder.picture_grid_image = retView.findViewById<ImageView>(R.id.picture_grid_image);

            retView.setTag(holder);
        } else {
            retView = convertView
            holder = retView.getTag() as ViewHolder
        }

        var photo = photoList.get(position);

        holder.picture_grid_image.setImageBitmap(imageLoader.getImage(photo.photoID, photo.photoPath, photo.orientation))

        if (selected.contains(position.toString())) {
            val idx = selected.indexOf(position.toString())
            holder.picture_grid_click.text = (idx + 1).toString()

            //Log.d("yjs" ,"idx : " + idx.toString()  )
        }else {
            holder.picture_grid_click.text = ""
        }

        return retView;
    }

    override fun getItem(position: Int): PhotoData {
        return photoList.get(position)
    }

    override fun getItemId(position: Int): Long {

        return position.toLong()
    }

    override fun getCount(): Int {

        return photoList.count()
    }

    fun removeItem(position: Int){
        photoList.removeAt(position)
    }

    inner class ViewHolder {
        lateinit var picture_grid_click :TextView
        lateinit var picture_grid_image : ImageView
    }

}