package com.beanstream.sample.goldeneggs.events;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 11/02/2016.
 */
public class LoadSignatureEvent {

    String transactionId;

    public LoadSignatureEvent(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
