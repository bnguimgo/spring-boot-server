package com.bnguimgo.springbootrestserver.security.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Create and validate Json Web Token
 * Source: https://www.javainuse.com/spring/boot-jwt
 */
//JSON Web Token (JWT) is often used in REST API security
@Component
@Getter
public class JwtTokenUtil implements Serializable {

	@Serial
	private static final long serialVersionUID = -2550185165626007488L;
	//durée expiration du token en minutes --> ChronoUnit.MINUTES
	//On peut avoir aussi la durée d'expiration en heure, jour, semaine ou jamais, etc. Il faut juste changer --> ChronoUnit.xxx
	public static final long JWT_TOKEN_VALIDITY = 20; //expiration in minutes

	@Value("${jwt.secret}")
	String secret; //code secret à garder dans un fichier pour déchiffrage du token

	public JwtTokenUtil() {
	}

	//for retrieving any information from token we will need the secret key
	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}

	//Generic method for retrieving any information
	private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	//retrieve username from jwt token
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	//retrieve expiration date from jwt token
	public Date getExpDate(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	//check if the token has expired
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpDate(token);
		LocalDateTime now = LocalDateTime.now();
		Date nowDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
		return expiration.before(nowDate);
	}

	/**
	 * Create JWT token
	 * @param claims datas to protect
	 * @param subject Identifier string used to create the token
	 * @return Return the generated token and sign it with SignatureAlgorithm.HS512
	 */
	//while creating the token -
	//1. Define  claims (Object to protect) of the token, startDate, expirationDate, Subject (Identifier used to create the token), and the ID
	//2. Sign the JWT using the HS512 algorithm and secret key.
	//3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	//   compaction of the JWT to a URL-safe string
	private String doGenerateToken(Map<String, Object> claims, String subject) {
		LocalDateTime now = LocalDateTime.now();
		Date startDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
		LocalDateTime dateTimeExpiration = now.plus(Duration.of(JWT_TOKEN_VALIDITY, ChronoUnit.MINUTES));
		Date expirationDate = Date.from(dateTimeExpiration.atZone(ZoneId.systemDefault()).toInstant());
		return Jwts.builder()
				.setClaims(claims)//datas to protect
				.setSubject(subject)//Identifier string used to create the token
				.setIssuedAt(startDate) //startDate
				.setExpiration(expirationDate)//expirationDate
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	/**
	 * validate token
	 * @param token token to validate
	 * @param userDetails userDetails from spring security
	 * @return return true if userDetails name match the userName from the token and token is not expired
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	/**
	 * generate token for user, based on userName or login
	 * @param login User logging
	 * @param passwordToProtect password to protect
	 * @return return the token generated
	 */
	public String generateToken(String login, String passwordToProtect) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(login, passwordToProtect);
		return doGenerateToken(claims, login);
	}
}