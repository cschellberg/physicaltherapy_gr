package com.agileapps.pt.exceptions;

/**
 * Created by dschellb on 9/26/2015.
 */
public class GoogleDriverException  extends Exception{

    public GoogleDriverException(String detailMessage) {
        super(detailMessage);
    }

    public GoogleDriverException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
