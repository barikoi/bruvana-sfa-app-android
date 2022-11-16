package com.barikoi.cnlapp.Notice

import java.io.Serializable

class Notice (
    val notice_id: String,
    val message: String,
    val updated_at: String,
    val senderName: String,
    val designation: String,
    val imageUrl: String
    ):Serializable