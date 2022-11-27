package com.example.boogiestagram.navigation

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.boogiestagram.R
import com.example.boogiestagram.navigation.model.AlarmDTO
import com.example.boogiestagram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*


class DetailViewFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    companion object{
        var lottieheart : LottieAnimationView? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid


        view.detailviewfragment_recyclerview.adapter = DetailVewRecyclerViewAdater()
        view.detailviewfragment_recyclerview.layoutManager =  LinearLayoutManager(activity)
        //역순으로 출력
        (view.detailviewfragment_recyclerview.layoutManager as LinearLayoutManager).reverseLayout = true
        (view.detailviewfragment_recyclerview.layoutManager as LinearLayoutManager).stackFromEnd = true
        //최상단으로 출력
        view.detailviewfragment_recyclerview?.smoothScrollToPosition(0)
        return view
    }
    inner class DetailVewRecyclerViewAdater : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                //Sometimes, This code return null of querySnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail,p0,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            //p0값을 CustomViewHolder 에 캐스팅
            var viewholder = (p0 as CustomViewHolder).itemView

            //UserId
            viewholder.detailviewitem_profile_textview.text = contentDTOs[p1].userId

            //Image
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl).into(viewholder.detailviewitem_imageview_content)

            //Explan of content
            viewholder.detailviewitem_explan_textview.text = contentDTOs[p1].explain

            //likes
            viewholder.detailviewitem_favoritecount_textview.text = "좋아요 " + contentDTOs[p1].favoriteCount

            //ProfileImage
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl).into(viewholder.detailviewitem_imageview_content)

            //This code is when the button is clicked
//            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
//                favoriteEvent(p1)
//            }

            viewholder.favorite_animationView.setOnClickListener {
                favoriteEvent(p1)
            }

            //This code os when the page is loaded
            if(contentDTOs[p1].favorites.containsKey(uid)){
                //This is like status
                //viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
                val animator = ValueAnimator.ofFloat(0f, 0.5f).setDuration(1500)
                animator.addUpdateListener {
                    viewholder.favorite_animationView!!.progress = it.animatedValue as Float
                }
                animator.start()
            }else{
                //This is unlike status
                //viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
                val animator = ValueAnimator.ofFloat(0f).setDuration(500)
                animator.addUpdateListener {
                    viewholder.favorite_animationView!!.progress = it.animatedValue as Float
                }
                animator.start()
            }

            //This code is when the profile image clicked : 프로필 이미지 클릭 이벤트
            viewholder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[p1].uid)
                bundle.putString("userId", contentDTOs[p1].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }
            //This code is when the profile text clicked : 프로필옆 UID 이벤트
            viewholder.detailviewitem_profile_textview.setOnClickListener{
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[p1].uid)
                bundle.putString("userId", contentDTOs[p1].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }

            //리플 이벤트
            viewholder.detailviewitem_comment_imageview.setOnClickListener { view ->
                var intent = Intent(view.context , CommentActivity::class.java)
                intent.putExtra("contentUid" , contentUidList[p1])
                intent.putExtra("destinationUid" , contentDTOs[p1].uid)
                startActivity(intent)
            }

        }

        var fragmentManager : FragmentManager? = null
        fun test(view: View){
            //   fragmentManager.beginTransaction()
            //     .setCustomAnimations(R.id.favorite_animationView)
        }
        //좋아요 이벤트
        fun favoriteEvent (position: Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])

            //데이터를 입력하기 위해 transaction을 불러오는
            firestore?.runTransaction { transaction ->
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    // When the button is clicked - 좋아요 버튼이 눌려 있을 때 좋아요 취소
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! - 1
                    contentDTO?.favorites?.remove(uid)
                }else{
                    // When the button is not clicked 좋아요 버튼이 눌려 있지 않을 때 클릭해서 좋아요.
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! + 1
                    contentDTO?.favorites?.set(uid!!, true)
                    favoriteAlramEvent(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc,contentDTO)

            }
        }

        //알람 이벤트
        fun favoriteAlramEvent(destinationUid: String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        }



    }
}