package com.example.boogiestagram.navigation.model

data class AlarmDTO(
    var destinationUid : String? = null,
    var userId : String? = null,
    var uid : String? = null,

    //0: like alarm
    //1: comment alarm
    //2: follow alarm
    var kind : Int? = null, // 어떤 타입의 메세지 종류인지 확인 할 수 있는
    var message : String? = null, //메세지를 담을 메세지 String 변수
    var timestamp : Long? = null //현재 시간을 담을
)