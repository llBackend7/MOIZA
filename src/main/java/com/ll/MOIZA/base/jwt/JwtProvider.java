package com.ll.MOIZA.base.jwt;

import com.ll.MOIZA.base.utils.Ut;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtProvider {
    private SecretKey cachedSecretKey;

    @Value("${custom.jwt.secretKey}")
    private String secretKeyPlain;

    private SecretKey _getSecretKey() {
        String keyBase64Encoded = Base64.getEncoder().encodeToString(secretKeyPlain.getBytes());
        return Keys.hmacShaKeyFor(keyBase64Encoded.getBytes());
    }

    public SecretKey getSecretKey() {
        if (cachedSecretKey == null) cachedSecretKey = _getSecretKey();

        return cachedSecretKey;
    }

    public String genToken(Map<String, Object> claims, int seconds) {
        long now = new Date().getTime();
        Date expire = new Date(now + 1000L * seconds);

        return Jwts.builder()
                .claim("body", Ut.json.toStr(claims))
                .setExpiration(expire)
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean verify(String accessToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(accessToken);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Map<String, Object> getClaims(String accessToken) {
        String body = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .get("body", String.class);

        return Ut.json.toMap(body);
    }
}
