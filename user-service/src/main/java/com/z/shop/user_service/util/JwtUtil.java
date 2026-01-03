package com.z.shop.user_service.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;

public class JwtUtil {

    private static final PrivateKey PRIVATE_KEY;

    static {
        try {
            Properties props = new Properties();

            // 从 classpath 加载
            InputStream in = JwtUtil.class
                    .getClassLoader()
                    .getResourceAsStream("key/jwt.properties");

            if (in == null) {
                throw new RuntimeException("jwt.properties not found");
            }

            props.load(in);
            String base64Key = props.getProperty("jwt.private-key");

            if (base64Key == null || base64Key.isBlank()) {
                throw new RuntimeException("jwt.private-key is empty");
            }

            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            PRIVATE_KEY = KeyFactory.getInstance("RSA")
                    .generatePrivate(spec);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT private key", e);
        }
    }

    public static String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(PRIVATE_KEY, SignatureAlgorithm.RS256)
                .compact();
    }
}
