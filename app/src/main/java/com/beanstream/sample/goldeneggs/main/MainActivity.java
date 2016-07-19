package com.beanstream.sample.goldeneggs.main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beanstream.emv.entity.InitCardReaderResponse;
import com.beanstream.emv.events.PinPadStateChangeEvent;
import com.beanstream.mobile.sdk.transport.entity.Error.AttachSignatureError;
import com.beanstream.mobile.sdk.transport.entity.Error.AuthenticateSessionError;
import com.beanstream.mobile.sdk.transport.entity.Error.GetReceiptError;
import com.beanstream.mobile.sdk.transport.entity.Error.TransactionError;
import com.beanstream.mobile.sdk.transport.entity.Request.TransactionRequest;
import com.beanstream.mobile.sdk.transport.entity.Response.AttachSignatureResponse;
import com.beanstream.mobile.sdk.transport.entity.Response.AuthenticateSessionResponse;
import com.beanstream.mobile.sdk.transport.entity.Response.GetReceiptResponse;
import com.beanstream.mobile.sdk.transport.entity.Response.TransactionResponse;
import com.beanstream.mobile.sdk.transport.entity.Response.UpdatePinPadResponse;
import com.beanstream.mobile.sdk.transport.events.PasswordRequiredEvent;
import com.beanstream.mobile.sdk.transport.events.SessionInvalidEvent;
import com.beanstream.mobilesdk.BeanstreamAPI;
import com.beanstream.sample.goldeneggs.GoldenEggsApplication;
import com.beanstream.sample.goldeneggs.R;
import com.beanstream.sample.goldeneggs.events.LoadGetReceiptEvent;
import com.beanstream.sample.goldeneggs.events.LoadSignatureEvent;
import com.beanstream.sample.goldeneggs.events.TitleEvent;
import com.beanstream.sample.goldeneggs.signin.SignInActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.danlew.android.joda.JodaTimeAndroid;

import java.lang.reflect.Type;

import de.greenrobot.event.EventBus;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 * <p/>
 * Created by babramovitch on 03/02/2016.
 */
public class MainActivity extends AppCompatActivity implements SaleFragment.payment_callback, HistoryFragment.HistoryCallback {

    private final String SALE_FRAGMENT = "SALE";
    private final String PROCESSING_FRAGMENT = "PROCESSING";
    private final String SIGNATURE_FRAGMENT = "SIGNATURE";
    private final String HISTORY_FRAGMENT = "HISTORY";
    private final String ACCOUNT_FRAGMENT = "ACCOUNT";
    private final String RECEIPT_FRAGMENT = "RECEIPT";

    private BeanstreamAPI beanstreamAPI;
    private Handler handler;

    private TabLayout tabLayout;
    private int currentTab;
    private int previousTab;

    private String currentFragment;
    private TransactionRequest request;

    private String completedTransactionId;

