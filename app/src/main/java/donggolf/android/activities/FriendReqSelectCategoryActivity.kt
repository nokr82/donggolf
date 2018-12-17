package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import donggolf.android.R
import donggolf.android.adapters.FriendCategoryAdapter
import donggolf.android.base.RootActivity
import donggolf.android.models.FriendCategory
import kotlinx.android.synthetic.main.activity_friend_req_select_category.*

class FriendReqSelectCategoryActivity : RootActivity() {

    lateinit var context: Context
    lateinit var selCategAdapter : FriendCategoryAdapter
    var categoryList : ArrayList<FriendCategory> = ArrayList<FriendCategory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_req_select_category)

        context = this
        var tmp = FriendCategory("1촌 골퍼", "45", false, true, false, true)
        categoryList.add(tmp)
        selCategAdapter = FriendCategoryAdapter(context, R.layout.item_friend_category_list, categoryList)
        selectCategoryLV.adapter = selCategAdapter

        selectCategoryLV.setOnItemClickListener { parent, view, position, id ->
            
        }
    }
}
