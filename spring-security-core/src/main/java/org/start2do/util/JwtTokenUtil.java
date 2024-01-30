package org.start2do.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import javax.crypto.spec.SecretKeySpec;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.start2do.dto.UserCredentials;
import reactor.core.publisher.Mono;

@UtilityClass
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    public static String SECRET = null;
    private final static String USERNAME = "username";
    private final static String ROLES = "roles";
    private final static String MENUS = "menus";
    public final static String AUTHORIZATION = "Authorization";
    public final static String AUTHORIZATIONStr = "AuthorizationStr";
    public static String Bearer = "Bearer ";
    public static int BearerLen = 7;
    public static boolean CheckExpired = true;
    public static boolean MockUser = false;
    public static String MockUserName = "admin";
    public static Integer MockUserId = 1;
    public static boolean IsWebFlux = true;


    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(USERNAME)).toString();
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 返回所有附加信息
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET).build().parseClaimsJws(token).getBody();
    }

    //是否过期
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date()) && CheckExpired;

    }

    //生成Token
    public String generateToken(UserCredentials userCredentials) {
        return doGenerateToken(userCredentials);
    }

    private String doGenerateToken(UserCredentials userCredentials) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(USERNAME, userCredentials.getUsername());
        map.put(MENUS, userCredentials.getMenus());
        map.put(ROLES, userCredentials.getRoles());
        Map<String, Object> customInfo = userCredentials.getCustomInfo();
        if (customInfo != null) {
            for (Entry<String, Object> entry : customInfo.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return Jwts.builder().setClaims(map).setIssuedAt(new Date(System.currentTimeMillis()))
            .setSubject(String.valueOf(userCredentials.getId()))
            .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
            .signWith(SignatureAlgorithm.HS512, SECRET).compact();
    }

    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


    public String genKey() {
        Key KEY = new SecretKeySpec(StringUtils.randomString(2048).getBytes(), SignatureAlgorithm.HS512.getJcaName());
        return Base64.getEncoder().encodeToString(KEY.getEncoded());
    }

    public static void main(String[] args) {
        System.out.println(genKey());
    }

    public Integer getUserId() {
        if (MockUser) {
            return MockUserId;
        }
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra == null) {
            return null;
        }
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        String header = request.getHeader(AUTHORIZATION);
        return Optional.ofNullable(getClaimFromToken(header.substring(BearerLen), Claims::getSubject))
            .map(Integer::parseInt).orElse(null);
    }

    public Mono<Integer> getUserIdReactive() {
        return Mono.deferContextual(ctx -> Mono.just(ctx.get(JwtTokenUtil.AUTHORIZATION))).cast(UserCredentials.class)
            .map(UserCredentials::getId);
    }

    public Mono<String> getUserNameReactive() {
        return Mono.deferContextual(ctx -> Mono.just(ctx.get(JwtTokenUtil.AUTHORIZATION))).cast(UserDetails.class)
            .map(UserDetails::getUsername);
    }

    public Mono<String> getRealNameReactive() {
        return Mono.deferContextual(ctx -> Mono.just(ctx.get(JwtTokenUtil.AUTHORIZATION))).cast(UserCredentials.class)
            .map(UserCredentials::getRealName);
    }

    public String getUserName() {
        if (MockUser) {
            return MockUserName;
        }
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra == null) {
            return null;
        }
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        String header = request.getHeader(AUTHORIZATION);
        return getUsernameFromToken(header.substring(BearerLen));
    }


}
