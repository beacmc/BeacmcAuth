package com.beacmc.beacmcauth.core.config.serializer;

import com.beacmc.beacmcauth.api.crypto.CryptoRegistry;
import com.beacmc.beacmcauth.api.crypto.hash.Hash;
import com.beacmc.beacmcauth.core.crypto.HashTypes;
import de.exlll.configlib.Serializer;

public class HashSerializer implements Serializer<Hash, String> {

    private static final Hash DEFAULT_HASH = HashTypes.BCRYPT2A;

    @Override
    public Hash deserialize(String element) {
        if (element == null || element.isBlank()) {
            return DEFAULT_HASH;
        }

        Hash hash = CryptoRegistry.INSTANCE.findById(element);
        return hash != null ? hash : DEFAULT_HASH;
    }

    @Override
    public String serialize(Hash element) {
        return (element != null ? element : DEFAULT_HASH).id();
    }
}
