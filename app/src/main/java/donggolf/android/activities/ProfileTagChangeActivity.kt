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
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.actions.MemberAction
import donggolf.android.adapters.ProfileTagAdapter
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.tag.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream


class ProfileTagChangeActivity : RootActivity() {

    var tag: String? = ""

    private lateinit var context: Context

    internal lateinit var adapter: ProfileTagAdapter

    private var adapterData : ArrayList<String> = ArrayList<String>()
    private var delList = ArrayList<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_tag_change)

        context = this

        finishtagLL.setOnClickListener {
            finish()
        }

        var intent = getIntent()

        adapter = ProfileTagAdapter(context,R.layout.tag,adapterData)

        tagList.adapter = adapter


        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val data = response.getJSONArray("MemberTags")
                        for (i in 0..data.length()-1) {
                            val json = data[i] as JSONObject
                            val member_tag = json.getJSONObject("MemberTag")
                            val tag = Utils.getString(member_tag, "tag")

                            adapterData.add(tag)
                        }

                        adapter.notifyDataSetChanged()

                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

            }
        })

        tagList.setOnItemClickListener { parent, view, position, id ->
            view.removeIV.setOnClickListener {

                val taglist = adapterData.get(position)
//                delList.add(oldTagID)

                adapter.removeItem(position)
                adapterData.remove(taglist)

            }
        }

        confirmRL.setOnClickListener {

            Utils.hideKeyboard(context!!)

            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context,"member_id")) //where절에 들어갈 조건
//            params.put("update", adapterData)//추가할거
            if (adapterData != null){
                Log.d("작성",adapterData.toString())
                if (adapterData!!.size != 0){
                    for (i in 0..adapterData!!.size - 1){

                        params.put("update[" + i + "]",adapterData.get(i))
                    }
                }
            }
            //params.put("del_tag", delList) //지울거
            params.put("type", "tags") //태그를 건드릴것이다

            MemberAction.update_info(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    setResult(RESULT_OK,intent)

                    finish()
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                }
            })


            if (intent.getStringExtra("type") != null){
                val type = intent.getStringExtra("type")
                println("type $type")
                if (type == "post"){
                    if (adapterData.size > 0 ){
                        intent.putExtra("data",adapterData)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {

                }
            }

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

                    //sTag.add(tag!!)

                    adapter.notifyDataSetChanged()

                    hashtagET.setText("")
                }
            }

            return@setOnEditorActionListener true
        }

        clearIV.setOnClickListener {
            hashtagET.setText("")
        }

        /*confirmRL.setOnClickListener {
            var intent = Intent();
            intent.putExtra("data",adapterData)
            if(adapterData.size > 0 && !adapterData.get(0).equals("")){
                setResult(Activity.RESULT_OK, intent)
                finish()
            }else {
                Toast.makeText(context, "태그를 입력해 주세요.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            intent.putExtra("data",adapterData)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }*/

    }
}
