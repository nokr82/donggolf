package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.loopj.android.http.RequestParams
import donggolf.android.R
import donggolf.android.adapters.FriendCategoryAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.models.FriendCategory
import kotlinx.android.synthetic.main.activity_friend_manage.*
import kotlinx.android.synthetic.main.dialog_add_category.view.*

class FriendManageActivity : RootActivity() {

    //adapter랑 dataList
    lateinit var frdMngAdapter : FriendCategoryAdapter
    var friendCategoryData : ArrayList<FriendCategory> = ArrayList<FriendCategory>()
    lateinit var context : Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_manage)

        context = this

        frdMngAdapter = FriendCategoryAdapter(context, R.layout.item_friend_category_list, friendCategoryData)
        friendCategoryLV.adapter = frdMngAdapter

        friendCategoryLV.setOnItemClickListener { parent, view, position, id ->
            val itt = Intent(context, FriendCategoryDetailActivity::class.java)
            itt.putExtra("groupTitle", friendCategoryData.get(position).cateTitle)
            startActivity(itt)
        }

        reqFriendLL.setOnClickListener {
            val intent = Intent(context, RequestFriendActivity::class.java)
            intent.putExtra("type","waiting")
            startActivity(intent)
        }

        btn_addCategory.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null) //사용자 정의 다이얼로그 xml 붙이기
            dialogView.categoryTitleET.addTextChangedListener(object : TextWatcher {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // 입력되는 텍스트에 변화가 있을 때 호출된다.
                }

                override fun afterTextChanged(count: Editable) {
                    // 입력이 끝났을 때 호출된다.

                    dialogView.leftWords.setText(Integer.toString(dialogView.categoryTitleET.text.toString().length))
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    // 입력하기 전에 호출된다.
                }
            })

            builder.setView(dialogView)
                    .setPositiveButton("확인") { dialog, id ->
                        //println("그룹 제목 ::: $title")
                        var tmp = FriendCategory("", "0", true, true, true)
                        tmp.cateTitle = dialogView.categoryTitleET.text.toString()
                        println("그룹 제목 ::: ${tmp.cateTitle}")
                        friendCategoryData.add(tmp)
                        frdMngAdapter.notifyDataSetChanged()
                    }
                    .show()
            //val alert = builder.show() //builder를 끄기 위해서는 alertDialog에 이식해줘야 함

            dialogView.btn_title_clear.setOnClickListener {
                dialogView.categoryTitleET.setText("")
            }

        }

    }

    fun getMatesList() {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))


    }

    fun addMateCategory() {
        val params = RequestParams()
    }
}
