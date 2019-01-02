package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import donggolf.android.R
import donggolf.android.adapters.SelectMemberAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_select_member.*
import org.json.JSONObject
import java.util.ArrayList

class SelectMemberActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  memberAdapter : SelectMemberAdapter
    private  var memberList: ArrayList<JSONObject> = ArrayList<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_member)

        context = this

        memberAdapter = SelectMemberAdapter(context,R.layout.item_select_member,memberList)

        selMemList.adapter = memberAdapter

    }



}
