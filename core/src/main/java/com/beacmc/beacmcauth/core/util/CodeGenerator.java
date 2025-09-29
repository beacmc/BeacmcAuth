package com.beacmc.beacmcauth.core.util;

import java.security.SecureRandom;

public class CodeGenerator {

    public static String generate(String chars, int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            code.append(chars.charAt(index));
        }

        return code.toString();
    }
}
