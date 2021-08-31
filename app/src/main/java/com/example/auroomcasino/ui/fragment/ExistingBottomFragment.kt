package com.example.auroomcasino.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.auroomcasino.R
import com.example.auroomcasino.utils.OnLinkClickListener
import com.example.auroomcasino.utils.stringToLink
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_existing_bottom.*


class ExistingBottomFragment() : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_existing_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textLink.stringToLink(
            text = resources.getString(R.string.mirror_error),
            link = "https://t.me/auroombetcom",
            listener = onTextLinkClickListener()
        )

        textLinkTwo.stringToLink(
            text = resources.getString(R.string.mirror_errorTwo),
            link = "https://www.instagram.com/auroombet/",
            listener = onTextLinkClickListener()
        )
    }

    private fun onTextLinkClickListener() = object : OnLinkClickListener {
        override fun onClick(string: String) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(string))
            startActivity(browserIntent)
        }
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme;
    }
}