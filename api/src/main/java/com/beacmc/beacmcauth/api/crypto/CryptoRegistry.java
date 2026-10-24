package com.beacmc.beacmcauth.api.crypto;

import com.beacmc.beacmcauth.api.crypto.hash.Hash;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CryptoRegistry {

    public static CryptoRegistry INSTANCE = new CryptoRegistry();
    private final List<Hash> hashTypes = new ArrayList<>();

    public void register(Hash hashType) {
        if (!hashTypes.contains(hashType)) {
            hashTypes.add(hashType);
        }
    }

    public void unregister(Hash hashType) {
        hashTypes.remove(hashType);
    }

    public Hash findById(String id) {
        return hashTypes.stream()
                .filter(type -> type.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}
