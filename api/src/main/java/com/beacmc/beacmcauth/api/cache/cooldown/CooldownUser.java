package com.beacmc.beacmcauth.api.cache.cooldown;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class CooldownUser<ID> {

    private ID id;
    private long cooldown;
}
