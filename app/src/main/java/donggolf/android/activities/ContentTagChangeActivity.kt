package donggolf.android.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_profile_tag_change.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.kakao.kakaostory.StringSet.text
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.actions.MemberAction
import donggolf.android.actions.PostAction
import donggolf.android.adapters.ProfileTagAdapter
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.tag.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream


class ContentTagChangeActivity : RootActivity() {

    var tag: String? = ""

    private lateinit var context: Context

    internal lateinit var adapter: ProfileTagAdapter

    private var adapterData : ArrayList<String> = ArrayList<String>()
    private var delList = ArrayList<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_tag_change)

        context = this

        titleTV.text = "나의 게시글 #"




        finishtagLL.setOnClickListener {
            Utils.hideKeyboard(context!!)
            if (intent.getStringExtra("type") != null){
                val type = intent.getStringExtra("type")
                println("type $type")
                if (type == "post"){
                    if (adapterData.size > 0 ){
                        intent.putExtra("data",adapterData)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            } else {
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }


        adapter = ProfileTagAdapter(context,R.layout.tag,adapterData)
        tagList.adapter = adapter
        getPost()


        tagList.setOnItemClickListener { parent, view, position, id ->
            view.removeIV.setOnClickListener {

                val taglist = adapterData.get(position)

                adapter.removeItem(position)
                adapterData.remove(taglist)

            }
        }

        confirmRL.setOnClickListener {

        }

        //입력관련 처리
        hashtagET.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 입력되는 텍스트에 변화가 있을 때 호출된다.
            }

            override fun afterTextChanged(count: Editable) {
                // 입력이 끝났을 때 호출된다.
                countTV.setText(Integer.toString(hashtagET.text.toString().length))
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // 입력하기 전에 호출된다.
            }
        })

        //Enter key is pressed
        hashtagET.setOnEditorActionListener { v, actionId, event ->

            if(actionId == EditorInfo.IME_ACTION_DONE){

                tag = Utils.getString(hashtagET)

                if("" == tag || null == tag || tag!!.isEmpty()) {

                    Toast.makeText(context, "태그를 입력해주세요.", Toast.LENGTH_LONG).show()

                    return@setOnEditorActionListener false

                } else {

                    Utils.hideKeyboard(context!!)

                    adapterData.add(tag!!)


                    adapter.notifyDataSetChanged()

                    hashtagET.setText("")
                }
            }

            return@setOnEditorActionListener true
        }

        clearIV.setOnClickListener {
            hashtagET.setText("")
        }


    }


    fun getPost(){
        //게시글 불러오기
            val id = intent.getStringExtra("id")
            val login_id = PrefUtils.getIntPreference(context, "member_id")
            var params = RequestParams()
            params.put("id",id)
            params.put("member_id",login_id)

            PostAction.get_post(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    try {
                        val result = response!!.getString("result")
                        Log.d("태그",response.toString())

                        if (result == "ok") {

                            val tags = response.getJSONArray("tags")

                            if (tags != null && tags.length() > 0 ){
                                for (i in 0 until tags.length()){
                                    var json = tags.get(i) as JSONObject
                                    var MemberTags = json.getJSONObject("ContentsTags")
                                    val division = Utils.getString(MemberTags,"division")

                                    if (division == "1"){
                                        val tag = Utils.getString(MemberTags,"tag")
                                        adapterData.add(tag)
                                        adapter.notifyDataSetChanged()
                                    }
                                }
                            }

                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                }
            })

    }

    override fun onBackPressed() {
        Utils.hideKeyboard(context!!)
        if (intent.getStringExtra("type") != null){
            val type = intent.getStringExtra("type")
            println("type $type")
            if (type == "post"){
                if (adapterData.size > 0 ){
                    intent.putExtra("data",adapterData)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        } else {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

}
