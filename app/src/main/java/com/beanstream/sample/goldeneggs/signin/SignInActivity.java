package com.beanstream.sample.goldeneggs.signin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.beanstream.mobile.sdk.transport.entity.Error.CreateSessionError;
import com.beanstream.mobile.sdk.transport.entity.Response.CreateSessionResponse;
import com.beanstream.mobilesdk.BeanstreamAPI;
import com.beanstream.mobilesdk.BeanstreamEvents;
import com.beanstream.sample.goldeneggs.GoldenEggsApplication;
import com.beanstream.sample.goldeneggs.R;
import com.beanstream.sample.goldeneggs.events.TitleEvent;
import com.beanstream.sample.goldeneggs.events.TryItEvent;
import com.beanstream.sample.goldeneggs.main.MainActivity;

import de.greenrobot.event.EventBus;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 03/02/2016.
 */
public class SignInActivity extends AppCompatActivity implements BeanstreamEvents.CreateSession {

    BeanstreamAPI beanstreamAPI;
    Toolbar toolbar;
    private static boolean welcomeShown;
    AlertDialog errorDialog;

    private final String WELCOME_FRAGMENT = "WELCOME";
    private final String SIGNIN_FRAGMENT = "SIGNIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        GoldenEggsApplication application = (GoldenEggsApplication) getApplication();
        beanstreamAPI = application.getBeanstreamAPI();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setVisibility(View.GONE);

        if (savedInstanceState == null) {

            if (getIntent().getBooleanExtra("invalidSession", false)) {
                showErrorDialog("", getString(R.string.invalid_session_dialog_message));
            }

            if (!welcomeShown) {
                welcomeShown = true;
                WelcomeFragment welcomeFragment = new WelcomeFragment();
                FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_container, welcomeFragment,WELCOME_FRAGMENT);
                transaction.commit();
            } else {
                SignInFragment signInFragment = new SignInFragment();
                FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_container, signInFragment, SIGNIN_FRAGMENT);
                transaction.commit();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
    }

    public void showErrorDialog(String title, String message) {
        errorDialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle).create();
        errorDialog.setTitle(title);
        errorDialog.setMessage(message);
        errorDialog.setCanceledOnTouchOutside(true);

        errorDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                errorDialog.dismiss();
            }
        });

        errorDialog.show();
    }

    public void onEventMainThread(TryItEvent event) {
        EventBus.getDefault().removeStickyEvent(event);

        SignInFragment signInFragment = new SignInFragment();
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, signInFragment, SIGNIN_FRAGMENT);
        transaction.commit();
    }

    public void onEventMainThread(CreateSessionResponse response) {
        EventBus.getDefault().removeStickyEvent(response);

        if (response.isAuthorized()) {
            if(response.getMerchant().getTerminalType().equals(CreateSessionResponse.TERMINAL_TYPE_ENCRYPTED)) {
                if (!getIntent().getBooleanExtra("invalidSession", false)) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("companyName", response.session.merchant.getCompanyName());
                    intent.putExtra("companyPhone", response.session.merchant.getCompanyPhone());
                    intent.putExtra("currency", response.session.merchant.getCurrencyType());
                    startActivity(intent);
                }
                finish();
            }else{
                showErrorDialog("Invalid Account Type", "This account type is not supported.");
                showSignInButton();
            }
        } else {
            showErrorDialog("", response.getMessage());
            showSignInButton();
        }
    }

    private void showSignInButton(){
        SignInFragment signInFragment = (SignInFragment) getSupportFragmentManager().findFragmentByTag(SIGNIN_FRAGMENT);
        if (signInFragment != null) {
            signInFragment.showSignInButton();
        }
    }

    public void onEventMainThread(CreateSessionError response) {
        EventBus.getDefault().removeStickyEvent(response);

        beanstreamAPI.clearSavedPassword();

        SignInFragment signInFragment = (SignInFragment) getSupportFragmentManager().findFragmentByTag(SIGNIN_FRAGMENT);
        if (signInFragment != null) {
            signInFragment.showSignInButton();
        }

        showErrorDialog("", response.getUserFacingMessage());
    }

    public void onEventMainThread(TitleEvent event) {
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(event.getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onDestroy(){
        if(errorDialog != null && errorDialog.isShowing()){
            errorDialog.dismiss();
        }

        super.onDestroy();
    }
}