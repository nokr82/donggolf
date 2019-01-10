package donggolf.android.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Adapter
import donggolf.android.R
import donggolf.android.adapters.DlgRegionAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_add_dong_chat.*
import kotlinx.android.synthetic.main.dlg_select_chat_region.*
import kotlinx.android.synthetic.main.dlg_select_chat_region.view.*
import java.util.*

class AddDongChatActivity : RootActivity() {

    lateinit var context: Context

    var regionList1 = ArrayList<Map<String,Boolean>>()
    var regionList2 = ArrayList<Map<String,Boolean>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dong_chat)

        context = this

        var tmp = HashMap<String,Boolean>()
        tmp.put("서울", false)
        regionList1.add(tmp)
        tmp.put("강남구", false)
        regionList2.add(tmp)

        region1TV.setOnClickListener {
            /*val dialogView = layoutInflater.inflate(R.layout.dlg_select_chat_region, null)
            val dlgAdapter = DlgRegionAdapter(context, R.layout.item_right_radio_btn_list, regionList1)
            dialogView.dlg_region_LV.adapter = dlgAdapter
            val builder = AlertDialog.Builder(this)
                    .setView(dialogView)

            val alert = builder.show()

            dialogView.btn_regionOK.setOnClickListener {
                var selecteditem = dlg_region_LV.selectedItem.toString()
                println("........................................$selecteditem")
                alert.dismiss()
            }

            dialogView.btn_dlg_dismiss.setOnClickListener {
                alert.dismiss()
            }*/

        }


        //region2TV
    }
}
