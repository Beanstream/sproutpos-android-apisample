package com.beanstream.sample.goldeneggs.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.beanstream.mobile.sdk.transport.entity.PaymentMethods;
import com.beanstream.mobile.sdk.transport.entity.Request.TransactionRequest;
import com.beanstream.mobile.sdk.transport.entity.TransactionTypes;
import com.beanstream.mobilesdk.BeanstreamAPI;
import com.beanstream.sample.goldeneggs.GoldenEggsApplication;
import com.beanstream.sample.goldeneggs.R;
import com.beanstream.sample.goldeneggs.events.TitleEvent;
import com.cocosw.bottomsheet.BottomSheet;

import de.greenrobot.event.EventBus;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 03/02/2016.
 */
public class SaleFragment extends Fragment {

    public final String FRAGMENT_TITLE = "Payment";

    View rootView;
    BeanstreamAPI beanstreamAPI;

    LinearLayout checkoutButton;
    ProgressBar progressBar;
    LinearLayout progressBarLayout;

    public payment_callback initiatePayment = sDummyCallbacks;

    public interface payment_callback {
        void initiatePayment(TransactionRequest request);
    }

    private static payment_callback sDummyCallbacks = new payment_callback() {
        public void initiatePayment(TransactionRequest request) {}
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            initiatePayment = (payment_callback) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement payment_callback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_sale, container, false);

        GoldenEggsApplication application = (GoldenEggsApplication) getActivity().getApplication();
        beanstreamAPI = application.getBeanstreamAPI();

        checkoutButton = (LinearLayout) rootView.findViewById(R.id.checkoutButton);
        progressBarLayout = (LinearLayout) rootView.findViewById(R.id.progressBarLayout);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final TransactionRequest transactionRequest = new TransactionRequest();

                transactionRequest.setTrans_type(TransactionTypes.PURCHASE);
                transactionRequest.setAmount("5.00");
                transactionRequest.setLatitude("48.438077");
                transactionRequest.setLongitude("-123.36643");

                new BottomSheet.Builder(getActivity(),R.style.BottomSheet_StyleDialog).title(R.string.payment_methods_title).sheet(R.menu.bottom_menu).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.menu_cash:
                                transactionRequest.setPayment_method(PaymentMethods.CASH);
                                break;

                            case R.id.menu_check:
                                transactionRequest.setPayment_method(PaymentMethods.CHECK);
                                break;

                            case R.id.menu_credit:
                                transactionRequest.setPayment_method(PaymentMethods.CREDIT_EMV);
                                break;

                            case R.id.menu_debit:
                                transactionRequest.setPayment_method(PaymentMethods.DEBIT_EMV);
                                break;
                        }

                        initiatePayment.initiatePayment(transactionRequest);

                    }
                }).show();
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.action_initialize_disconnected).setVisible(false);
        menu.findItem(R.id.action_initialize_connected).setVisible(false);


        if (beanstreamAPI.isPinPadConnectionAlive()) {
            menu.findItem(R.id.action_initialize_connected).setVisible(true);
        } else {
            menu.findItem(R.id.action_initialize_disconnected).setVisible(true);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void showCheckoutButton() {
        checkoutButton.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.INVISIBLE);
    }

    public void showProgressBar() {
        checkoutButton.setVisibility(View.INVISIBLE);
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new TitleEvent(FRAGMENT_TITLE));
    }
}