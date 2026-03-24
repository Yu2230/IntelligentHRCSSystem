package com.yyds.hrcscommon.utils;


import com.yyds.hrcscommon.constants.ConfigEnum;
import com.yyds.hrcscommon.constants.TimeOutEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

public class JwtUtils {
    // 生成jwt
    private final static Duration expiration = Duration.ofHours(TimeOutEnum.JWT_TIME_OUT.getTimeOut());

    //public static SecretKey secretKey =  Jwts.SIG.HS256.key().build();
    public static SecretKey secretKey = Keys.hmacShaKeyFor(ConfigEnum.TOKEN_SECRET_KEY.getValue().getBytes(StandardCharsets.UTF_8));
    public static String generate(String userID){
        Date expiryDate = new Date(System.currentTimeMillis() + expiration.toMillis());

        return Jwts.builder()
                .setSubject(userID)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // 解析jwt
    public static Claims parse(String token) throws JwtException {

        if (StringUtils.isEmpty(token)){
            throw new JwtException("token 为空");
        }
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static String getUserID(String token) throws JwtException {
        Claims claims = parse(token);
        return claims.getSubject();
    }

    public static long getUserId(String token)throws JwtException {
        Claims claims = parse(token);
        return Long.valueOf(claims.getSubject());
    }
}
