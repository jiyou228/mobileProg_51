package com.example.boogiestagram.navigation.model

data class PushDTO(
    var to : String? = null,
    var notification : Notification = Notification()
){
    data class Notification(
        var body : String? = null, //푸쉬메세지의 바디
        var title : String? = null //푸쉬메세지의 제목
    )
}