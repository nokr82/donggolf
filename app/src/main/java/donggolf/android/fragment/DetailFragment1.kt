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
import donggolf.android.models.Content
import kotlinx.android.synthetic.main.fragment_main.*
import org.json.JSONObject

open class DetailFragment1 : Fragment() {


    var ctx: Context? = null

    private var mAuth: FirebaseAuth? = null

    lateinit var activity: MainDetailActivity

    lateinit var imgIV: ImageView




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val ctx = context
        if (null != ctx) {
            doSomethingWithContext(ctx)
        }

        //여기서 데이터를 불러와야 함
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()


        val db = FirebaseFirestore.getInstance()




        return inflater.inflate(R.layout.fragmentdetail1, container, false)
    }

    //여기서 뷰 연결
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //탭 호스트 글자색 변경같이 눌렀을 때 변경되는 것
        imgIV = view.findViewById(R.id.imgIV)


    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity = getActivity() as MainDetailActivity

        //기본화면설정


    }

    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
    }


    //pageAdapter class

    //여기서부터는 MainActivity에 있던 코드들


}