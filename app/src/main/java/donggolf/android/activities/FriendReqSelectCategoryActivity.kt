package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MateAction
import donggolf.android.adapters.FriendCategoryAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_friend_req_select_category.*
import kotlinx.android.synthetic.main.dialog_add_category.view.*
import kotlinx.android.synthetic.main.item_friend_category_list.view.*
import org.json.JSONObject

class FriendReqSelectCategoryActivity : RootActivity() {

    lateinit var context: Context
    lateinit var selCategAdapter : FriendCategoryAdapter
    var categoryList = ArrayList<JSONObject>()

    var mates_id = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_req_select_category)

        context = this
        val intent = getIntent()
        mates_id = intent.getIntExtra("mates_id", -1)
        //Log.d("2아뒤",mates_id.toString())
        selCategAdapter = FriendCategoryAdapter(context, R.layout.item_friend_category_list, categoryList)
        selectCategoryLV.adapter = selCategAdapter

        btn_addCategory.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null) //사용자 정의 다이얼로그 xml 붙이기
            builder.setView(dialogView)
            val alert = builder.show()
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
            dialogView.btn_title_clear.setOnClickListener {
                alert.dismiss()
            }
            dialogView.dlg_addchattingTV.setOnClickListener {
                var category = Utils.getString(dialogView.categoryTitleET)
                if (category == "" || category == null) {
                    Toast.makeText(context, "빈칸은 입력하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                addMateCategory(Utils.getString(dialogView.categoryTitleET))
                alert.dismiss()
            }
        }

        selectCategoryLV.setOnItemClickListener { parent, view, position, id ->
            var data = categoryList.get(position)
            val category = data.getJSONObject("MateCategory")
            val category_id = Utils.getString(category,"id")
            view.category_del_LL.setOnClickListener {
                if (category_id == "-1"){
                    Toast.makeText(context,"일촌골퍼는 삭제하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val builder = AlertDialog.Builder(context)
                builder.setMessage("정말로 이 카테고리를 삭제 하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            delete_category(Utils.getString(category,"id"))
                            categoryList.removeAt(position)
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }

            if (mates_id!=-1){
                acceptMates(category_id,mates_id)

            }else{
                val get_category_id = intent.getStringExtra("category_id")
                val mate_id = intent.getSerializableExtra("mate_id")
                println("----mate_id : $mate_id")
                var intent = Intent()
                intent.putExtra("mate_id",mate_id)
                intent.putExtra("category_id",category_id)
                intent.putExtra("get_category_id",get_category_id)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }




        }

        finishLL.setOnClickListener {
            finish()
            Utils.hideKeyboard(this)
        }

        getCategoryList()

    }

    fun getCategoryList(){
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        MateAction.getCategoryInfo(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    categoryList.clear()
                    val categories = response.getJSONArray("categories")
                    for (i in 0..categories.length()-1 ) {
                        categoryList.add(categories[i] as JSONObject)
                    }

                    selCategAdapter.notifyDataSetChanged()

                /*    if (categoryList.size == 1){
                        val builder = AlertDialog.Builder(context)
                        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null) //사용자 정의 다이얼로그 xml 붙이기
                        builder.setView(dialogView)
                        val alert = builder.show()
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
                        dialogView.btn_title_clear.setOnClickListener {
                            alert.dismiss()
                        }
                        dialogView.dlg_addchattingTV.setOnClickListener {
                            var category = Utils.getString(dialogView.categoryTitleET)
                            if (category == "" || category == null) {
                                Toast.makeText(context, "빈칸은 입력하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            addMateCategory(Utils.getString(dialogView.categoryTitleET))
                            alert.dismiss()
                        }
                    }*/
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }

            override fun onFinish() {

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

                    var intent = Intent()
                    intent.action = "RESET_CATEGORY"
                    sendBroadcast(intent)
                    setResult(RESULT_OK, intent);

                } else if (result == "already"){
                    Toast.makeText(context,"같은 이름의 카테고리가 있습니다.", Toast.LENGTH_SHORT).show()
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

    fun acceptMates(category_id: String,member_id:Int) {

        val params = RequestParams()

        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))

        params.put("mate_id",  member_id)
        params.put("category_id", category_id)
        params.put("status", "m")

        MateAction.accept_mates(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                //Log.d("결과",response.toString())
                Toast.makeText(context,"친구추가되었습니다.", Toast.LENGTH_SHORT).show()
                var intent = Intent()
                intent.action = "ADD_FRIEND"
                sendBroadcast(intent)

                finish()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }
        })
    }

    fun delete_category(id : String){
        val params = RequestParams()
        params.put("id", id)

        MateAction.delete_category(params, object : JsonHttpResponseHandler(){
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
