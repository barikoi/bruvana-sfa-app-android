package com.barikoi.cnlapp.ui.summary_details

interface DateFilterListener {
    fun onDataReceived(data: Pair<String, String>, useId: String)
}