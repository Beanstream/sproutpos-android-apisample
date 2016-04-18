package com.beanstream.sample.goldeneggs.events;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 03/02/2016.
 */
public class TitleEvent {

    private String title;

    public TitleEvent(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
