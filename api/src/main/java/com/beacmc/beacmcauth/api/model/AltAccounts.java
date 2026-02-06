package com.beacmc.beacmcauth.api.model;

import com.beacmc.beacmcauth.api.cache.CachedData;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AltAccounts implements CachedData<String> {

    private String ip;
    private List<String> names;

    @Override
    public String getId() {
        return ip;
    }
}
