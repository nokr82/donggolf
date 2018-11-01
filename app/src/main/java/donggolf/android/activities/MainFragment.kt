package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import org.json.JSONException
import org.json.JSONObject

open class MainFragment : Fragment() {
    private var progressDialog: ProgressDialog? = null

    lateinit var activity: MainActivity//메인액티비티 위에 드로잉할 권한
    var tabType = 1//탭호스트 위치 초기화
    var member_id = -1 //로그인 회원 아이디 값

    /*lateinit var tabMyTown:Button
    lateinit var tabChat:Button
    lateinit var tabNotice:Button
    lateinit var tabMyPage:Button*/

    lateinit var tabMyTown: RelativeLayout
    lateinit var tabChat: RelativeLayout
    lateinit var tabNotice: RelativeLayout
    lateinit var tabMyPage: RelativeLayout

    lateinit var vpPage: ViewPager

    lateinit var main_listview:ListView

    var adapterData: ArrayList<JSONObject> = ArrayList<JSONObject>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        progressDialog = ProgressDialog(context)

        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //탭 호스트 글자색 변경같이 눌렀을 때 변경되는 것
        /*free2TV = view.findViewById(R.id.free2TX)
        info2TV = view.findViewById(R.id.info2TX)
        study2TV = view.findViewById(R.id.Study2TX)
        class2TV = view.findViewById(R.id.class2TX)
        meeting2TV = view.findViewById(R.id.Miting2TX)
        coupon2TV = view.findViewById(R.id.Coupon2TX)



        free2V = view.findViewById(R.id.free2V)
        info2V = view.findViewById(R.id.info2V)
        study2V = view.findViewById(R.id.Study2V)
        class2V = view.findViewById(R.id.class2V)
        meeting2V = view.findViewById(R.id.miting2V)
        coupon2V = view.findViewById(R.id.coupon2V)*/

        tabMyTown = view.findViewById(R.id.tabMyTown)
        tabChat = view.findViewById(R.id.tabChat)
        tabNotice = view.findViewById(R.id.tabNotice)
        tabMyPage = view.findViewById(R.id.tabMyPage)

        main_listview = view.findViewById(R.id.main_listview)

        vpPage = view.findViewById(R.id.pagerVP)
    }

    /*fun setMenuTabView() {
        tabMyTown.setTextColor(Color.parseColor("#A19F9B"))
        info2TV.setTextColor(Color.parseColor("#A19F9B"))
        study2TV.setTextColor(Color.parseColor("#A19F9B"))
        class2TV.setTextColor(Color.parseColor("#A19F9B"))
        meeting2TV.setTextColor(Color.parseColor("#A19F9B"))
        coupon2TV.setTextColor(Color.parseColor("#A19F9B"))

        //탭 호스트 밑에 언더라인. 고객사의 요청이 있을 때까지 보류
        free2V.visibility = View.INVISIBLE
        info2V.visibility = View.INVISIBLE
        study2V.visibility = View.INVISIBLE
        class2V.visibility = View.INVISIBLE
        meeting2V.visibility = View.INVISIBLE
        coupon2V.visibility = View.INVISIBLE


        if(tabType == 1) {
            free2TV.setTextColor(Color.parseColor("#01b4ec"))
            free2V.visibility = View.VISIBLE
        } else if (tabType == 2) {
            info2V.visibility = View.VISIBLE
            info2TV.setTextColor(Color.parseColor("#01b4ec"))
        } else if (tabType == 3) {
            study2V.visibility = View.VISIBLE
            study2TV.setTextColor(Color.parseColor("#01b4ec"))
        } else if (tabType == 4) {
            class2V.visibility = View.VISIBLE
            class2TV.setTextColor(Color.parseColor("#01b4ec"))
        } else if (tabType == 5) {
            meeting2V.visibility = View.VISIBLE
            meeting2TV.setTextColor(Color.parseColor("#01b4ec"))
        } else if (tabType == 6) {
            coupon2V.visibility = View.VISIBLE
            coupon2TV.setTextColor(Color.parseColor("#01b4ec"))
        }


    }*/

    /*fun loadData(type: Int) {
        val params = RequestParams()
        member_id = PrefUtils.getIntPreference(context, "member_id")
        params.put("member_id", member_id)
        params.put("tab", taptype)
        params.put("type", tabType)

        MemberAction.my_page_index(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                try {
                    val result = response!!.getString("result")

                    adapterData.clear()
                    if ("ok" == result) {

                        var member = response.getJSONObject("member");



                        val data = response.getJSONArray("list")

                        for (i in 0..data.length() - 1) {

                            adapterData.add(data[i] as JSONObject)

                        }
                        adapterMy.notifyDataSetChanged()

                    } else {
                        Toast.makeText(context, "일치하는 회원이 존재하지 않습니다.", Toast.LENGTH_LONG).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }


            override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseString: String?) {

                // System.out.println(responseString);
            }

            private fun error() {
                Utils.alert(context, "조회중 장애가 발생하였습니다.")
            }

            override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>?,
                    responseString: String?,
                    throwable: Throwable
            ) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                // System.out.println(responseString);

                throwable.printStackTrace()
                error()
            }


            override fun onStart() {
                // show dialog
                if (progressDialog != null) {

                    progressDialog!!.show()
                }
            }

            override fun onFinish() {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
            }
        })
    }*/

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity = getActivity() as MainActivity
//        val filter2 = IntentFilter("DEL_POSTING")
//        activity.registerReceiver(delPostingReceiver, filter2)

        //기본화면설정
        tabType= 1
        //setMenuTabView()
        //loadData(tabType)


        tabMyTown.setOnClickListener {
            adapterData.clear()
            tabType = 1;
            //loadData(tabType)
            //setMenuTabView()

        }

        tabChat.setOnClickListener {
            adapterData.clear()
            tabType = 2;
            //loadData(tabType)
            //setMenuTabView()

        }

        tabNotice.setOnClickListener {
            adapterData.clear()
            tabType = 3;
            //loadData(tabType)
            //setMenuTabView()
        }

        tabMyPage.setOnClickListener {
            adapterData.clear()
            tabType = 4;
            //loadData(tabType)
            //setMenuTabView()

        }

        member_id = PrefUtils.getIntPreference(context, "member_id")

    }

}