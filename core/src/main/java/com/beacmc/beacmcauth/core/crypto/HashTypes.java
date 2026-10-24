package com.beacmc.beacmcauth.core.crypto;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.beacmc.beacmcauth.api.crypto.CryptoRegistry;
import com.beacmc.beacmcauth.api.crypto.hash.Hash;
import com.beacmc.beacmcauth.core.BaseBeacmcAuth;
import de.mkammerer.argon2.Argon2Factory;

@SuppressWarnings("unused")
public class HashTypes {

    public static final BCryptHash BCRYPT2Y = register(new BCryptHash(
            BaseBeacmcAuth.getInstance(),
            "bcrypt_2y",
            BCrypt.Version.VERSION_2Y
    ));
    public static final BCryptHash BCRYPT2A = register(new BCryptHash(
            BaseBeacmcAuth.getInstance(),
            "bcrypt_2a",
            BCrypt.Version.VERSION_2A
    ));
    public static final BCryptHash BCRYPT2B = register(new BCryptHash(
            BaseBeacmcAuth.getInstance(),
            "bcrypt_2b",
            BCrypt.Version.VERSION_2B
    ));
    public static final BCryptHash BCRYPT2X = register(new BCryptHash(
            BaseBeacmcAuth.getInstance(),
            "bcrypt_2x",
            BCrypt.Version.VERSION_2X
    ));
    public static final Argon2Hash ARGON2I = register(new Argon2Hash(
            BaseBeacmcAuth.getInstance(),
            "argon2i",
            Argon2Factory.Argon2Types.ARGON2i
    ));
    public static final Argon2Hash ARGON2D = register(new Argon2Hash(
            BaseBeacmcAuth.getInstance(),
            "argon2d",
            Argon2Factory.Argon2Types.ARGON2d
    ));
    public static Argon2Hash ARGON2ID = register(new Argon2Hash(
            BaseBeacmcAuth.getInstance(),
            "argon2id",
            Argon2Factory.Argon2Types.ARGON2id
    ));

    private static <T extends Hash> T register(T hash) {
        CryptoRegistry.INSTANCE.register(hash);
        hash.init();
        return hash;
    }
}
