package com.beacmc.beacmcauth.api.crypto.hash;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(chain = true)
public class HashedPassword {

    private String hashId;
    private String password;
}
