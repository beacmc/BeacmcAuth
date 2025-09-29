package com.beacmc.beacmcauth.api.cache.cooldown;

import com.beacmc.beacmcauth.api.cache.CachedData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class CooldownUser<ID> implements CachedData<ID> {

    private ID id;
    private long cooldown;
}
