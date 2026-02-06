package com.beacmc.beacmcauth.api.auth.premium.response;

public interface Response<T> {

    boolean isSuccess();

    boolean isRateLimited();

    long getRateLimitTime();

    int getStatusCode();

    T getData();
}
