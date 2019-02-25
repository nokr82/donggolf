package donggolf.android.adapters

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import donggolf.android.R
import donggolf.android.base.Config
import donggolf.android.base.Utils
import org.json.JSONObject
import java.util.*

class ViewAlbumAdapter(context: Context, view:Int, data:ArrayList<JSONObject>, selected : LinkedList<String>) : ArrayAdapter<JSONObject>(context,view, data) {
    private lateinit var item: ViewHolder
    var view:Int = view
    var data: java.util.ArrayList<JSONObject> = data
    private val selected: LinkedList<String> = selected

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        lateinit var retView: View

        if (convertView == null) {
            retView = View.inflate(context, view, null)
            item = ViewAlbumAdapter.ViewHolder(retView)
            retView.tag = item
        } else {
            retView = convertView
            item = convertView.tag as ViewAlbumAdapter.ViewHolder
            if (item == null) {
                retView = View.inflate(context, view, null)
                item = ViewAlbumAdapter.ViewHolder(retView)
                retView.tag = item
            }
        }

        var json = data.get(position)
        val memberImg = json.getJSONObject("MemberImg")

        json.put("image_id", Utils.getString(memberImg,"id"))
        json.put("image_uri", Utils.getString(memberImg,"image_uri"))
        var editMode= json.getBoolean("editMode")

        if (editMode) {

            item.picture_grid_click.visibility = View.VISIBLE
            var duplicateClicks= json.getBoolean("duplicateClicks")

            if (duplicateClicks) {

                //activity에서 연산하고 put -> setting
                var img_cnt = json.getInt("select_album_img_cnt")
                if (img_cnt != 0) {
                    item.picture_grid_click.text = img_cnt.toString()
                }
            } else {
                item.picture_grid_click.text = ""
            }

        } else {

            item.picture_grid_click.visibility = View.GONE

        }

        var img_uri = Utils.getString(memberImg,"small_uri")
        val image = Config.url + img_uri
        ImageLoader.getInstance().displayImage(image, item.picture_grid_image, Utils.UILoptionsProfile)

        if (selected.contains(position.toString())) {
            val idx = selected.indexOf(position.toString())
            item.picture_grid_click.text = (idx + 1).toString()
        }else {
            item.picture_grid_click.text = ""
        }

        return retView
    }

    fun addItem(newImg : JSONObject){

        data.add(0, newImg)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): JSONObject {
        return data.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return data.count()
    }

    /*fun removeItem(position: Int){
        data.removeAt(position)
        notifyDataSetChanged()
    }*/

    class ViewHolder(v: View) {

        var picture_grid_image : ImageView
        var picture_grid_click : TextView

        init {

            picture_grid_image = v.findViewById(R.id.picture_grid_image)
            picture_grid_click = v.findViewById(R.id.picture_grid_click)
        }


    }
}