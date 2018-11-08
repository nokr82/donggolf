package donggolf.android.fragment

import android.os.Bundle
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.activities.*
import donggolf.android.adapters.MainAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.base.PrefUtils
import org.json.JSONObject

open class FreeFragment : Fragment() {


    var ctx: Context? = null


    //원본 데이터 정의 부분
    private  var adapterData : ArrayList<Map<String, Any>> = ArrayList<Map<String, Any>>()
    private  lateinit var  adapter : MainAdapter
    private  lateinit var  editadapter : MainEditAdapter
    private  var editadapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    val user = HashMap<String, Any>()

    lateinit var main_edit_listview: ListView
    lateinit var addpostLL : LinearLayout
    lateinit var main_edit_search : EditText
    lateinit var main_listview_search : LinearLayout
    lateinit var main_edit_close : TextView

    lateinit var main_listview:ListView


    private var mAuth: FirebaseAuth? = null
    //원본 데이터 정의 끝

    private var progressDialog: ProgressDialog? = null

    lateinit var activity: MainActivity//메인액티비티 위에 드로잉할 권한
    var tabType = 1//탭호스트 위치 초기화
    var member_id = -1 //로그인 회원 아이디 값
    private val SELECT_PICTURE: Int = 101


    lateinit var vpPage: ViewPager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        progressDialog = ProgressDialog(context)

        val ctx = context
        if (null != ctx) {
            doSomethingWithContext(ctx)
        }

        //여기서 데이터를 불러와야 함
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()

        println("currentUser======$currentUser")

        updateUI(currentUser)

        val db = FirebaseFirestore.getInstance()

        user.put("first", "Ada")
        user.put("last", "Lovelace")
        user.put("born", 1815)

        db.collection("users")
                .get()
                .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                    override fun onComplete(task: Task<QuerySnapshot>) {
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                Log.d(MainActivity.TAG, document.getId() + " => " + document.getData())
                            }
                        } else {
                            Log.w(MainActivity.TAG, "Error getting documents.", task.exception)
                        }
                    }
                })

        ContentAction.list(user,Pair("createAt",null),0) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->

            if(success && data != null) {
                data.forEach {
                    println(it)

                    if (it != null) {
                        adapterData.add(it)
                    }

                }

                adapter.notifyDataSetChanged()

            }

        }

        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    //여기서 뷰 연결
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main_edit_search = view.findViewById(R.id.main_edit_search)
        addpostLL = view.findViewById(R.id.addpostLL)
        main_listview_search = view.findViewById(R.id.main_listview_search)
        main_edit_close = view.findViewById(R.id.main_edit_close)
        main_edit_listview = view.findViewById(R.id.main_edit_listview)
        main_listview = view.findViewById(R.id.main_listview)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity = getActivity() as MainActivity

        //기본화면설정

        // 뷰페이저

        adapter = MainAdapter(activity,R.layout.main_listview_item,adapterData)

        main_listview.adapter = adapter

        main_listview.setOnItemClickListener { parent, view, position, id ->
            val id : String
            id = adapterData.get(position)!!.get("id").toString()
            MoveMainDetailActivity(id)
        }

        addpostLL.setOnClickListener {
            MoveAddPostActivity()
        }

        member_id = PrefUtils.getIntPreference(context, "member_id")

    }

    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
        progressDialog = ProgressDialog(ctx)
    }


    //pageAdapter class


    //여기서부터는 MainActivity에 있던 코드들
    fun MoveAddPostActivity(){

        var intent = Intent(context, AddPostActivity::class.java);
        intent.putExtra("category",1)
        startActivityForResult(intent, SELECT_PICTURE);
    }

    fun MoveMainDetailActivity(id : String){
        var intent: Intent = Intent(activity, MainDetailActivity::class.java)
        intent.putExtra("id",id)
        startActivity(intent)
    }

    fun MoveAreaRangeActivity(){
        var intent: Intent = Intent(activity, AreaRangeActivity::class.java)
        startActivity(intent)
    }

    fun MoveMarketMainActivity(){
        var intent: Intent = Intent(activity, MarketMainActivity::class.java)
        startActivity(intent)
    }


    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun updateUI(currentUser: FirebaseUser?) {

        /*
        mAuth!!.signInWithCustomToken(mCustomToken)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCustomToken:success")
                        val user = mAuth!!.getCurrentUser()
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        */
    }

    override fun onResume() {
        super.onResume()
        ContentAction.list(user,Pair("createAt",null),0) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->

            if(success && data != null) {
                data.forEach {
                    println(it)

                    if (it != null) {
                        adapterData.add(it)
                    }

                }

                adapter.notifyDataSetChanged()

            }

        }
    }

}