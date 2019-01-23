package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_profile.*
import org.json.JSONException
import org.json.JSONObject

class ProfileActivity : RootActivity() {

    lateinit var context: Context
    var member_id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        context = this

        //상대 홈피
        val user_id = intent.getIntExtra("other_member_id",0)

        var intent = getIntent()
        member_id = intent.getStringExtra("member_id")
        member_info(member_id)


        //프로필 사진
        otherPrfImgIV.setOnClickListener {
            val intent = Intent(context,ViewProfileListActivity::class.java)
            intent.putExtra("viewAlbumUser", member_id.toInt())
            startActivity(intent)
        }

        profBack.setOnClickListener {
            finish()
        }

        click_post.setOnClickListener {
            val intent = Intent(context, MyPostMngActivity::class.java)
            intent.putExtra("founder", member_id)
            intent.putExtra("type", "founder")
            intent.putExtra("nick",txUserName.text.toString())
            startActivity(intent)
        }

    }

    fun get_user_information(){
        val params = RequestParams()
        //params.put("member_id")//상대방 홈페이지로 넘어갈 때 주는 인텐트값을 줌
    }

    fun member_info(member_id:String){
        val params = RequestParams()
        params.put("member_id", member_id)

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject) {
                try {
                    val result = response.getString("result")

                    if (result == "ok") {

                        val member = response.getJSONObject("Member")

                        val friendCount = response.getString("friendCount")
                        val contentCount = response.getString("contentCount")
                        val chatCount = response.getString("chatCount")

                        if (chatCount==null){
                            txChatCnt.setText("0")
                        }else{
                            txChatCnt.setText(chatCount)

                        }

                        txPostCnt.setText(contentCount)
                        friendCountTV.setText(friendCount)

                        textDate.text = Utils.getString(member,"created").substringBefore(" ")
                        txUserName.text = Utils.getString(member,"nick")

                        //지역
                        var region = ""

                        if (Utils.getString(member,"region1") != null) {
                            region += Utils.getString(member,"region1") + ","
                        }
                        if (Utils.getString(member,"region2") != null) {
                            region += Utils.getString(member,"region2") + ","
                        }
                        if (Utils.getString(member,"region3") != null) {
                            region += Utils.getString(member,"region3")
                        }

                        /*       if (region.substring(region.length-1) == ","){
                                   region = region.substring(0, region.length-2)
                               }*/
                        txUserRegion.text = region

                        //상메
                        var statusMessage = Utils.getString(member,"status_msg")
                        if (statusMessage != null) {
                            statusMessageTV.text = statusMessage
                        }

                        knowTogether.visibility = View.GONE

                        //해시태그
                        val data = response.getJSONArray("MemberTags")
                        if (data != null) {
                            var string_tag = ""
                            for (i in 0 until data.length()) {
                                var json = data[i] as JSONObject
                                val memberTag = json.getJSONObject("MemberTag")

                                string_tag += "#" + Utils.getString(memberTag, "tag") + " "
                            }
//                            hashtagTV.text = string_tag
                        }

                        //프로필 이미지
                        val imgData = response.getJSONArray("MemberImgs")
                        txPhotoCnt.text = imgData.length().toString()

                        //val tmpProfileImage = imgData.getJSONObject(0)
                        val img_uri = Utils.getString(member,"profile_img")//small_uri
                        val image = Config.url + img_uri

                        ImageLoader.getInstance().displayImage(image, otherPrfImgIV, Utils.UILoptionsProfile)

                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject) {
                println(errorResponse.toString())
            }
        })

    }


}
