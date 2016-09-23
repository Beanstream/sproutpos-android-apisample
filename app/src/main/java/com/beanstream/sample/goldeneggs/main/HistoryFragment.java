package com.beanstream.sample.goldeneggs.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beanstream.mobile.sdk.transport.entity.CardTypes;
import com.beanstream.mobile.sdk.transport.entity.Error.SearchTransactionError;
import com.beanstream.mobile.sdk.transport.entity.Request.SearchTransactionRequest;
import com.beanstream.mobile.sdk.transport.entity.Response.SearchTransactionResponse;
import com.beanstream.mobile.sdk.transport.entity.TransactionTypes;
import com.beanstream.mobile.sdk.transport.events.SessionInvalidEvent;
import com.beanstream.mobilesdk.BeanstreamAPI;
import com.beanstream.mobilesdk.BeanstreamEvents;
import com.beanstream.sample.goldeneggs.GoldenEggsApplication;
import com.beanstream.sample.goldeneggs.R;
import com.beanstream.sample.goldeneggs.events.TitleEvent;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Locale;

import de.greenrobot.event.EventBus;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 03/02/2016.
 */
public class HistoryFragment extends Fragment implements BeanstreamEvents.SearchTransaction {

    public final String FRAGMENT_TITLE = "Transactions";
    public HistoryCallback historyCallback = sDummyCallbacks;

    View rootView;
    BeanstreamAPI beanstreamAPI;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    TextView noResults;
    Bundle extras;

    public interface HistoryCallback {
        void historyItemPressed(String transactionId);

        void showErrorDialog(String title, String message);
    }

    private static HistoryCallback sDummyCallbacks = new HistoryCallback() {
        public void historyItemPressed(String transactionId) {
        }

        public void showErrorDialog(String title, String message) {
        }
    };

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        try {
            historyCallback = (HistoryCallback) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement history_callback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        //Any pending sticky is removed in case you come back after leaving an already started query that finished.
        EventBus.getDefault().removeStickyEvent(SearchTransactionResponse.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_history, container, false);

            progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
            noResults = (TextView) rootView.findViewById(R.id.noResultsText);

            GoldenEggsApplication application = (GoldenEggsApplication) getActivity().getApplication();
            beanstreamAPI = application.getBeanstreamAPI();

            extras = getArguments();

            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);

            recyclerView = (RecyclerView) rootView.findViewById(R.id.searchResultsRecycler);
            recyclerView.setLayoutManager(llm);
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

