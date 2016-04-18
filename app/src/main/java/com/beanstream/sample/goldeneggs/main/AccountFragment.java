package com.beanstream.sample.goldeneggs.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beanstream.mobilesdk.BeanstreamAPI;
import com.beanstream.sample.goldeneggs.GoldenEggsApplication;
import com.beanstream.sample.goldeneggs.R;
import com.beanstream.sample.goldeneggs.events.TitleEvent;

import de.greenrobot.event.EventBus;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 03/02/2016.
 */
public class AccountFragment extends Fragment {

    public final String FRAGMENT_TITLE = "Account";

    View rootView;
    BeanstreamAPI beanstreamAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_account, container, false);

        GoldenEggsApplication application = (GoldenEggsApplication) getActivity().getApplication();
        beanstreamAPI = application.getBeanstreamAPI();

        TextView accountLogin = (TextView) rootView.findViewById(R.id.account_login);
        TextView user = (TextView) rootView.findViewById(R.id.account_user);
        TextView companyName = (TextView) rootView.findViewById(R.id.account_company_name);
        TextView phoneNumber = (TextView) rootView.findViewById(R.id.account_phone);
        TextView currency = (TextView) rootView.findViewById(R.id.account_currency);

        accountLogin.setText(beanstreamAPI.getSavedCompanyLogin());
        user.setText(beanstreamAPI.getSavedUsername());

        Bundle args = getArguments();

        companyName.setText(args.getString("companyName", ""));
        phoneNumber.setText(args.getString("companyPhone", ""));
        currency.setText(args.getString("currency", ""));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.account_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new TitleEvent(FRAGMENT_TITLE));
    }

}

