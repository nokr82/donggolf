package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.squareup.okhttp.internal.Util
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MateAction
import donggolf.android.adapters.FriendCategoryAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_friend_manage.*
import kotlinx.android.synthetic.main.dialog_add_category.view.*
import org.json.JSONObject

class FriendManageActivity : RootActivity() {

    //adapter랑 dataList
    lateinit var frdMngAdapter : FriendCategoryAdapter
    var friendCategoryData = ArrayList<JSONObject>()
    lateinit var context : Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_manage)

        context = this

        frdMngAdapter = FriendCategoryAdapter(context, R.layout.item_friend_category_list, friendCategoryData)
        friendCategoryLV.adapter = frdMngAdapter

        getCategoryList()

        friendCategoryLV.setOnItemClickListener { parent, view, position, id ->
            val itt = Intent(context, FriendCategoryDetailActivity::class.java)
            itt.putExtra("groupTitle", friendCategoryData.get(position).getString("title"))
            startActivity(itt)
        }

        reqFriendLL.setOnClickListener {
            val intent = Intent(context, RequestFriendActivity::class.java)
            intent.putExtra("type","waiting")
            startActivity(intent)
        }

        blockListLL.setOnClickListener {
            val intent = Intent(context, FriendCategoryDetailActivity::class.java)
            intent.putExtra("type","block")
            startActivity(intent)
        }

        friendCategoryLV.setOnItemClickListener { parent, view, position, id ->
            val data = friendCategoryData.get(position)
            val category = data.getJSONObject("MateCategory")

            val intent = Intent(context, FriendCategoryDetailActivity::class.java)
            intent.putExtra("category_id", Utils.getInt(category,"id"))
            intent.putExtra("category_title", Utils.getString(category,"category"))
            println(Utils.getInt(category,"id"))
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
                        addMateCategory(Utils.getString(dialogView.categoryTitleET))
                        /*val jsonobject = null as JSONObject
                        jsonobject.put("category", Utils.getString(dialogView.categoryTitleET))
                        jsonobject.put("new_post","y")
                        jsonobject.put("chat", "y")
                        jsonobject.put("login","y")
                        jsonobject.put("open_mate","y")
                        friendCategoryData.add(jsonobject)
                        frdMngAdapter.notifyDataSetChanged()*/
                    }
                    .show()
            //val alert = builder.show() //builder를 끄기 위해서는 alertDialog에 이식해줘야 함

            dialogView.btn_title_clear.setOnClickListener {
                dialogView.categoryTitleET.setText("")
            }

        }

    }

    fun getCategoryList() {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        MateAction.getCategoryInfo(params,object :JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok") {
                    val categories = response.getJSONArray("categories")
                    friendCategoryData.clear()
                    for (i in 0 until categories.length()) {
                        friendCategoryData.add(categories[i] as JSONObject)
                    }
                    frdMngAdapter.notifyDataSetChanged()
                    val waitcount = response!!.getString("waitcount")
                    friend_request_cnt.setText(waitcount)
                    val blockcount = response!!.getString("blockcount")
                    friend_block_cnt.setText(blockcount)
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }
        })
    }

    fun addMateCategory(category_name : String) {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("category", category_name)

        MateAction.addCategory(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok") {
                    getCategoryList()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }
        })
    }


}
