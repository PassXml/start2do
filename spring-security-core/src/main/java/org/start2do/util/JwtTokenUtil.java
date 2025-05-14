package org.start2do.util;

import com.nimbusds.jose.JWSAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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

  public static long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
  public static String SECRET = null;
  public static final String USERNAME = "username";


  public static final String ROLES = "roles";
  public static final String MENUS = "menus";
  public static final String REALNAME = "realName";
  public static final String AUTHORIZATION = "Authorization";
  public static final String AUTHORIZATIONStr = "AuthorizationStr";
  public static String Bearer = "Bearer ";
  public static int BearerLen = 7;
  public static boolean CheckExpired = true;
  public static boolean MockUser = false;
  public static String MockUserName = "admin";
  public static String MockUserNameRealName = "admin";
  public static String MockUserId = "1";
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

  public static byte[] getSecret() {
    return Base64.getDecoder().decode(SECRET);
  }

  /** 返回所有附加信息 */
  private Claims getAllClaimsFromToken(String token) {
    try {
      com.nimbusds.jose.JWEObject jweObject = com.nimbusds.jose.JWEObject.parse(token);
      jweObject.decrypt(new com.nimbusds.jose.crypto.DirectDecrypter(getSecret()));
      com.nimbusds.jwt.SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
      signedJWT.verify(new com.nimbusds.jose.crypto.MACVerifier(getSecret()));
      com.nimbusds.jwt.JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

      // 将 nimbus 的 claims 转换为 JJWT 的 Claims 对象以保持兼容性
      io.jsonwebtoken.Claims claims = Jwts.claims();
      claims.putAll(claimsSet.getClaims());
      return claims;
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse JWE token", e);
    }
  }

  // 是否过期
  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date()) && CheckExpired;
  }

  // 生成Token
  public String generateToken(UserCredentials userCredentials) {
    return doGenerateToken(userCredentials);
  }

  private String doGenerateToken(UserCredentials userCredentials) {
    try {
      HashMap<String, Object> map = new HashMap<>();
      map.put(USERNAME, userCredentials.getUsername());
      map.put(MENUS, userCredentials.getMenus());
      map.put(ROLES, userCredentials.getRoles());
      map.put(REALNAME, userCredentials.getRealName());
      Map<String, Object> customInfo = userCredentials.getUserExtInfo();
      if (customInfo != null) {
          map.putAll(customInfo);
      }
      // 使用 nimbus-jose-jwt 进行 JWE 加密
      com.nimbusds.jwt.JWTClaimsSet claimsSet =
          new com.nimbusds.jwt.JWTClaimsSet.Builder()
              .subject(String.valueOf(userCredentials.getId()))
              .issueTime(new Date(System.currentTimeMillis()))
              .expirationTime(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
              .claim(USERNAME, userCredentials.getUsername())
              .claim(MENUS, userCredentials.getMenus())
              .claim(ROLES, userCredentials.getRoles())
              .claim(REALNAME, userCredentials.getRealName())
              .build();

      com.nimbusds.jwt.SignedJWT signedJWT = new com.nimbusds.jwt.SignedJWT(
          new com.nimbusds.jose.JWSHeader(JWSAlgorithm.HS256),
          claimsSet);
      signedJWT.sign(new com.nimbusds.jose.crypto.MACSigner(getSecret()));

      com.nimbusds.jose.JWEObject jweObject = new com.nimbusds.jose.JWEObject(
          new com.nimbusds.jose.JWEHeader.Builder(com.nimbusds.jose.JWEAlgorithm.DIR, com.nimbusds.jose.EncryptionMethod.A256GCM)
              .contentType("JWT")
              .build(),
          new com.nimbusds.jose.Payload(signedJWT));
      jweObject.encrypt(new com.nimbusds.jose.crypto.DirectEncrypter(getSecret()));

      return jweObject.serialize();
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate JWE token", e);
    }
  }

  // validate token
  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  public String genKey() {
    // 生成一个 32 字节（256 位）的密钥
    byte[] keyBytes = new byte[32];
    new java.security.SecureRandom().nextBytes(keyBytes);
    return Base64.getEncoder().encodeToString(keyBytes);
  }

  public static void main(String[] args) {
    System.out.println(new JwtTokenUtil().genKey());
  }

  public String getUserId() {
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
        .orElse(null);
  }

  public Mono<String> getUserIdReactive() {
    return Mono.deferContextual(ctx -> Mono.just(ctx.get(JwtTokenUtil.AUTHORIZATION)))
        .cast(UserCredentials.class)
        .map(UserCredentials::getId);
  }

  public Mono<String> getUserNameReactive() {
    return Mono.deferContextual(ctx -> Mono.just(ctx.get(JwtTokenUtil.AUTHORIZATION)))
        .cast(UserDetails.class)
        .map(UserDetails::getUsername);
  }

  public Mono<String> getRealNameReactive() {
    return Mono.deferContextual(ctx -> Mono.just(ctx.get(JwtTokenUtil.AUTHORIZATION)))
        .cast(UserCredentials.class)
        .map(UserCredentials::getRealName);
  }

  public String getRealName() {
    if (MockUser) {
      return MockUserNameRealName;
    }
    RequestAttributes ra = RequestContextHolder.getRequestAttributes();
    if (ra == null) {
      return null;
    }
    ServletRequestAttributes sra = (ServletRequestAttributes) ra;
    HttpServletRequest request = sra.getRequest();
    String header = request.getHeader(AUTHORIZATION);
    if (header == null || !header.startsWith(Bearer)) {
      return null;
    }
    String token = header.substring(BearerLen);
    Claims claims = getAllClaimsFromToken(token);
    return Optional.ofNullable(claims.get(REALNAME)).map(Object::toString).orElse(null);
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

  public static String getUserId(String jwtStr) {
    return Optional.ofNullable(getClaimFromToken(jwtStr, Claims::getSubject)).orElse(null);
  }
}
