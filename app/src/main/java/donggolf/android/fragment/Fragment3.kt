package donggolf.android.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import donggolf.android.R

class Fragment3 : Fragment() {



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return LayoutInflater.from(inflater.context).inflate(R.layout.fragment3,container, false)

    }

}