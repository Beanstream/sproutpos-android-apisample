package com.beanstream.sample.goldeneggs.main;

import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.beanstream.mobile.sdk.transport.entity.Request.TransactionRequest;
import com.beanstream.sample.goldeneggs.R;
import com.beanstream.sample.goldeneggs.events.TitleEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import de.greenrobot.event.EventBus;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 03/02/2016.
 */
public class ReceiptFragment extends Fragment {

    public final String FRAGMENT_TITLE = "Transaction Receipt";

    View rootView;
    WebView webView;
    String customerReceipt;
    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_receipt, container, false);
        webView = (WebView) rootView.findViewById(R.id.htmlReceipt);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000, ContextCompat.getColor(getActivity(),R.color.colorPrimary)));

        Bundle extras = getArguments();
        if (extras != null) {
            customerReceipt = extras.getString("customerReceipt");
            webView.loadData(customerReceipt, "text/html", "UTF-8");
            showWebView();
        } else {
            if (savedInstanceState != null) {
                customerReceipt = savedInstanceState.getString("customerReceipt", "");
                if (customerReceipt.length() > 0) {
                    webView.loadData(customerReceipt, "text/html", "UTF-8");
                    showWebView();
                }
            }
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new TitleEvent(FRAGMENT_TITLE));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setReceipt(String customerReceipt) {
        showWebView();
        this.customerReceipt = customerReceipt;
        webView.loadData(customerReceipt, "text/html", "UTF-8");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("customerReceipt", customerReceipt);
    }

    private void showWebView(){
        webView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
}