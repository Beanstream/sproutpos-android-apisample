package com.beanstream.sample.goldeneggs.signin;

import android.graphics.LightingColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
public class SignInFragment extends Fragment {

    public final String FRAGMENT_TITLE = "Beanstream Login";

    View rootView;
    BeanstreamAPI beanstreamAPI;

    EditText companyLogin;
    EditText userName;
    EditText password;
    CheckBox rememberMeCheckbox;
    Button signInButton;
    ProgressBar progressBar;
    TextView apiState;
    LinearLayout rememberMeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        companyLogin = (EditText) rootView.findViewById(R.id.companyLogin);
        userName = (EditText) rootView.findViewById(R.id.userName);
        password = (EditText) rootView.findViewById(R.id.password);
        rememberMeCheckbox = (CheckBox) rootView.findViewById(R.id.remember_checkbox);
        signInButton = (Button) rootView.findViewById(R.id.signInButton);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        apiState = (TextView) rootView.findViewById(R.id.apiStateHeader);
        rememberMeLayout = (LinearLayout) rootView.findViewById(R.id.remember_me_layout);

        progressBar.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000, getResources().getColor(R.color.colorAccent)));

        GoldenEggsApplication application = (GoldenEggsApplication) getActivity().getApplication();
        beanstreamAPI = application.getBeanstreamAPI();


        boolean isRememberMe = beanstreamAPI.isRememberMe();

        if (isRememberMe && beanstreamAPI.isPasswordSaved()) {
            beanstreamAPI.createSessionWithSavedCredentials();
            showProgressBar();
        }

        if (rememberMeCheckbox != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            rememberMeCheckbox.setChecked(isRememberMe);

            rememberMeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    boolean isSuccessful = beanstreamAPI.setRememberMe(isChecked);
                    if (!isSuccessful) {
                        Toast.makeText(getActivity(), R.string.remember_me_error_message, Toast.LENGTH_LONG).show();
                    }
                }
            });

        }else{
            rememberMeLayout.setVisibility(View.GONE);
        }



        if (application.isSimulation()) {
            companyLogin.setText("sampleCo");
            userName.setText("sampleUser");
            password.setText("samplePassword");
        } else {
            companyLogin.setText(beanstreamAPI.getSavedCompanyLogin());
            userName.setText(beanstreamAPI.getSavedUsername());
            hideSimulatorHeader(rootView);
        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!beanstreamAPI.isInternetAvailable(getActivity())) {
                    Toast.makeText(getActivity(), R.string.internet_not_available, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (areEmptyFields()) {
                    Toast.makeText(getActivity(), R.string.please_fill_out_all_fields, Toast.LENGTH_SHORT).show();
                    return;
                }

                beanstreamAPI.createSession(companyLogin.getText().toString(),
                        userName.getText().toString(),
                        password.getText().toString());

                showProgressBar();

            }
        });

        return rootView;
    }

    private void hideSimulatorHeader(View view) {

        view.findViewById(R.id.apiStateColorBlock).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.apiStateHeader).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.apiStateCredentials).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.apiStateColorBlock).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.apiStateImage).setVisibility(View.INVISIBLE);

    }

    private boolean areEmptyFields() {
        return companyLogin.getText().length() == 0 || userName.getText().length() == 0 || password.getText().length() == 0;
    }

    public void showSignInButton() {
        signInButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    public void showProgressBar() {
        signInButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new TitleEvent(FRAGMENT_TITLE));
    }


}
