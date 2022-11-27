package com.example.boogiestagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.boogiestagram.R
import com.example.boogiestagram.navigation.model.DdayDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*

class GridFragment : Fragment(){

    // Firebase
    var fragmentView : View? = null
    var auth : FirebaseAuth? = null

    //private String destinationUid;
    var uid : String? = null
    var currentUserUid : String? = null

    var firestore : FirebaseFirestore? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var viewGroup = LayoutInflater.from(activity).inflate(R.layout.fragment_grid, container, false)
        val fragmentManager : FragmentManager
        var fragmentTransaction : FragmentTransaction

//        val fragment = UserFragment()
//        fragmentTransaction.add(R.id.frgment_user, fragment)
//        fragmentTransaction.commit()

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

//        viewGroup.plus_icon.setOnClickListener {
//            val builder = AlertDialog.Builder(this.context)
//            builder.setTitle("디데이")
//                    .setMessage("디데이를 추가 하시겠습니까?")
//                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->
//                        plus_icon.visibility = View.GONE
//                    })
//                    .setPositiveButton("추가", DialogInterface.OnClickListener { dialogInterface, i ->
//                        plus_icon.visibility = View.VISIBLE
//                    })
//            builder.show()
//        }

        viewGroup.detailviewfragment_recyclerview.adapter = D_dayRecyclerViewAdater()
        viewGroup.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        viewGroup.detailviewfragment_recyclerview?.smoothScrollToPosition(0)
        return viewGroup

    }

    inner class D_dayRecyclerViewAdater : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var d_dayDTOs : ArrayList<DdayDTO> = arrayListOf()
        init {
            firestore?.collection("counter")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents){
                    var item = snapshot.toObject(DdayDTO::class.java)
                    d_dayDTOs.add(item!!)
                }

            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.d_dayfragment,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView
            var spinner: Spinner

//            viewholder.plus_icon.setOnClickListener { view ->
//
//        }
        }

        override fun getItemCount(): Int {
            return d_dayDTOs.size
        }
    }

}