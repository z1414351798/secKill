package com.z.shop.user_service.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class RsaKeyGenerator {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        System.out.println("PRIVATE KEY:");
        System.out.println(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));

        System.out.println("PUBLIC KEY:");
        System.out.println(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
    }
}
