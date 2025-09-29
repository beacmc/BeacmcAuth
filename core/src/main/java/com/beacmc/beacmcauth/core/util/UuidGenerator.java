package com.beacmc.beacmcauth.core.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UuidGenerator {

    public static UUID byName(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
