package com.barikoi.cnlapp.callback

import android.location.Location

interface LocationFetch {
    fun onFetchSuccess(location: Location)
    fun onFailure()
}