package com.kidssaveocean.fatechanger.resources


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView

import com.kidssaveocean.fatechanger.R
import com.kidssaveocean.fatechanger.utility.GeneralImageListCardView

import java.util.ArrayList


class ResourcesFragment (private val url : String = "https://www.kidssaveocean.com/fatechanger-resources") : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_resources, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val webView: WebView = view.findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
    }
}
