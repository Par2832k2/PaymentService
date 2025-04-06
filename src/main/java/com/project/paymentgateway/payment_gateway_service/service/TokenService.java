package com.project.paymentgateway.payment_gateway_service.service;

import com.project.paymentgateway.payment_gateway_service.ExceptionHandler.ObjectNotFoundException;
import com.project.paymentgateway.payment_gateway_service.configuration.RsaKeyProperties;
import com.project.paymentgateway.payment_gateway_service.dao.User;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private final RsaKeyProperties rsaKeyProperties;
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 Hour
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    public TokenService(RsaKeyProperties rsaKeyProperties, UserDetailsService userDetailsService, UserService userService) {
        this.rsaKeyProperties = rsaKeyProperties;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    public String signIn(String userName, String password, boolean isInMemoryUser) throws ObjectNotFoundException {
        if(isInMemoryUser) return generateToken(userName, isInMemoryUser);
        User user = userService.retrieveUserByName(userName);
        if (BCrypt.checkpw(password, user.getPassword())) {
            return generateToken(userName, isInMemoryUser);
        } else {
            throw new AuthorizationServiceException("The userName and the password does not match");
        }
    }

    //  Generate JWT Token with Expiry
    public String generateToken(String userName, boolean isInMemoryUser) throws ObjectNotFoundException {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .claim("roles", getRoleForUser(userName, isInMemoryUser))
                .signWith(rsaKeyProperties.privateKey())
                .compact();
    }

    // Validate JWT Token
    public boolean validateToken(HttpServletRequest request) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(rsaKeyProperties.publicKey())
                    .build()
                    .parseClaimsJws(getTokenFromAuth(request));
            return true;
        } catch (JwtException e) {
            return false;
        }
    }


    public String getTokenFromAuth(HttpServletRequest request){
        return request.getHeader("Authorization").substring(7);
    }

    // Extract Username from Token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(rsaKeyProperties.publicKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getRoleForUser(String userName, boolean isInMemoryUser) throws ObjectNotFoundException {
        String role = "";
        if(isInMemoryUser){
            List<String> roles = userDetailsService.loadUserByUsername(userName).getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            role += roles.get(0).substring(5); // roles contain role of format "ROLE_ADMin" . Using Substring to isolate ADMIN from ROLE_
        } else {
            role += userService.getRoleFromUserName(userName);
        }
        return role;
    }
}
