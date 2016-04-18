package com.beanstream.sample.goldeneggs.events;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 11/02/2016.
 */
public class LoadGetReceiptEvent {

    String transactionId;


    String language;

    public LoadGetReceiptEvent(String transactionId, String language) {
        this.transactionId = transactionId;
        this.language = language;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getLanguage() {
        return language;
    }

}
