package sfera.eduportal2.Security;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sfera.eduportal2.Exception.JwtException;

import java.util.Date;

@Component
public class JwtProvider {
    @Value("${jwt.token.ttl}")
    private long ttl;

    @Value("${jwt.secretkey}")
    private String secretKey;

    public String generateToken(String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(now + ttl))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
    public String getEmailFromToken(String token) {
        try{
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        }catch (ExpiredJwtException e){
            throw  new JwtException("Jwt token has expired" + e.getMessage());
        }catch (SignatureException e){
            throw  new JwtException("Jwt token signature exception" + e.getMessage());
        }catch (JwtException e){
            throw  new JwtException("Jwt exception" + e.getMessage());
        }
    }
}
