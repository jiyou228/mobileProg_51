package com.example.boogiestagram.navigation.model

data class DdayDTO(
    var name_1 : String? = null, //왼쪽 인물의 이름
    var name_2 : String? = null, //오른쪽 인물의 이름
    var imageUrl_1 : String? = null, //왼쪽 인물의 이미지 링크
    var imageUrl_2 : String? = null, //오른쪽 인물의 이미지 링크
    var message : String? = null, //디데이 카운트 상단에 들어갈 문구
    var timestamp : Long? = null, //올린 날짜
    var daycount : Int = 0 //디데이 카운트
)