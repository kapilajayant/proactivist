package com.jayant.proactivist.rest;

import okhttp3.logging.HttpLoggingInterceptor;

public class AddLoggingInterceptor {

    public static HttpLoggingInterceptor getInterceptor(){
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return loggingInterceptor;
    }
}