package com.example.boogiestagram.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.boogiestagram.LoginActivity
import com.example.boogiestagram.MainActivity
import com.example.boogiestagram.R
import com.example.boogiestagram.navigation.model.AlarmDTO
import com.example.boogiestagram.navigation.model.ContentDTO
import com.example.boogiestagram.navigation.model.FollowDTO
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment(){

    // Firebase
    var fragmentView : View? = null
    var auth : FirebaseAuth? = null

    //private String destinationUid;
    var uid : String? = null
    var currentUserUid : String? = null

    var firestore : FirebaseFirestore? = null

    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if(arguments != null){
            uid = arguments?.getString("destinationUid")
        }


        if(uid == currentUserUid){
            //MyPage
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        }else{
            //Other User Page
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)
            mainactivity?.toolbar_title_image?.visibility = View.GONE
            mainactivity?.toolbar_username?.visibility = View.VISIBLE
            mainactivity?.toolbar_back_btn?.visibility = View.VISIBLE

            //mainactivity?.toolbar_username?.text = arguments!!.getString("userId")
            mainactivity?.toolbar_back_btn?.setOnClickListener {
                mainactivity.bottom_navigation.selectedItemId = R.id.action_home
            }
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                requestFollw()
            }
        }
        fragmentView?.account_reclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_reclerview?.layoutManager = GridLayoutManager(activity!!, 3)


        //버튼 이벤트
        fragmentView?.account_iv_profile?.setOnClickListener{
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()
        getFollowAndFollowing()
        return fragmentView
    }

    fun followerAlarm(destinationUid : String) {
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun getProfileImage(){

        firestore?.collection("profileImages")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                if (documentSnapshot.data != null){
                    var url = documentSnapshot?.data!!["image"]
                    if(activity != null){
                        Glide.with(activity!!)
                            .load(url)
                            .apply(RequestOptions().circleCrop()).into(account_iv_profile!!)
                    }
                }
            }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun getFollowAndFollowing(){
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)

            if(followDTO?.followingCount != null){
                fragmentView?.account_tv_following_count?.text = followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount != null){
                fragmentView?.account_tv_follow_count?.text = followDTO?.followerCount?.toString()
                if(followDTO?.followers?.containsKey(currentUserUid)!!){
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow_cancel)
                    fragmentView?.account_btn_follow_signout?.background?.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
                }else{
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
                    if(uid != currentUserUid){
                        fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                        fragmentView?.account_btn_follow_signout?.text = getString(R.string.following)

                    }
                }
            }
        }
    }



    fun requestFollw(){
        //save data to my account
        var tsDocFollowing = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction{ transacion ->
            var followDTO = transacion.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followings[uid!!] = true
                followerAlarm(uid!!)

                transacion.set(tsDocFollowing, followDTO)
                return@runTransaction
            }
            if(followDTO.followings.containsKey(uid)){
                //It remove following thired person when a third person follow me
                //내가 팔로우 한 상태, 나의 키가 상대에게 있으면.
                followDTO?.followingCount = followDTO?.followingCount!! - 1
                followDTO?.followings?.remove(uid)
            }else {
                //It add following thired person when a third person do not follow me
                //내가 팔로우 안 한 상태라면
                followDTO?.followerCount = followDTO?.followingCount!! + 1
                followDTO?.followers?.set(uid!!, true)
                followerAlarm(uid!!)
            }
            transacion.set(tsDocFollowing, followDTO)
            return@runTransaction
        }
        //Save data to third person
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transacion ->
            var followDTO = transacion.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true

                transacion.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }
            if(followDTO!!.followers.containsKey(currentUserUid)){
                //It cancel my follower when I follow a third person
                // 팔로우를 한 상태에 클릭하면 팔로잉 취소
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)
            }else{
                //It add my follower when I don't follow a third person //팔로우를 하지 않은 상태에 클릭하면 팔로잉
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
            }
            transacion.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                //Sometimes, This code return null of querySnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener

                //Get Data
                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            //폭의 3분의 1 값을 가져와서
            var width = resources.displayMetrics.widthPixels / 3
            //이미지 뷰에 넣어줌
            var imageview = ImageView(p0.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var imageview = (p0 as CustomViewHolder).imageView
            Glide.with(p0.itemView.context)
                .load(contentDTOs[p1].imageUrl)
                .apply(RequestOptions().centerCrop())
                .into(imageview)
        }

    }
}

