package donggolf.android.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_profile_phone_change.*
import kotlinx.android.synthetic.main.activity_profile_tag_change.*
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import donggolf.android.adapters.ProfileTagAdapter
import donggolf.android.base.Utils


class ProfileTagChangeActivity : RootActivity() {

    var tag: String? = ""

    private lateinit var context: Context

    internal lateinit var adapter: ProfileTagAdapter

    private  var adapterData : ArrayList<String> = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_tag_change)

        context = this

        finishtagLL.setOnClickListener {
            finish()
        }

        adapter = ProfileTagAdapter(context,R.layout.tag,adapterData)

        tagList.adapter = adapter



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

        hashtagET.setOnEditorActionListener { v, actionId, event ->

            if(actionId == EditorInfo.IME_ACTION_DONE){

                tag = Utils.getString(hashtagET)

                if("".equals(tag) || null == tag || tag!!.length < 1){

                    Toast.makeText(context, "검색어를 입력해주세요.", Toast.LENGTH_LONG).show()

                    return@setOnEditorActionListener false

                }

                Utils.hideKeyboard(context!!)

                adapterData.add("#" + tag!!)

                adapter.notifyDataSetChanged()

                hashtagET.setText("")

            }




            return@setOnEditorActionListener true
        }

        clearIV.setOnClickListener {
            hashtagET.setText("")
        }

        confirmRL.setOnClickListener {
            var intent = Intent();
            intent.putExtra("data",adapterData)
            if(adapterData.size > 0 && !adapterData.get(0).equals("")){
                setResult(Activity.RESULT_OK, intent);
                finish()
            }else {
                Toast.makeText(context, "태그를 입력해 주세요.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            intent.putExtra("data",adapterData)
            setResult(Activity.RESULT_OK, intent);
            finish()
        }



    }
}