    private AlertDialog passwordRetryDialog;
    private AlertDialog errorDialog;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        JodaTimeAndroid.init(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_pay).setIcon(R.drawable.ic_credit_card_white_24dp));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_transactions).setIcon(R.drawable.ic_list_white_24dp));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_account).setIcon(R.drawable.ic_account_white_24dp));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                currentTab = tab.getPosition();

                if (tab.getPosition() == 0 && previousTab != tab.getPosition()) {
                    replaceFragment(new SaleFragment(), SALE_FRAGMENT, false);
                } else if (tab.getPosition() == 1 && previousTab != tab.getPosition()) {
                    replaceFragment(new HistoryFragment(), HISTORY_FRAGMENT, false);
                } else if (tab.getPosition() == 2 && previousTab != tab.getPosition()) {
                    AccountFragment accountFragment = new AccountFragment();
                    Bundle args = new Bundle();
                    args.putString("companyName", getIntent().getStringExtra("companyName"));
                    args.putString("companyPhone", getIntent().getStringExtra("companyPhone"));
                    args.putString("currency", getIntent().getStringExtra("currency"));
                    accountFragment.setArguments(args);
                    replaceFragment(accountFragment, ACCOUNT_FRAGMENT, false);
                }

                previousTab = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        GoldenEggsApplication application = (GoldenEggsApplication) getApplication();
        beanstreamAPI = application.getBeanstreamAPI();

        if (savedInstanceState == null) {
            SaleFragment saleFragment = new SaleFragment();
            FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, saleFragment, SALE_FRAGMENT);
            transaction.commit();
            currentFragment = SALE_FRAGMENT;
        } else {

            Gson gson = new Gson();
            String json = savedInstanceState.getString("transactionRequest");
            Type type = new TypeToken<TransactionRequest>() {
            }.getType();

            request = gson.fromJson(json, type);

            currentFragment = savedInstanceState.getString("currentFragment");
            currentTab = savedInstanceState.getInt("currentTab", 0);
            previousTab = savedInstanceState.getInt("previousTab", 0);
            completedTransactionId = savedInstanceState.getString("completedTransactionId", "");

            if (currentFragment.equals(RECEIPT_FRAGMENT)) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }

            if (!currentFragment.equals(SALE_FRAGMENT) && !currentFragment.equals(HISTORY_FRAGMENT) && !currentFragment.equals(ACCOUNT_FRAGMENT)) {
                tabLayout.setVisibility(View.GONE);
            }

            TabLayout.Tab tab = tabLayout.getTabAt(currentTab);
            if (tab != null) {
                tab.select();
            }

            //Ensure the password dialog is re-shown if they rotate the device if it was showing
            boolean isPasswordRetryShowing = savedInstanceState.getBoolean("isPasswordRetryShowing", false);
            if (isPasswordRetryShowing) {
                EventBus.getDefault().postSticky(new PasswordRequiredEvent());
            }

            boolean isProgressDialogShowing = savedInstanceState.getBoolean("isProgressDialogShowing", false);
            if (isProgressDialogShowing) {
                showUpdateProgressDialog();
            }
        }

        handler = new Handler();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);

        /*
        Used to start/stop the pin pad service. We host the actual logic in the GoldenEggsApplication class.
        It enables us to not start/stop the service when switching between activities or rotating, but still
        stop it when we fully leave the app, helping to save battery power.

        We don't want to start/stop the pin pad service while processing a transaction
        */
        if (!currentFragment.equals(PROCESSING_FRAGMENT)) {
            ((GoldenEggsApplication) getApplication()).activityResumed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_initialize_disconnected:
                Toast.makeText(this, R.string.pin_pad_not_connected, Toast.LENGTH_LONG).show();
                break;

            case R.id.action_initialize_connected:
                initializePinPad();
                showProgressBar();
                break;

            case R.id.action_logout:
                beanstreamAPI.abandonSession();
                beanstreamAPI.clearSavedPassword();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void replaceFragment(Fragment fragment, String tag, boolean addToBackstack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (addToBackstack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();

        currentFragment = tag;
    }

    public void initializePinPad() {
        GoldenEggsApplication application = (GoldenEggsApplication) getApplication();
        application.getBeanstreamAPI().initializePinPad();
    }

    @Override
    public void initiatePayment(TransactionRequest transactionRequest) {

        EventBus.getDefault().removeStickyEvent(transactionRequest);

        replaceFragment(new ProcessingFragment(), PROCESSING_FRAGMENT, true);
        tabLayout.setVisibility(View.GONE);

        request = transactionRequest;

        /*
        If we don't have saved credentials to re-attempt a transaction when a session fails
        we instead call authenticate session first.  This allows us to deal with a bad session
        before a customer is part way through the EMV logic which can cause interruptions while
        they are using the PIN pad.
        */
        if (beanstreamAPI.isRememberMe() && beanstreamAPI.isSessionSaved()) {

            /*
            On EMV transactions, this will enable tips to be prompted on the iCMP.
            When tips are enabled, you cannot do contactless transactions.
            */

            int[] tipPercentPresets = new int[]{10, 15, 20};
            beanstreamAPI.processTransaction(request, /* enableTips */ true, tipPercentPresets);
        } else {
            beanstreamAPI.authenticateSession();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(TitleEvent event) {
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(event.getTitle());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(AuthenticateSessionResponse response) {
        EventBus.getDefault().removeStickyEvent(response);
        if (response.isSessionAuthenticated()) {
            int[] tipPercentPresets = new int[]{10, 15, 20};
            beanstreamAPI.processTransaction(request, /* enableTips */ true, tipPercentPresets);
        } else {
            getSupportFragmentManager().popBackStack();
            tabLayout.setVisibility(View.VISIBLE);
            EventBus.getDefault().postSticky(new SessionInvalidEvent());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(AuthenticateSessionError error) {
        EventBus.getDefault().removeStickyEvent(error);
        getSupportFragmentManager().popBackStack();
        tabLayout.setVisibility(View.VISIBLE);
        EventBus.getDefault().postSticky(new SessionInvalidEvent());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final TransactionResponse response) {
        EventBus.getDefault().removeStickyEvent(response);

        ProcessingFragment processingFragment = (ProcessingFragment) getSupportFragmentManager().findFragmentByTag(PROCESSING_FRAGMENT);

        completedTransactionId = response.getTrnId();

        if (processingFragment != null) {
            if (response.isApproved()) {
                processingFragment.showTransactionApproved();
            } else {
                processingFragment.showTransactionDeclined();
            }
        }

        if (response.isApproved()) {
            if (response.isSignatureRequired()) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        //Sticky event so if the app is minimized during the delay, it will complete when it's resumed.
                        EventBus.getDefault().postSticky(new LoadSignatureEvent(completedTransactionId));
                    }
                };
                handler.postDelayed(runnable, 1500);

            } else {
                postDelayLoadGetReceiptEvent(completedTransactionId);
            }
        } else {
            postDelayLoadGetReceiptEvent(completedTransactionId);
            Toast.makeText(this, "Processing Response: " + response.getMessageText(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(TransactionError transactionError) {
        /*
         A transaction error will only occur in the following circumstances

         1) You attempt to do a transaction before calling create session, or after calling abandon session.
         2) You attempt to do an EMV transaction and the PIN pad is not connected
         3) You do an EMV transaction and the PIN pad fails to initialize.
        */
        EventBus.getDefault().removeStickyEvent(transactionError);
        getSupportFragmentManager().popBackStack();
        tabLayout.setVisibility(View.VISIBLE);
        showErrorDialog("", "Processing Error: " + transactionError.getUserFacingMessage());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadSignatureEvent event) {
        EventBus.getDefault().removeStickyEvent(event);

        Bundle args = new Bundle();
        args.putString("TransactionID", event.getTransactionId());
        SignatureFragment signatureFragment = new SignatureFragment();
        signatureFragment.setArguments(args);
        replaceFragment(signatureFragment, SIGNATURE_FRAGMENT, true);
    }

    private void postDelayLoadGetReceiptEvent(final String transactionId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //Sticky event so if the app is minimized during the delay, it will complete when it's resumed.
                EventBus.getDefault().postSticky(new LoadGetReceiptEvent(transactionId, getString(R.string.language_english)));

            }
        };
        handler.postDelayed(runnable, 1500);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadGetReceiptEvent event) {
        EventBus.getDefault().removeStickyEvent(event);

        if (event.getTransactionId() != null) {
            beanstreamAPI.getPrintReceipt(event.getTransactionId(), event.getLanguage());
        } else {
            currentFragment = SALE_FRAGMENT;
            tabLayout.setVisibility(View.VISIBLE);
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void historyItemPressed(String transactionId) {
        replaceFragment(new ReceiptFragment(), RECEIPT_FRAGMENT, true);
        tabLayout.setVisibility(View.GONE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        beanstreamAPI.getPrintReceipt(transactionId, getString(R.string.language_english));
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(GetReceiptResponse getReceiptResponse) {
        EventBus.getDefault().removeStickyEvent(getReceiptResponse);

        if (getReceiptResponse.isSuccessful()) {

            if (currentFragment.equals(RECEIPT_FRAGMENT)) {
                ReceiptFragment receiptFragment = (ReceiptFragment) getSupportFragmentManager().findFragmentByTag(RECEIPT_FRAGMENT);
                if (receiptFragment != null) {
                    receiptFragment.setReceipt(getReceiptResponse.getReceipt_customer());
                }

            } else if (currentFragment.equals(PROCESSING_FRAGMENT) || currentFragment.equals(SIGNATURE_FRAGMENT)) {
                ReceiptFragment receiptFragment = new ReceiptFragment();
                Bundle args = new Bundle();
                args.putString("customerReceipt", getReceiptResponse.getReceipt_customer());
                receiptFragment.setArguments(args);
                replaceFragment(receiptFragment, RECEIPT_FRAGMENT, true);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }

        } else {
            receiptFailure(getReceiptResponse.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(GetReceiptError error) {
        EventBus.getDefault().removeStickyEvent(error);
        receiptFailure(error.getUserFacingMessage());
    }

    private void receiptFailure(String message) {
        if (currentTab == 0) {
            currentFragment = SALE_FRAGMENT;
            tabLayout.setVisibility(View.VISIBLE);
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (currentTab == 1) {
            getSupportFragmentManager().popBackStack();
            currentFragment = HISTORY_FRAGMENT;
        }

        showErrorDialog("", "Error Generating Receipt:" + message);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(AttachSignatureResponse attachSignatureResponse) {
        EventBus.getDefault().removeStickyEvent(attachSignatureResponse);

        if (attachSignatureResponse.isSignatureAttached()) {
            beanstreamAPI.getPrintReceipt(completedTransactionId, getString(R.string.language_english));
        } else {

            SignatureFragment signatureFragment = (SignatureFragment) getSupportFragmentManager().findFragmentByTag(SIGNATURE_FRAGMENT);
            if (signatureFragment != null) {
                signatureFragment.showSubmitButton();
            }

            showErrorDialog("", attachSignatureResponse.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(AttachSignatureError attachSignatureError) {
        EventBus.getDefault().removeStickyEvent(attachSignatureError);

        SignatureFragment signatureFragment = (SignatureFragment) getSupportFragmentManager().findFragmentByTag(SIGNATURE_FRAGMENT);
        if (signatureFragment != null) {
            signatureFragment.showSubmitButton();
        }

        showErrorDialog("", attachSignatureError.getUserFacingMessage());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SessionInvalidEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        beanstreamAPI.clearSavedPassword();

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("invalidSession", true);
        startActivity(intent);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PasswordRequiredEvent event) {

        EventBus.getDefault().removeStickyEvent(event);

        final EditText passwordInput = new EditText(MainActivity.this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setLayoutParams(layoutParams);

        passwordRetryDialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle).create();

        passwordRetryDialog.setTitle(getString(R.string.session_retry_title));
        passwordRetryDialog.setMessage(getString(R.string.session_retry_message));
        passwordRetryDialog.setView(passwordInput);
        passwordRetryDialog.setCanceledOnTouchOutside(false);

        passwordRetryDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_password_retry_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String password = passwordInput.getText().toString();
                beanstreamAPI.sendRetryPassword(password);
            }
        });

        passwordRetryDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_cancel_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                beanstreamAPI.cancelPasswordRetry();
            }
        });

        passwordRetryDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                beanstreamAPI.cancelPasswordRetry();
            }
        });


        passwordRetryDialog.show();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(InitCardReaderResponse initCardReaderResponse) {
        EventBus.getDefault().removeStickyEvent(initCardReaderResponse);

        showCheckoutButton();

        if (initCardReaderResponse.isSuccessful() && initCardReaderResponse.isInitialized()) {
            if (!initCardReaderResponse.isSilent()) {
                if (initCardReaderResponse.isUpdateKeyEncryption()) {
                    showUpdateEncryptionKeyDialog(getString(R.string.update_pinpad_mandatory_message), getString(R.string.update_pinpad_mandatory_update));
                }
                Toast.makeText(this, R.string.initialized_message, Toast.LENGTH_LONG).show();
            }
        } else {
            if (!initCardReaderResponse.isSilent()) {
                showErrorDialog("", getString(R.string.initialization_failed_message));
            }
        }
    }

    private ProgressDialog progressDialog;

    public void showUpdateEncryptionKeyDialog(String message, String title) {

        AlertDialog updateEncryptionKeyDialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle).create();

        updateEncryptionKeyDialog.setTitle(title);
        updateEncryptionKeyDialog.setMessage(message);
        updateEncryptionKeyDialog.setCancelable(true);
        updateEncryptionKeyDialog.setCanceledOnTouchOutside(false);

        updateEncryptionKeyDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_update_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                beanstreamAPI.updatePinPad(MainActivity.this);
                showUpdateProgressDialog();

                progressDialog.show();
                tabLayout.setEnabled(false);
                arg0.dismiss();
            }
        });

        updateEncryptionKeyDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_cancel_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });

        updateEncryptionKeyDialog.show();
    }

    public void showUpdateProgressDialog() {

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getString(R.string.update_progress_dialog_message));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(UpdatePinPadResponse updatePinPadResponse) {

        if (updatePinPadResponse.isUpdateSuccessful()) {
            showErrorDialog("", getString(R.string.update_successful_message));
        } else {
            showErrorDialog("", String.format(getResources().getString(R.string.update_failed_message), updatePinPadResponse.getUpdateResponse()));
        }

        progressDialog.dismiss();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PinPadStateChangeEvent event) {
        EventBus.getDefault().removeStickyEvent(event);

        /*
        Use this to change icons and give visual queues in the app of connection status changes.

        Note - Do not use this event to initiate actions on the iCMP.

        The change to connected status, does not always mean the iCMP is "ready" to initialize or process a transaction.
        During the initial reboot there is a delay between when it says it's ready, and when it's actually ready.
        If you initiate an iCMP action on the change, it might fail.

        However, If you simply turn your bluetooth on/off it would be ready when the event triggers.
        Unfortunately we do not know if the state change is happening during a reboot.
        */

        //Invalidating the menu so the actionbar will update with the correct icon
        invalidateOptionsMenu();
    }

    private void showProgressBar() {
        SaleFragment saleFragment = (SaleFragment) getSupportFragmentManager().findFragmentByTag(SALE_FRAGMENT);
        if (saleFragment != null) {
            saleFragment.showProgressBar();
        }
    }

    private void showCheckoutButton() {
        SaleFragment saleFragment = (SaleFragment) getSupportFragmentManager().findFragmentByTag(SALE_FRAGMENT);
        if (saleFragment != null) {
            saleFragment.showCheckoutButton();
        }
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

    @Override
    public void onBackPressed() {

        //We don't want them cancelling out of processing a transaction or adding a signature via the back button
        if (currentFragment.equals(PROCESSING_FRAGMENT) || currentFragment.equals(SIGNATURE_FRAGMENT)) {
            return;
        }

        if (currentFragment.contentEquals(RECEIPT_FRAGMENT)) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            tabLayout.setVisibility(View.VISIBLE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            currentFragment = SALE_FRAGMENT;
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);

        /*
        Used to start/stop the pin pad service. We host the actual logic in the application class.
        It enables us to not start/stop the service when switching between activities or rotating, but still
        stop it when we fully leave the app, helping to save battery power.

        We don't want to start/stop the pin pad service while processing a transaction
        */
        if (!currentFragment.equals(PROCESSING_FRAGMENT)) {
            ((GoldenEggsApplication) getApplication()).activityPaused();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (passwordRetryDialog != null) {
            outState.putBoolean("isPasswordRetryShowing", passwordRetryDialog.isShowing());
        }

        if (progressDialog != null) {
            outState.putBoolean("isProgressDialogShowing", progressDialog.isShowing());
        }

        outState.putString("currentFragment", currentFragment);
        outState.putInt("currentTab", currentTab);
        outState.putInt("previousTab", previousTab);
        outState.putString("completedTransactionId", completedTransactionId);

        Gson gson = new Gson();
        Type type = new TypeToken<TransactionRequest>() {
        }.getType();
        String json = gson.toJson(request, type);

        outState.putString("transactionRequest", json);
    }

    @Override
    public void onDestroy() {

        if (passwordRetryDialog != null && passwordRetryDialog.isShowing()) {
            passwordRetryDialog.dismiss();
        }

        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        super.onDestroy();
    }
}