            if (savedInstanceState == null) {

                showProgressBar();

                SearchTransactionRequest searchTransactionRequest = new SearchTransactionRequest();
                searchTransactionRequest.setServiceName(SearchTransactionRequest.SERVICE_TRANS_HISTORY_MINIMAL);
                searchTransactionRequest.setRptOrder(SearchTransactionRequest.REPORT_ORDER_DESCENDING);
                searchTransactionRequest.setRptFromDateTime("2015-03-01 00:00:00");
                searchTransactionRequest.setRptToDateTime("2050-01-01 00:00:00");
                searchTransactionRequest.setRptStartRow("1");
                searchTransactionRequest.setRptEndRow("50");

                beanstreamAPI.searchTransaction(searchTransactionRequest);
            }
        }
        return rootView;
    }

    public static class SearchResultsViewHolder extends RecyclerView.ViewHolder {
        protected TextView date;
        protected TextView time;
        protected TextView transactionId;
        protected TextView amount;
        protected ImageView transactionTypeImage;
        protected ImageView transactionStatusImage;

        public SearchResultsViewHolder(View view) {
            super(view);

            date = (TextView) view.findViewById(R.id.date);
            time = (TextView) view.findViewById(R.id.time);
            transactionId = (TextView) view.findViewById(R.id.t_id);
            amount = (TextView) view.findViewById(R.id.amount);
            transactionTypeImage = (ImageView) view.findViewById(R.id.card_image);
            transactionStatusImage = (ImageView) view.findViewById(R.id.transaction_status);
        }
    }

    public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsViewHolder> {

        private ArrayList<SearchTransactionResponse.TransactionDetail> transactionDetails;

        DateTimeFormatter dateSourceFormat = DateTimeFormat.forPattern("M/d/yyyy h:mm:ss a");
        DateTimeFormatter dateOutputFormat = DateTimeFormat.forPattern("MMM dd yyyy");
        DateTimeFormatter timeOutputFormat = DateTimeFormat.forPattern("h:mm a");

        public SearchResultsAdapter(ArrayList<SearchTransactionResponse.TransactionDetail> transactionDetails) {
            this.transactionDetails = transactionDetails;
        }

        @Override
        public int getItemCount() {
            return transactionDetails.size();
        }

        @Override
        public void onBindViewHolder(final SearchResultsViewHolder searchViewHolder, final int i) {
            final SearchTransactionResponse.TransactionDetail transactionDetail = transactionDetails.get(i);

            DateTime date = DateTime.parse(transactionDetail.getTrnDateTime(), dateSourceFormat.withLocale(Locale.CANADA));

            searchViewHolder.date.setText(dateOutputFormat.print(date));
            searchViewHolder.time.setText(timeOutputFormat.print(date));
            searchViewHolder.transactionId.setText(String.format("# %1$s", transactionDetail.getTrnId()));
            searchViewHolder.amount.setText(String.format("$%1$s", transactionDetail.getTrnAmount()));
            searchViewHolder.transactionTypeImage.setImageDrawable(getTransactionTypeImage(transactionDetail.getTrnType(), transactionDetail.getPaymentMethodByCardType()));
            searchViewHolder.transactionStatusImage.setImageDrawable(getStatusImage(transactionDetail.trnResponse));

            searchViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    historyCallback.historyItemPressed(transactionDetails.get(searchViewHolder.getAdapterPosition()).getTrnId());
                }
            });

        }

        public Drawable getStatusImage(int status) {
            if (status == 1) {
                return ContextCompat.getDrawable(getActivity(), R.drawable.approved_processing);
            } else {
                return ContextCompat.getDrawable(getActivity(), R.drawable.declined_processing);
            }
        }

        public Drawable getTransactionTypeImage(String transactionType, String paymentMethod) {

            Drawable image = ContextCompat.getDrawable(getActivity(), R.drawable.ic_credit_card_grey600_24dp);

            switch (transactionType) {

                case TransactionTypes.PURCHASE:
                    image = getPurchaseImage(paymentMethod);
                    break;

                case TransactionTypes.PRE_AUTH:
                    image = ContextCompat.getDrawable(getActivity(), R.drawable.ic_play_box_outline_grey600_24dp);
                    break;

                case TransactionTypes.PRE_AUTH_COMPELTE:
                    image = ContextCompat.getDrawable(getActivity(), R.drawable.ic_credit_card_grey600_24dp);
                    break;

                case TransactionTypes.REFUND:
                    image = ContextCompat.getDrawable(getActivity(), R.drawable.ic_arrow_left_bold_circle_grey600_24dp);
                    break;
            }

            return image;
        }

        private Drawable getPurchaseImage(String paymentMethod) {

            Drawable image;

            switch (paymentMethod) {
                case CardTypes.CASH:
                    image = ContextCompat.getDrawable(getActivity(), R.drawable.ic_cash_100_grey600_24dp);
                    break;
                case CardTypes.CHEQUE:
                    image = ContextCompat.getDrawable(getActivity(), R.drawable.ic_file_document_grey600_24dp);
                    break;
                default:
                    image = ContextCompat.getDrawable(getActivity(), R.drawable.ic_credit_card_grey600_24dp);
                    break;
            }

            return image;
        }

        @Override
        public SearchResultsViewHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.search_list_item_two, viewGroup, false);

            return new SearchResultsViewHolder(itemView);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SearchTransactionResponse searchTransactionResponse) {

        EventBus.getDefault().removeStickyEvent(searchTransactionResponse);

        hideProgressBar();

        if (searchTransactionResponse.isSearchReportGenerated()) {
            if (searchTransactionResponse.getTransactionRecords().getTransactionDetail().size() > 0) {
                recyclerView.setVisibility(View.VISIBLE);
                SearchResultsAdapter adapter = new SearchResultsAdapter(searchTransactionResponse.getTransactionRecords().transactionDetails);
                recyclerView.setAdapter(adapter);
            } else {
                noResults.setVisibility(View.VISIBLE);
            }
        } else {
            noResults.setText(searchTransactionResponse.getMessage());
            noResults.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SearchTransactionError error) {
        EventBus.getDefault().removeStickyEvent(error);
        hideProgressBar();
        historyCallback.showErrorDialog("", error.getUserFacingMessage());
    }

    @Override
    public void onEventMainThread(SessionInvalidEvent sessionInvalidEvent) {
        //Handled in MainActivity
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
        EventBus.getDefault().post(new TitleEvent(FRAGMENT_TITLE));
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }
}