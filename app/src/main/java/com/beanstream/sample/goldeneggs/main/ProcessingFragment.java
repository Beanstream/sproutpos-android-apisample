package com.beanstream.sample.goldeneggs.main;

import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beanstream.sample.goldeneggs.R;
import com.beanstream.sample.goldeneggs.events.TitleEvent;

import de.greenrobot.event.EventBus;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 03/02/2016.
 */
public class ProcessingFragment extends Fragment {

    public final String FRAGMENT_TITLE = "Processing";

    View rootView;
    ProgressBar progressBar;

    TextView processing;
    TextView payment;
    ImageView completedImage;
    int status = 0;

    private final int PROCESSING = 0;
    private final int APPROVED = 1;
    private final int DECLINED = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_processing, container, false);

        processing = (TextView) rootView.findViewById(R.id.processing_text);
        payment = (TextView) rootView.findViewById(R.id.payment_text);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        completedImage = (ImageView) rootView.findViewById(R.id.completedImage);

        progressBar.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000, getResources().getColor(R.color.colorPrimary)));

        if (savedInstanceState != null) {
            status = savedInstanceState.getInt("status", PROCESSING);
            if (status == APPROVED) {
                showTransactionApproved();
            } else if (status == DECLINED) {
                showTransactionDeclined();
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

    public void showTransactionApproved() {
        status = APPROVED;
        processing.setTextColor(ContextCompat.getColor(getActivity(), R.color.approvedColor));
        payment.setTextColor(ContextCompat.getColor(getActivity(), R.color.approvedColor));
        progressBar.setVisibility(View.GONE);
        completedImage.setVisibility(View.VISIBLE);
        completedImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.approved_processing));
    }

    public void showTransactionDeclined() {
        status = DECLINED;
        processing.setTextColor(ContextCompat.getColor(getActivity(), R.color.declinedColor));
        payment.setTextColor(ContextCompat.getColor(getActivity(), R.color.declinedColor));
        progressBar.setVisibility(View.GONE);
        completedImage.setVisibility(View.VISIBLE);
        completedImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.declined_processing));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("status", status);
    }
}

