package com.beanstream.sample.goldeneggs.main;

import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beanstream.mobilesdk.BeanstreamAPI;
import com.beanstream.sample.goldeneggs.GoldenEggsApplication;
import com.beanstream.sample.goldeneggs.R;
import com.beanstream.sample.goldeneggs.events.TitleEvent;
import com.beanstream.sample.goldeneggs.views.SignatureView;

import de.greenrobot.event.EventBus;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 03/02/2016.
 */
public class SignatureFragment extends Fragment {

    public final String FRAGMENT_TITLE = "Signature";

    View rootView;
    SignatureView signatureView;

    String transactionId;
    BeanstreamAPI beanstreamAPI;
    Button submitButton;
    ProgressBar progressBar;

    Bundle extras;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_signature, container, false);

        submitButton = (Button) rootView.findViewById(R.id.submitButton);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000, getResources().getColor(R.color.colorPrimary)));
        signatureView = (SignatureView) rootView.findViewById(R.id.signatureView);
        extras = getArguments();

        transactionId = extras.getString("TransactionID");

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signatureView.isSigned()) {
                    GoldenEggsApplication application = (GoldenEggsApplication) getActivity().getApplication();
                    beanstreamAPI = application.getBeanstreamAPI();
                    beanstreamAPI.attachSignature(transactionId, signatureView.getSignatureBitMap());
                    showProgressBar();
                }else{
                    Toast.makeText(getActivity(), R.string.signature_required_text, Toast.LENGTH_LONG).show();
                }
            }
        });

        ViewTreeObserver viewTreeObserver = signatureView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                signatureView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = signatureView.getMeasuredWidth();
                int height = signatureView.getMeasuredHeight();
                signatureView.setDimensions(width, height);
            }
        });

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

    public void showSubmitButton() {
        submitButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void showProgressBar() {
        submitButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

}

