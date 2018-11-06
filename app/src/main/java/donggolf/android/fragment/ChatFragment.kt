package donggolf.android.fragment

import android.os.Bundle
import android.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import donggolf.android.R

class ChatFragment : android.support.v4.app.Fragment() {

    lateinit var tabMyChat : ImageView
    lateinit var tabTownChat : ImageView
    lateinit var btn_myChat_mng : ImageView
    lateinit var btn_make_chat : ImageView
    lateinit var txMyChat : TextView
    lateinit var txTownChat : TextView
    lateinit var chat_list : ListView
    lateinit var viewpagerChat : ViewPager





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
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

        tabMyChat = view.findViewById(R.id.tabMyChat)
        tabTownChat = view.findViewById(R.id.tabTownChat)
        btn_myChat_mng = view.findViewById(R.id.btn_myChat_mng)
        btn_make_chat = view.findViewById(R.id.btn_make_chat)
        txMyChat = view.findViewById(R.id.txMyChat)
        txTownChat = view.findViewById(R.id.txTownChat)

        chat_list = view.findViewById(R.id.chat_list)

        viewpagerChat = view.findViewById(R.id.viewpagerChat)
    }










//    class PagerAdapter(fm: android.app.FragmentManager) : FragmentStatePagerAdapter(fm) {
//
//        override fun getItem(i: Int): android.support.v4.app.Fragment {
//
//            var fragment: android.support.v4.app.Fragment
//
//            val args = Bundle()
//            when (i) {
//                0 -> {
//                    fragment = FreeFragment()
//                    fragment.arguments = args
//
//                    return fragment
//                }
//                1 -> {
//                    fragment = InfoFragment()
//                    fragment.arguments = args
//
//                    return fragment
//                }
//                2 -> {
//                    fragment = StudyFragment()
//                    fragment.arguments = args
//                    return fragment
//                }
//                3 -> {
//                    fragment = ClassFragment()
//                    fragment.arguments = args
//                    return fragment
//                }
//                4 -> {
//                    fragment = MeetingFragment()
//                    fragment.arguments = args
//                    return fragment
//                }
//                5 -> {
//                    fragment = CouponFragment()
//                    fragment.arguments = args
//                    return fragment
//                }
//                else -> {
//                    fragment = FreeFragment()
//                    fragment.arguments = args
//                    return fragment
//                }
//            }
//        }
//
//        override fun getCount(): Int {
//            return 6
//        }
//
//        override fun getPageTitle(position: Int): CharSequence? {
//            return ""
//        }
//
//        override fun getItemPosition(`object`: Any): Int {
//            return POSITION_NONE
//        }
//    }
//
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        pagerAdapter = PagerAdapter(getChildFragmentManager())
//        viewpagerChat.adapter = pagerAdapter
//        viewpagerChat.setOnPageChangeListener(object : ViewPager.OnPageChangeListener{
//            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//        })
//    }








}
