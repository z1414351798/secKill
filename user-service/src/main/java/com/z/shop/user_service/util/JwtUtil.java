package com.z.shop.user_service.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String PRIVATE_KEY_BASE64 = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQChhPHpJjpAp+TW2gcw39Kplez4bomdyzpbjxA2csA1hUdjAseBMpcpPo9Kn/dU4Nk7FjlFcygMLsKSFrUXOIsgaSRpHWS01RhIDOffisoU8/EVTYZNKXoTlHtmgfy+ZniNAWP5yuhlRGtMLzcSnFFksU90HTBilBrQMSIDdWhgVWzVlM40KIYfhw9jU48/cWEK3PGC3XMvUpZpawsdUHmk36svq9mTefa/uum/qNv9VvL/HvXyoK25BQtdFdInVstt2Bf+YmrwNAUtPlo4XBCg8nO2GAfEIv43kizoju0sjcGXMbnn/pOgVR0QlLFsgNUUOLzIQuihyeeBxGCfiaB1AgMBAAECggEAAIhVGYZyKI6VKH3cCmB2i+9uB6yqS1OlS9zRz3LQk5dD/WgqhbsdDfpXk8X+wAV6GeHbIFGT9yRRU797ld8ZGwCAaaX6lBu5bTAOX1Q7EddzEEsAOhwR+7i128g/xStGfaUx8fEmE9gQ/YA7WiubRhh35q4GSpDqnLdIwYJUgjAh6EQlYGRFXLzK5ULLp/Ylbbk6l/aFkquVQ1iShsj7804ShEULWtc6aCM4KTnzwQzK6d3+0cdu1MBV16QJTiuy/ik0RTMzuwsfKzT0V3RA1i8CuAvIfmlntPtG81C5gp+tgnzaBJK2oov5tNViufay3W275DpeyIMPRwlTa8Lj+QKBgQDXy3prH5AnbToEc+YuAtsSmYawBr3UlzvvDZVPOso9Ss1C3bqB6XzN++FiyjPxykS/7U2EdXCiUx3v5dG0wm8lac2+ujhPVd6dRf+bIxZ1CbV/WBB21RSejSlKiKZ0YlHWScusD8w8LLdFflPbzAjK2jGfmFQYGKxaDl/5zTWumQKBgQC/nL+vWh86DwUcP3dZ0uMMjGYriZx3kRdsgKhfJrt3irew39chgAe52xk/WNb5ZqZutYRpj9glPmmDjYZTcus0160IuDOq9dTPFDet6gQBKECGg/A4JPt5kVoOl/mo2jPc47opxllje/nzcPjKjOkv42zq4aDOgM+cJV+2lrj2PQKBgQDEW6DvLO9NEPTD7Ho8z9hsGksWjWvasCqXIbMFPyebkh8ogv4dDErGXDYAYKHXmAfq5JGuv/ZXnm4VRvgFFQ9x6c6PiH+IoUhJr9IgZlMe+FCwqX+Bwe5bP3OdIK7mJDj3Vnr/+/gJoV6623KPBB/UBas137RVoM3jvETU10fLsQKBgQCI500J06FmER88nJgA5K125MWHK+5Qe6k5y/8A5SKieclfJb+G3vwmLcYvPaFzVoVpqmWuKqlritlGthKYgw5MliC6jynTZ4uiXTafGwm7bfAUbCZxZvMKW/D1Zl5p52WQ2GDFwkNR0l23Myt/RQjDP4ItnAISKXSsgwOM6MnEnQKBgQCDv6roE9se1lhDqN8cU2cV8LLQ6SQKZ5k7k/Gfc5ez2ldYG85R64+YlHeL8tBzXvOWHUw5rngcrfivdw8cgShk0TlIAECpnjImaGCu1RMpqb42WtMmkf/2+n0uj+J4OGR/3JAOY1OcfxCFKoWre/IRlODdxYgc1fi4B02STFAgsw==";

    private final PrivateKey privateKey;

    public JwtUtil() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(PRIVATE_KEY_BASE64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
